package com.github.nahnullscience.cypher_nexus.client.devtools.web

/**
 * Plain data classes describing a compiled cypher chain.
 * Kept free of any Minecraft/registry types on purpose: this is exactly what gets
 * Gson-serialized and sent to the browser, so it should only ever contain primitives
 * and other plain data classes.
 *
 * DEVTOOLS_API_VERSION is the actual contract between this file and the frontend's
 * src/types/ast.ts. Bump it whenever a shape here changes in a way the frontend needs
 * to know about (renamed/removed field, changed meaning of an existing field - adding
 * a new optional-in-spirit field is usually fine to skip). See DevToolsServer's
 * /api/meta and the frontend's checkApiMeta() for how this gets enforced at runtime.
 */
const val DEVTOOLS_API_VERSION = 2

/** GET /api/meta - the frontend checks this before trusting anything else it gets back */
data class AstApiMeta(
    val apiVersion: Int,
    val modVersion: String,
    val cypherCount: Int,
)

/** a single cypher, flattened down to whatever the web UI needs to draw it */
data class AstCypherRef(
    val id: String,           // "modid:path", stable key used to round-trip a deck back to the server
    val label: String,        // display name (defaults to the path, see AstExporter)
    val category: String,
    val categoryColor: Int,   // ARGB, same value used by CypherIndexScreen
    val manaDrain: Float,
    val draw: Int,
    val delay: Int,
    val recharge: Int,
)

/** one entry in the palette panel */
data class AstPaletteEntry(
    val cypher: AstCypherRef,
    val hidden: Boolean,
)

/**
 * one ShotStateChunk, i.e. everything that happens at a given "trigger depth".
 * `contributors` are the non-projectile cyphers (modifiers, requirements, etc.) that
 * shaped this chunk; `projectiles` are the branches that fan out from it.
 */
data class AstChunk(
    val delay: Int,
    val recharge: Int,
    val contributors: List<AstContribution>,
    val projectiles: List<AstProjectileEdge>,
)

data class AstContribution(
    val cypher: AstCypherRef,
    val count: Int,
)

/**
 * an edge from a chunk to the projectile it fires, and (if any) the payload chunk it triggers.
 * `count`: untriggered projectiles are stored on ShotStateChunk as a cypher->count map (the
 * common case - e.g. 8x snowball has no reason to carry 8 separate nodes), so this can be >1.
 * Triggered projectiles (payload != null) always have count == 1: each one owns a distinct
 * subtree, so there's nothing to collapse them together on.
 */
data class AstProjectileEdge(
    val cypher: AstCypherRef,
    val trigger: String,
    val count: Int,
    val payload: AstChunk?,
)

/**
 * the causal "call chain" - who invoked whom, in order, including copies (Proteus, DivideBy,
 * the Greek letters, CypherDuplication) that never show up in the compiled AstChunk at all.
 * Built from CallTreeTracer.Node, see AstExporter.buildCallChain().
 */
data class AstCallNode(
    val cypher: AstCypherRef,
    val isCopy: Boolean,
    val children: List<AstCallNode>,
)
