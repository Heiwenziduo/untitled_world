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
 * Ships zero frontend of its own on purpose - `/` just redirects to the hosted WebTinker
 * [WEB_TINKER_URL], passing this server's address along
 * so the page knows where to fetch from. Everything under `/api` is plain JSON.
 *
 * Start this from a CLIENT-only entrypoint (see WebServiceManager.kt / ClientSetup.kt) - it
 * reads the cypher registry and client-synced data maps, so it has no business existing on a
 * dedicated server, and it works identically in singleplayer or connected to someone else's
 * server since everything it reads is already synced to this client.
 */
object DevToolsServer {
    private const val PORT = 25599

    // self-host WebTinker on GitHub Pages
    private const val WEB_TINKER_URL = "https://nahnullscience.github.io/cypher-nexus-web-tinker/"

    // who's allowed to call /api/* from a browser. the hosted WebTinker + its local dev server.
    // deliberately an allowlist, not "*" - these endpoints are read/compute-only (no game-state
    // mutation), but there's no reason to let an arbitrary open tab poke a running instance either.
    private val ALLOWED_ORIGINS = setOf(
        "https://nahnullscience.github.io",
        "http://localhost:5173",
    )

    private val gson = Gson()
    private var server: HttpServer? = null

    val ip get() = server?.address
    val url: String? get() = if (server != null) "http://127.0.0.1:$PORT/" else null

    fun start() {
        if (server != null) return

        val http = HttpServer.create(InetSocketAddress("127.0.0.1", PORT), 0)
        http.executor = Executors.newFixedThreadPool(2)

        jsonRoute(http, "/api/meta") { _ ->
            gson.toJson(AstExporter.meta())
        }

        jsonRoute(http, "/api/palette") { _ ->
            gson.toJson(AstExporter.palette())
        }

        jsonRoute(http, "/api/ast") { ids ->
            gson.toJson(AstExporter.buildAst(ids))
        }

        jsonRoute(http, "/api/call-chain") { ids ->
            gson.toJson(AstExporter.buildCallChain(ids))
        }

        http.createContext("/") { exchange -> redirectToWebTinker(exchange) }

        http.start()
        server = http
        CypherNexus.LOGGER.info("cypher-nexus devtools listening on http://127.0.0.1:{}/", PORT)
    }

    fun stop() {
        server?.stop(0)
        server = null
    }

    // ------------------------------------------------------------------------------------------

    /**
     * registers a JSON API route with CORS + preflight handled once, in one place, instead of
     * copy-pasted per endpoint. `body` receives the parsed cypher-id list from a POST body (empty
     * list for a plain GET, e.g. /api/palette and /api/meta don't need one) and returns raw JSON.
     */
    private fun jsonRoute(http: HttpServer, path: String, body: (ids: List<String>) -> String) {
        http.createContext(path) { exchange ->
            if (!applyCors(exchange)) return@createContext // preflight handled, nothing more to do

            try {
                val ids = if (exchange.requestMethod == "POST") {
                    val raw = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8)
                    gson.fromJson(raw, Array<String>::class.java)?.toList() ?: emptyList()
                } else emptyList()

                write(exchange, 200, "application/json; charset=utf-8", body(ids).toByteArray(StandardCharsets.UTF_8))
            } catch (e: Exception) {
                CypherNexus.LOGGER.error("devtools request to {} failed", exchange.requestURI, e)
                val msg = (e.message ?: e::class.simpleName ?: "error").toByteArray(StandardCharsets.UTF_8)
                write(exchange, 500, "text/plain; charset=utf-8", msg)
            }
        }
    }

    /**
     * sets CORS headers for allowed origins and answers preflight OPTIONS requests directly.
     * @return false if the exchange was already fully handled here (preflight) - caller should
     * return immediately without touching the exchange again.
     */
    private fun applyCors(exchange: HttpExchange): Boolean {
        val origin = exchange.requestHeaders.getFirst("Origin")
        if (origin != null && origin in ALLOWED_ORIGINS) {
            exchange.responseHeaders.add("Access-Control-Allow-Origin", origin)
            exchange.responseHeaders.add("Vary", "Origin")
        }

        if (exchange.requestMethod == "OPTIONS") {
            exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
            return false
        }
        return true
    }

    private fun redirectToWebTinker(exchange: HttpExchange) {
        exchange.use {
            val target = "$WEB_TINKER_URL?api=http://127.0.0.1:$PORT"
            it.responseHeaders.add("Location", target)
            it.sendResponseHeaders(302, -1)
        }
    }

    private fun write(exchange: HttpExchange, status: Int, contentType: String, bytes: ByteArray) {
        exchange.use {
            it.responseHeaders.add("Content-Type", contentType)
            it.sendResponseHeaders(status, bytes.size.toLong())
            it.responseBody.use { out -> out.write(bytes) }
        }
    }
}
