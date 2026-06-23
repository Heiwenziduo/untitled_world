package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import org.apache.logging.log4j.Level
import java.util.*

class MapOfModules(
    val instance: ItemWandInstance
) : EnumMap<ModuleCategory, IWandModule>(ModuleCategory::class.java) {

    private var init = false

    override fun put(key: ModuleCategory, module: IWandModule): IWandModule? {
        if (key != module.category) {
            CypherNexus.debugWand(Level.ERROR) { "category [$key] [$module] mismatch!" }
            return null
        }
        init = false
        return super.put(key, module)
    }

    fun finalizeInit() {
        init = true
        CypherNexus.debugWand { "$instance computed modules, current module: $this" }
    }

    override fun clear() {
        init = false
        super.clear()
    }

    override fun toString() = super.toString()

}