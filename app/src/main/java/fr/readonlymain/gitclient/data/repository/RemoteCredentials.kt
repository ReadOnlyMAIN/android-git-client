package fr.readonlymain.gitclient.data.repository

import fr.readonlymain.gitclient.data.model.GitCredential
import java.net.URI
import java.io.File
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

internal fun validateRemoteUrl(url: String) {
    require(url.isNotBlank() && url == url.trim()) { "Invalid remote URL" }
    if (File(url).isAbsolute) return
    val uri = URI(url)
    require(uri.scheme?.lowercase() in setOf("https", "http", "file")) { "Unsupported remote protocol" }
    require(uri.rawUserInfo == null && uri.fragment == null && uri.query == null) {
        "Remote URLs must not contain credentials, queries, or fragments"
    }
    if (uri.scheme != "file") require(!uri.host.isNullOrBlank()) { "Remote URL has no host" }
    else require(uri.host.isNullOrEmpty() || uri.host == "localhost") { "Unsupported file URL host" }
}

/** Re-check each authentication challenge, including any redirected destination. */
internal fun scopedCredentials(credential: GitCredential): CredentialsProvider {
    val delegate = UsernamePasswordCredentialsProvider(credential.username, credential.token)
    return object : CredentialsProvider() {
        override fun isInteractive() = false
        override fun supports(vararg items: CredentialItem) = delegate.supports(*items)
        override fun get(uri: URIish, vararg items: CredentialItem): Boolean {
            require(credential in credentialAttempts(uri.toString(), listOf(credential))) {
                "Refusing credentials for an unauthorized remote"
            }
            return delegate.get(uri, *items)
        }
    }
}

/** An invalid or non-HTTP provider never authorizes sending a saved secret. */
internal fun credentialAttempts(url: String, credentials: List<GitCredential>): List<GitCredential?> {
    validateRemoteUrl(url)
    val remote = runCatching { URI(url) }.getOrNull()
    val matching = credentials.filter { credential ->
        val provider = runCatching { URI(credential.providerUrl) }.getOrNull()
        remote?.host != null && provider?.host != null &&
            remote.scheme.equals("https", true) && provider.scheme.equals("https", true) &&
            remote.host.equals(provider.host, true) && effectivePort(remote) == effectivePort(provider) &&
            (provider.path.isNullOrEmpty() || provider.path == "/" ||
                remote.path.removeSuffix(".git") == provider.path.trimEnd('/').removeSuffix(".git"))
    }
    return listOf(null) + matching
}

private fun effectivePort(uri: URI): Int = if (uri.port == -1) 443 else uri.port
