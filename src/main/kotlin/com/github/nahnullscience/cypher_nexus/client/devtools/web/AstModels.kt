package com.github.nahnullscience.cypher_nexus.client.devtools.web

/**
 * Plain data classes describing a compiled cypher chain.
 * Kept free of any Minecraft/registry types on purpose: this is exactly what gets
 * Gson-serialized and sent to the browser, so it should only ever contain primitives
 * and other plain data classes.
 */

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

/** an edge from a chunk to the projectile it fires, and (if any) the payload chunk it triggers */
data class AstProjectileEdge(
    val cypher: AstCypherRef,
    val trigger: String,
    val payload: AstChunk?,
)
