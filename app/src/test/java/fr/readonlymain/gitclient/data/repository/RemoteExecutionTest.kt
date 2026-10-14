package fr.readonlymain.gitclient.data.repository

import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.URIish
import org.junit.Test

class RemoteExecutionTest {
    private val credential = GitCredential(accountName = "test", username = "user", token = "secret")

    @Test fun `credentials never cross host port protocol or repository boundaries`() {
        assertThat(credentialAttempts("https://github.com/org/repo.git", listOf(credential))).containsExactly(null, credential).inOrder()
        listOf("https://evil.invalid/repo", "http://github.com/org/repo", "https://github.com:8443/org/repo")
            .forEach { assertThat(credentialAttempts(it, listOf(credential))).containsExactly(null) }
        val scoped = credential.copy(providerUrl = "https://github.com/org/repo")
        assertThat(credentialAttempts("https://github.com/org/repo.git", listOf(scoped))).containsExactly(null, scoped).inOrder()
        assertThat(credentialAttempts("https://github.com/other/repo.git", listOf(scoped))).containsExactly(null)
    }

    @Test fun `redirect challenges cannot bypass credential scope`() {
        val provider = scopedCredentials(credential)
        assertThat(runCatching {
            provider.get(URIish("https://evil.invalid/repo"), CredentialItem.Password())
        }.isFailure).isTrue()
        assertThat(runCatching {
            provider.get(URIish("http://github.com/repo"), CredentialItem.Password())
        }.isFailure).isTrue()
    }

    @Test fun `remote URLs reject embedded secrets and unsupported schemes`() {
        listOf("https://user:secret@github.com/repo", "https://github.com/repo?token=secret",
            "ssh://git@github.com/repo", "https:///missing-host", " https://github.com/repo")
            .forEach { assertThat(runCatching { validateRemoteUrl(it) }.isFailure).isTrue() }
    }

    @Test fun `only authentication errors trigger credential retry`() = runTest {
        var attempts = 0
        val result = withRemoteCredentials("https://github.com/org/repo", listOf(credential)) { provider, _ ->
            attempts++
            if (provider == null) throw TransportException("not authorized")
            "ok"
        }
        assertThat(result).isEqualTo("ok")
        assertThat(attempts).isEqualTo(2)
        attempts = 0
        val failure = runCatching {
            withRemoteCredentials("https://github.com/org/repo", listOf(credential)) { _, _ ->
                attempts++
                throw TransportException("connection reset")
            }
        }.exceptionOrNull() as GitOperationException
        assertThat(failure.kind).isEqualTo(GitFailureKind.TRANSPORT)
        assertThat(attempts).isEqualTo(1)
    }

    @Test fun `cancellation is propagated rather than retried or wrapped`() = runTest {
        var attempts = 0
        val failure = runCatching {
            withRemoteCredentials("https://github.com/org/repo", listOf(credential)) { _, _ ->
                attempts++
                throw CancellationException("cancelled")
            }
        }.exceptionOrNull()
        assertThat(failure).isInstanceOf(CancellationException::class.java)
        assertThat(attempts).isEqualTo(1)
    }
}
