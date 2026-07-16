package com.github.nahnullscience.cypher_nexus.client.devtools.web

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * A tiny localhost-only web server for tinkering with cypher chains from a browser.
 *
 * Start this from a CLIENT-only entrypoint (see ClientSetup.kt) - it reads the cypher registry and
 * (later, if you extend it) client-only state, so it has no business existing on a dedicated server.
 */
object DevToolsServer {
    private const val PORT = 25599
    private val gson = Gson()
    private var server: HttpServer? = null

    val ip get() = server?.address

    fun start() {
        if (server != null) return

        val http = HttpServer.create(InetSocketAddress("127.0.0.1", PORT), 0)
        http.executor = Executors.newFixedThreadPool(2)

        http.createContext("/api/palette") { exchange ->
            respondJson(exchange) { gson.toJson(AstExporter.palette()) }
        }

        http.createContext("/api/ast") { exchange ->
            respondJson(exchange) {
                val body = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8)
                val ids = gson.fromJson(body, Array<String>::class.java)?.toList() ?: emptyList()
                gson.toJson(AstExporter.buildAst(ids))
            }
        }

        http.createContext("/") { exchange -> serveStatic(exchange) }

        http.start()
        server = http
        CypherNexus.LOGGER.info("cypher-nexus devtools listening on http://127.0.0.1:{}/", PORT)
    }

    fun stop() {
        server?.stop(0)
        server = null
    }

    // ------------------------------------------------------------------------------------------

    private inline fun respondJson(exchange: HttpExchange, crossinline body: () -> String) {
        try {
            write(exchange, 200, "application/json; charset=utf-8", body().toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            CypherNexus.LOGGER.error("devtools request to {} failed", exchange.requestURI, e)
            val msg = (e.message ?: e::class.simpleName ?: "error").toByteArray(StandardCharsets.UTF_8)
            write(exchange, 500, "text/plain; charset=utf-8", msg)
        }
    }

    private fun serveStatic(exchange: HttpExchange) {
        val requested = exchange.requestURI.path
        val resourcePath = "/devtools" + if (requested == "/") "/index.html" else requested

        val bytes = javaClass.getResourceAsStream(resourcePath)?.use { it.readBytes() }
        if (bytes == null) {
            write(exchange, 404, "text/plain; charset=utf-8", "not found".toByteArray())
            return
        }

        val contentType = when {
            resourcePath.endsWith(".html") -> "text/html; charset=utf-8"
            resourcePath.endsWith(".js") -> "application/javascript; charset=utf-8"
            resourcePath.endsWith(".css") -> "text/css; charset=utf-8"
            else -> "application/octet-stream"
        }
        write(exchange, 200, contentType, bytes)
    }

    private fun write(exchange: HttpExchange, status: Int, contentType: String, bytes: ByteArray) {
        exchange.use {
            it.responseHeaders.add("Content-Type", contentType)
            it.sendResponseHeaders(status, bytes.size.toLong())
            it.responseBody.use { out -> out.write(bytes) }
        }
    }
}
