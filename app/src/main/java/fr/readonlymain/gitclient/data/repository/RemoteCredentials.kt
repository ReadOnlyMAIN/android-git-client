package fr.readonlymain.gitclient.data.repository

import fr.readonlymain.gitclient.data.model.GitCredential
import java.net.URI

/** An invalid or non-HTTP provider never authorizes sending a saved secret. */
internal fun credentialAttempts(url: String, credentials: List<GitCredential>): List<GitCredential?> {
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
