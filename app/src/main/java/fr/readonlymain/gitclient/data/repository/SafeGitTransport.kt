package fr.readonlymain.gitclient.data.repository

import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.TransportHttp
import org.eclipse.jgit.transport.http.HttpConnection
import org.eclipse.jgit.transport.http.HttpConnectionFactory
import java.io.IOException
import java.net.Proxy
import java.net.URL

/** Validate rewritten URLs too, and never forward an authenticated HTTP request across origins. */
internal val safeGitTransport = TransportConfigCallback { transport ->
    validateRemoteUrl(transport.uri.toPrivateString())
    transport.timeout = 60
    if (transport is TransportHttp) {
        val origin = URL(transport.uri.toString())
        val delegate = transport.httpConnectionFactory
        transport.httpConnectionFactory = object : HttpConnectionFactory {
            private fun check(url: URL) {
                if (!sameOrigin(origin, url) || url.userInfo != null) {
                    throw IOException("Refusing cross-origin Git HTTP redirect")
                }
            }
            override fun create(url: URL): HttpConnection {
                check(url)
                return delegate.create(url)
            }
            override fun create(url: URL, proxy: Proxy): HttpConnection {
                check(url)
                return delegate.create(url, proxy)
            }
        }
    }
}

internal fun sameOrigin(first: URL, second: URL): Boolean =
    first.protocol.equals(second.protocol, true) && first.host.equals(second.host, true) &&
        (if (first.port == -1) first.defaultPort else first.port) ==
        (if (second.port == -1) second.defaultPort else second.port)
