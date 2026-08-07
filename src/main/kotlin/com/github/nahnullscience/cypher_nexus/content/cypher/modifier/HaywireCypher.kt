package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeAbortReleaseHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeAbortReleaseHook.ReleaseAbort
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk.ShotStateAccessor
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity

class HaywireCypher(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), ServerInvokeAbortReleaseHook {
    companion object {
        const val PROBABILITY = 0.125
    }

    override val resource = CypherNexus.modResource("haywire")

    override fun abortReleaseServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateAccessor
    ): ReleaseAbort {
        owner?.random?.let { random ->
            if (random.nextDouble() <= PROBABILITY * count) {
                level.playSound(
                    null,
                    owner.x,
                    owner.y,
                    owner.z,
                    SoundEvents.DISPENSER_FAIL,
                    SoundSource.PLAYERS
                )
                return ReleaseAbort.ABORT
            }
        }
        return ReleaseAbort.CONTINUE
    }
}