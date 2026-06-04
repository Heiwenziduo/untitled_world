package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.server.level.ServerPlayer

// prevent from instantiating
object CypherData {

    private val _enabledList: List<AbstractCypher> by lazy {
        val list = Cyphers.REGISTRY.toList().filter { true } // TODO server-config?
        println("cypher _enabledList init: $list")
        list
    }
    private val _categoryMap: Map<CypherCategory, List<AbstractCypher>> by lazy {
        val map = CypherUtility.sortCyphersByCategory(_enabledList)
        println("cypher _categoryMap init: $map")
        map
    }
    private val _cyphersUnhide: List<AbstractCypher> by lazy {
        _enabledList.filter { !it.hide }
    }

    val cyphersEnabled // guess these should not be called on logical client
        get() = _enabledList

    val cypherCategoryMap // _cypherMap will not compute (or take memory) if this is not called
        get() = _categoryMap

    val cyphersUnhide
        get() = _cyphersUnhide

    fun cyphersPlayerUnlocked (player: ServerPlayer): List<AbstractCypher> {
        return _cyphersUnhide
    }

    // called during common-setup, can access registry list safely
    // Q: what about other mods modified the registry?
    // A: access it during game play only, this timing make sure all registry is settled

}