package com.github.nahnullscience.cypher_nexus.init.config

import com.github.nahnullscience.cypher_nexus.CypherNexus.MOD_ID
import net.neoforged.neoforge.common.ModConfigSpec
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue
import net.neoforged.neoforge.common.ModConfigSpec.Builder
import net.neoforged.neoforge.common.ModConfigSpec.IntValue
import org.apache.commons.lang3.tuple.Pair

class ModClientConfig private constructor (builder: Builder) {
    companion object {
        val CONFIG: ModClientConfig
        val CONFIG_SPEC: ModConfigSpec

        init {
            var pair: Pair<ModClientConfig, ModConfigSpec> = Builder().configure(::ModClientConfig)
            CONFIG = pair.left
            CONFIG_SPEC = pair.right
        }

        private fun key(k: String): String = "$MOD_ID.config.$k"
    }

//    val engineMaxFPS: IntValue = builder
//        .comment("")
//        .translation(key("engine_max_fps"))
//        .defineInRange("engine_max_fps", 60, 20, Int.MAX_VALUE)

    val maxTrailParticleCount: IntValue = builder
        .comment(" the max number of cypher-entity trail particles")
        .translation(key("max_trail_particle_count"))
        .defineInRange("max_trail_particle_count", 12000, 0, Int.MAX_VALUE)

    val bouncePointsInterpolate: BooleanValue = builder
        .comment(" whether show bounce trajectory instead of moving to destination directly")
        .translation(key("bounce_points_interpolate"))
        .define("bounce_points_interpolate", true)
//    val circularInterpolate: BooleanValue = builder
//        .comment(" whether interpolate more smoothly when projectile goes a circular way")
//        .translation(key("circular_interpolate"))
//        .define("circular_interpolate", true)

}