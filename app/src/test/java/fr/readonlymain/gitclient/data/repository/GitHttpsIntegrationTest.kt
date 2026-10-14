package fr.readonlymain.gitclient.data.repository

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Credentials
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okio.Buffer
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/** Real JGit HTTP transport against loopback only, with a per-test trusted TLS certificate. */
class GitHttpsIntegrationTest {
    @get:Rule val temporary = TemporaryFolder()
    private val repository = DefaultGitRepository()
    private lateinit var originalFactory: SSLSocketFactory
    private lateinit var server: MockWebServer

    @Before fun setup() {
        originalFactory = HttpsURLConnection.getDefaultSSLSocketFactory()
        val certificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost")
            .addSubjectAlternativeName("127.0.0.1").addSubjectAlternativeName("::1").build()
        val serverTls = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        val clientTls = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
        HttpsURLConnection.setDefaultSSLSocketFactory(clientTls.sslSocketFactory())
        server = MockWebServer().apply { useHttps(serverTls.sslSocketFactory(), false); start() }
    }

    @After fun cleanup() {
        server.shutdown()
        HttpsURLConnection.setDefaultSSLSocketFactory(originalFactory)
    }

    private fun credential() = GitCredential(accountName = "loopback", username = "user", token = "test-secret",
        providerUrl = server.url("/").toString())

    private fun serveRepository(requireAuth: Boolean) {
        val (remote) = GitTestFixture(temporary.root).remoteWithSeed()
        File(remote, "info").mkdirs()
        Git.open(remote).use { git ->
            File(remote, "info/refs").writeText(git.repository.refDatabase.getRefsByPrefix("refs/heads/")
                .joinToString("") { "${it.objectId.name}\t${it.name}\n" })
        }
        File(remote, "objects/info").mkdirs()
        File(remote, "objects/info/packs").writeText(
            File(remote, "objects/pack").listFiles().orEmpty().filter { it.extension == "pack" }
                .joinToString("") { "P ${it.name}\n" } + "\n")
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (requireAuth && request.getHeader("Authorization") != Credentials.basic("user", "test-secret")) {
                    return MockResponse().setResponseCode(401).setHeader("WWW-Authenticate", "Basic realm=git-test")
                }
                val path = request.requestUrl!!.encodedPath.removePrefix("/repo.git/")
                val file = File(remote, path)
                if (!file.canonicalPath.startsWith(remote.canonicalPath + File.separator) || !file.isFile) {
                    return MockResponse().setResponseCode(404)
                }
                return MockResponse().setHeader("Content-Type", "text/plain")
                    .setBody(Buffer().write(file.readBytes()))
            }
        }
    }

    @Test fun `HTTPS cloning authenticates only to matching provider and transfers real Git objects`() = runBlocking {
        serveRepository(requireAuth = true)
        val destination = temporary.newFolder("clone")
        val result = repository.cloneRepo(server.url("/repo.git").toString(), listOf(credential()), destination.path) { _, _ -> }.getOrThrow()
        assertThat(File(result.folderPath, "file.txt").readLines()).containsExactly("initial")
        assertThat(repository.getCurrentBranch(result.folderPath).getOrThrow()).isEqualTo("trunk")
    }

    @Test fun `wrong host credentials are never sent to an HTTPS server`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setHeader("WWW-Authenticate", "Basic realm=test"))
        val result = repository.cloneRepo(server.url("/repo.git").toString(),
            listOf(credential().copy(providerUrl = "https://different.invalid")), temporary.newFolder("clone").path) { _, _ -> }
        assertThat(result.isFailure).isTrue()
        assertWithMessage(result.exceptionOrNull()?.stackTraceToString().orEmpty()).that(server.requestCount).isEqualTo(1)
        assertThat(server.takeRequest().getHeader("Authorization")).isNull()
    }

    @Test fun `HTTP server failure does not trigger credential retry`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))
        val result = repository.cloneRepo(server.url("/repo.git").toString(), listOf(credential()),
            temporary.newFolder("clone").path) { _, _ -> }
        assertThat(result.isFailure).isTrue()
        assertWithMessage(result.exceptionOrNull()?.stackTraceToString().orEmpty()).that(server.requestCount).isEqualTo(1)
    }

    @Test fun `authenticated HTTPS redirect cannot forward a secret to plain HTTP`() = runBlocking {
        MockWebServer().use { target ->
            target.start()
            val authenticated = AtomicBoolean(false)
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    if (request.getHeader("Authorization") == null) return MockResponse().setResponseCode(401)
                        .setHeader("WWW-Authenticate", "Basic realm=test")
                    authenticated.set(true)
                    return MockResponse().setResponseCode(302).setHeader("Location", target.url("/repo.git/info/refs"))
                }
            }
            val result = repository.cloneRepo(server.url("/repo.git").toString(), listOf(credential()),
                temporary.newFolder("clone").path) { _, _ -> }
            assertThat(result.isFailure).isTrue()
            assertThat(authenticated.get()).isTrue()
            assertThat(target.requestCount).isEqualTo(0)
        }
    }

    @Test fun `cancellation during an HTTPS response cleans partial clone data`() = runBlocking {
        server.enqueue(MockResponse().setHeadersDelay(1, TimeUnit.SECONDS).setResponseCode(503))
        val destination = temporary.newFolder("clone")
        val operation = async(Dispatchers.IO) {
            repository.cloneRepo(server.url("/repo.git").toString(), emptyList(), destination.path) { _, _ -> }
        }
        val request = server.takeRequest(5, TimeUnit.SECONDS)
        if (request == null && operation.isCompleted) operation.await().getOrThrow()
        assertThat(request).isNotNull()
        operation.cancel()
        withTimeout(5_000) { operation.join() }
        assertThat(runCatching { operation.await() }.exceptionOrNull()).isInstanceOf(CancellationException::class.java)
        assertThat(destination.listFiles()).isEmpty()
    }
}
