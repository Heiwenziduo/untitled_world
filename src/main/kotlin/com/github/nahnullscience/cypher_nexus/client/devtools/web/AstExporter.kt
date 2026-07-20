package com.github.nahnullscience.cypher_nexus.client.devtools.web

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.resources.Identifier

/**
 * Bridges the game-side registry / InvokingHelper world and the plain `AstModels` used by the web UI.
 *
 * IMPORTANT: this only works because InvokingHelper's `invoker: Entity?` is nullable and none of the
 * built-in cyphers dereference it unconditionally - that's the "decoupled from Minecraft" design the
 * mod already has. If a third-party cypher *does* require a real entity/level, calling it through here
 * with `invoker = null` will throw; that's a signal for that cypher's author, not a bug in this file.
 */
object AstExporter {

    // NOTE: `Identifier.parse(...)` may or may not exist depending on MC version - this only
    // relies on `fromNamespaceAndPath`, which the mod already uses elsewhere (see CypherNexus.modResource).
    private fun parseId(raw: String): Identifier {
        val i = raw.indexOf(':')
        return if (i < 0) Identifier.fromNamespaceAndPath("cypher_nexus", raw)
        else Identifier.fromNamespaceAndPath(raw.substring(0, i), raw.substring(i + 1))
    }

    private fun AbstractCypher.toRef(): AstCypherRef = AstCypherRef(
        id = "${resource.namespace}:${resource.path}",
        // swap for a real I18n.get(...) lookup client-side once you care about localized labels
        label = resource.path,
        category = category.value().resource.path,
        categoryColor = category.value().color,
        manaDrain = manaDrain,
        draw = draw,
        delay = delay,
        recharge = recharge,
    )

    /** every cypher currently in the registry - includes anything other mods added to it */
    fun palette(): List<AstPaletteEntry> =
        Cyphers.REGISTRY.map { cy -> AstPaletteEntry(cy.toRef(), cy.hide) }

    /**
     * Compile a list of cypher ids exactly the way a wand would fire them: one initial draw,
     * effectively unlimited mana (so the whole chain resolves instead of stopping on a mana check).
     */
    fun buildAst(cypherIds: List<String>): AstChunk {
        val cyphers = if (cypherIds.isEmpty()) listOf(EmptyCypher)
            else cypherIds.map { raw -> Cyphers.getCypher(parseId(raw)) ?: EmptyCypher }
        val aoc = ArrayOfCyphers(cyphers)

        val data = HelperDataBundle(
            manaCurrent = Float.MAX_VALUE,
            draw = 1,
            delay = 0,
            recharge = 0,
        )
        val helper = InvokingHelper(aoc, data, invoker = null, tracer = CallTreeTracer())
        helper.processSync()
        return helper.shotRoot.toAstChunk()
    }

    private fun ShotStateChunk.toAstChunk(): AstChunk {
        compute() // populate delay/recharge + computedOperationMap before we read them

        val contributors = ccMap.getMap()
            .filterKeys { it.isNotEmpty() && it !is AbstractProjectileCypher<*> }
            .map { (cy, count) -> AstContribution(cy.toRef(), count) }

        val edges = projectilesView.map { node ->
            AstProjectileEdge(
                cypher = node.instance.toRef(),
                trigger = node.trigger.name,
                payload = node.payload?.toAstChunk(),
            )
        }

        return AstChunk(delay = delay, recharge = recharge, contributors = contributors, projectiles = edges)
    }
}
