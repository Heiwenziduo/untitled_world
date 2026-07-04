package com.github.nahnullscience.cypher_nexus.init.data_driven

import com.github.nahnullscience.cypher_nexus.CypherNexus
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageEffects
import net.minecraft.world.damagesource.DamageScaling
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.damagesource.DeathMessageType


object ModDamageTypes {
    fun damageType(path: String): ResourceKey<DamageType> =
        ResourceKey.create(Registries.DAMAGE_TYPE, CypherNexus.modResource(path))

    val CYPHER_DEFAULT = damageType("cypher_default")

    // generate jsons
    fun registerDamageType(bootstrap: BootstrapContext<DamageType>) {
        bootstrap.register(CYPHER_DEFAULT, DamageType(
                CYPHER_DEFAULT.identifier().path,
                DamageScaling.NEVER,
                0.0f,
                DamageEffects.HURT,
                DeathMessageType.DEFAULT
            )
        )
        bootstrap
    }
}