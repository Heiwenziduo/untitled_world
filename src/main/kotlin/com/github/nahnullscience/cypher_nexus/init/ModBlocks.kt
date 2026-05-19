package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.block.CypherIndexBlock
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

/* @doc
 * The rule of thumb here is that if you have a finite and reasonably small amount of states (= a few hundred states at most),
 * use blockstates, and if you have an infinite or near-infinite amount of states, use a block entity.
 * */
object ModBlocks {
    val DEFERRED_REGISTER: DeferredRegister.Blocks = DeferredRegister.createBlocks(CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    /*
     * DeferredHolder<R, T extends R> is a subclass of Supplier<T>.
     * "by" make it automatically go inside the container and get the Block itself instead of a DeferredHolder<Block, T extends Block>.
     * */
    val CYPHER_INDEX_BLOCK: Block by DEFERRED_REGISTER.register("cypher_index") {
        registryName ->
        CypherIndexBlock()
    }
}