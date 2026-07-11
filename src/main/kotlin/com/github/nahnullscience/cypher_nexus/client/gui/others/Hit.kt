package com.github.nahnullscience.cypher_nexus.client.gui.others

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.client.gui.navigation.ScreenRectangle

data class Hit(val cypher: AbstractCypher, val index: Int, val rect: ScreenRectangle) {
    override fun equals(other: Any?): Boolean {
        if (other !is Hit) return false
        return cypher === other.cypher && index == other.index && rect == other.rect
    }

    override fun hashCode(): Int {
        var result = cypher.hashCode()
        result = 31 * result + rect.hashCode()
        return result
    }
}
