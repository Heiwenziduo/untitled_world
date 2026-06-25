package com.github.nahnullscience.cypher_nexus.mechanic.event

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance

/**
 *
 * */
interface IWandInstanceEvent {
    val instance: ItemWandInstance?
}