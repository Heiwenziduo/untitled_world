package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags.Companion.containsFlag
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import net.minecraft.core.Holder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import java.util.function.Supplier

abstract class AbstractProjectileCypher <CE> (
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = NONE_ATTR
) : AbstractCypher(defaultAttribute) where CE : Entity, CE : ICypherEntity {

    companion object {
        const val TRIGGER_CHARGE_MAX = 299_792_458 // large but finite, so decrements are traceable
    }

    abstract val projectileType: Supplier<out EntityType<out CE>>
    open val projectileCount: Int = 1

    protected open val innateTrigger: TriggerType = TriggerType.NONE
    protected open val innateTriggerCharge: Int = 1

//    init {
//        require(innateTriggerCharge > 0)
//        require(projectileCount > 0)
//    }

    override fun triggerInterplay() = true

    override fun invoke(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyShotState(helper, shotState, data, paras, isCopy)

        if (innateTrigger != TriggerType.NONE && paras.drawEnabled && draw > 0) {
            // create substate conditionally // this will save plenty of memory when copy thousands of trigger-cypher with draw-disabled
            // TODO when pierce, collide trigger is infinite, but flag hasn't been ready at this stage
            val subState = ShotStateChunk(innateTriggerCharge) // or innateTriggerCharge * projectileCount ?
            for (i in 0 until projectileCount) {
                shotState.addProjectileNode(this, subState, innateTrigger)
            }
            handleDraws(helper, shotState, data, paras)
        } else {
            // if no trigger or no payload
            addProjectileAlone(shotState)
            handleDraws(helper, shotState, data, paras)
        }
    }

    open fun addProjectileAlone(shotState: ShotStateChunk, count: Int = projectileCount) {
        shotState.addProjectileNode(this, count)
    }

    /**
     * @return the newly created payload-state
     * */
    open fun addProjectileWithTrigger(
        old: ShotStateChunk,
        trigger: TriggerType,
        charge: Int = TRIGGER_CHARGE_MAX
    ): ShotStateChunk = ShotStateChunk(charge).also { old.addProjectileNode(this, it, trigger) }


    fun getAttrBaseOrDefault(holder: Holder<CypherAttribute>) = getAttrBaseOrDefault(holder.value())
    fun getAttrBaseOrDefault(attr: CypherAttribute): Double = attributes().projectile.getAttrOrDefault(attr)
    fun getAttrBaseOrNull(holder: Holder<CypherAttribute>) = getAttrBaseOrNull(holder.value())
    fun getAttrBaseOrNull(attr: CypherAttribute): Double? = attributes().projectile[attr]
}