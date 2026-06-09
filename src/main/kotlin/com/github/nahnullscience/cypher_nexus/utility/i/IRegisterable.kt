package com.github.nahnullscience.cypher_nexus.utility.i

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

/** holds custom register item utilities */
interface IRegisterable {
    val resource: Identifier
    fun registryName(): String = resource.path
    fun translation(): MutableComponent = Component.empty()
}