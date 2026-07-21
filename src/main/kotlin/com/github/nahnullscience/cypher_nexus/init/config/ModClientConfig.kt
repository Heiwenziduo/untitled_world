package com.github.nahnullscience.cypher_nexus.init.config

import com.github.nahnullscience.cypher_nexus.CypherNexus.MOD_ID
import net.neoforged.neoforge.common.ModConfigSpec
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue
import net.neoforged.neoforge.common.ModConfigSpec.Builder
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
    }

    val bouncePointsInterpolate: BooleanValue = builder
        .comment("whether show bounce trajectory instead of moving to destination directly")
        .translation("$MOD_ID.config.bounce_points_interpolate")
        .define("bounce_points_interpolate", true)
    val circularInterpolate: BooleanValue = builder
        .comment("whether interpolate more smoothly when projectile goes a circular way // TODO")
        .translation("$MOD_ID.config.circular_interpolate")
        .define("circular_interpolate", true)
}