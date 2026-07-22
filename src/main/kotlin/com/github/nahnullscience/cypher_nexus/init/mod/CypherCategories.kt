package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * use to create pools, to display, or to roll for some sake
 * */
object CypherCategories {
    val RESOURCE_KEY: ResourceKey<Registry<CypherCategory>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher/category"))
    val REGISTRY: Registry<CypherCategory> = RegistryBuilder(RESOURCE_KEY).sync(true).create()

    val DEFERRED_REGISTER: DeferredRegister<CypherCategory> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun registerCategory(category: CypherCategory): Holder<CypherCategory> =
        DEFERRED_REGISTER.register(category.resource.path) { -> category }

    fun getCategory(resource: Identifier): CypherCategory? = REGISTRY.getValue(resource)

    val PROJECTILE_RESOURCE = CypherNexus.modResource("projectile")
    val STATIC_PROJECTILE_RESOURCE = CypherNexus.modResource("static_projectile")
    val MODIFIER_RESOURCE = CypherNexus.modResource("modifier")
    val UTILITY_RESOURCE = CypherNexus.modResource("utility")
    val OTHER_RESOURCE = CypherNexus.modResource("other")
    val WAND_MODULE_RESOURCE = CypherNexus.modResource("wand_module")


    val PROJECTILE = registerCategory(CypherCategory(PROJECTILE_RESOURCE, 0xFFBA1650.toInt()))
    val STATIC_PROJECTILE = registerCategory(CypherCategory(STATIC_PROJECTILE_RESOURCE, 0xFF228B22.toInt()))
    val MODIFIER = registerCategory(CypherCategory(MODIFIER_RESOURCE, 0xFF5F8CD6.toInt()))
    val UTILITY = registerCategory(CypherCategory(UTILITY_RESOURCE, 0xFF8D25E1.toInt()))
    val OTHER = registerCategory(CypherCategory(OTHER_RESOURCE, 0xFFF9C66D.toInt()))
    val WAND_MODULE = registerCategory(CypherCategory(WAND_MODULE_RESOURCE, 0xFF85858E.toInt()))
}