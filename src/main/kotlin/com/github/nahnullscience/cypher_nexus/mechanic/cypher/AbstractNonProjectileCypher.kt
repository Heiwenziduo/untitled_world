package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import net.minecraft.core.Holder

abstract class AbstractNonProjectileCypher: AbstractCypher() {
    /**
     * add Attributes as its ADD value
     * */
    final override fun addAttribute(holder: Holder<CypherAttribute>, add: Double): AbstractCypher {
        return addAttribute(holder, CypherAttributeOperation.ADD, add)
    }

    // Do not add BASE value on NonProjectileCypher, which will not count
}