package com.github.nahnullscience.cypher_nexus.client.devtools.web

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.resources.Identifier
import net.neoforged.fml.ModList

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

    private fun resolveDeck(cypherIds: List<String>): ArrayOfCyphers {
        val cyphers = if (cypherIds.isEmpty()) listOf(EmptyCypher)
            else cypherIds.map { raw -> Cyphers.getCypher(parseId(raw)) ?: EmptyCypher }
        return ArrayOfCyphers(cyphers)
    }

    private fun freshDataBundle() = HelperDataBundle(
        manaCurrent = Float.MAX_VALUE,
        draw = 1,
        delay = 0,
        recharge = 0,
    )

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

    private val modVersion: String by lazy {
        ModList.get().getModContainerById(CypherNexus.MOD_ID)
            .map { it.modInfo.version.toString() }
            .orElse("unknown")!!
    }

    fun meta(): AstApiMeta = AstApiMeta(
        apiVersion = DEVTOOLS_API_VERSION,
        modVersion = modVersion,
        cypherCount = Cyphers.REGISTRY.count(),
    )

    /**
     * Compile a list of cypher ids exactly the way a wand would fire them: one initial draw,
     * effectively unlimited mana (so the whole chain resolves instead of stopping on a mana check).
     * Returns the *compiled result* tree - final delay/recharge/attribute cumulation per chunk.
     * See [buildCallChain] for the causal "who invoked whom" tree instead.
     */
    fun buildAst(cypherIds: List<String>): AstChunk {
        val helper = InvokingHelper(resolveDeck(cypherIds), freshDataBundle(), invoker = null)
        helper.processSync()
        return helper.shotRoot.toAstChunk()
    }

    /**
     * Same compile, but captures the InvokingTracer call chain instead of reading the compiled
     * result - this is what actually shows copies (Proteus, DivideBy, the Greek letters,
     * CypherDuplication) as distinct calls, which the compiled AstChunk collapses away.
     */
    fun buildCallChain(cypherIds: List<String>): List<AstCallNode> {
        val tracer = CallTreeTracer()
        val helper = InvokingHelper(resolveDeck(cypherIds), freshDataBundle(), tracer = tracer, invoker = null)
        helper.processSync()
        return tracer.roots.map { it.toAstCallNode() }
    }

    private fun CallTreeTracer.Node.toAstCallNode(): AstCallNode = AstCallNode(
        cypher = cypher.toRef(),
        isCopy = isCopy,
        children = children.map { it.toAstCallNode() },
    )

    private fun ShotStateChunk.toAstChunk(): AstChunk {
        compute() // populate delay/recharge + attr2opMap before we read them

        val contributors = ccMap.getMap()
            .filterKeys { it.isNotEmpty() && it !is AbstractProjectileCypher<*> }
            .map { (cy, count) -> AstContribution(cy.toRef(), count) }

        // ShotStateChunk splits "fired with no trigger" (the common case, a cypher->count map)
        // from "fired with a trigger/payload" (a List<ProjectileNode>, one per firing since each
        // owns its own subtree) - both need to show up as edges, or most chains render empty.
        val simpleEdges = simpleProjectilesView.map { (cy, count) ->
            AstProjectileEdge(cypher = cy.toRef(), trigger = TriggerType.NONE.name, count = count, payload = null)
        }
        val triggeredEdges = triggeredProjectilesView.map { node ->
            AstProjectileEdge(
                cypher = node.instance.toRef(),
                trigger = node.trigger.name,
                count = 1,
                payload = node.payload?.toAstChunk(),
            )
        }

        return AstChunk(delay = delay, recharge = recharge, contributors = contributors, projectiles = simpleEdges + triggeredEdges)
    }
}
