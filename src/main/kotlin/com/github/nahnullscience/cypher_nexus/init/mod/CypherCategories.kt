package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
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

    fun getCategory(resource: ResourceLocation): CypherCategory? = REGISTRY.get(resource)

    val PROJECTILE_RESOURCE = CypherNexus.modResource("projectile")
    val STATIC_PROJECTILE_RESOURCE = CypherNexus.modResource("static_projectile")
    val MODIFIER_RESOURCE = CypherNexus.modResource("modifier")
    val PASSIVE_RESOURCE = CypherNexus.modResource("passive")
//    val WAND_MODULE = CypherNexus.modResource("wand_module")
    val UTILITY_RESOURCE = CypherNexus.modResource("utility")
    val OTHER_RESOURCE = CypherNexus.modResource("other")


    val PROJECTILE = registerCategory(CypherCategory(PROJECTILE_RESOURCE, 0xFFBA1650.toInt()))
    val STATIC_PROJECTILE = registerCategory(CypherCategory(STATIC_PROJECTILE_RESOURCE, 0xFF228B22.toInt()))
    val MODIFIER = registerCategory(CypherCategory(MODIFIER_RESOURCE, 0xFF5F8CD6.toInt()))
    val PASSIVE = registerCategory(CypherCategory(PASSIVE_RESOURCE, 0xFF056C5C.toInt()))
    val UTILITY = registerCategory(CypherCategory(UTILITY_RESOURCE, 0xFF8D25E1.toInt()))
    val OTHER = registerCategory(CypherCategory(OTHER_RESOURCE, 0xFFF2EC8D.toInt()))
}