package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import java.util.function.Supplier

abstract class AbstractProjectileCypher <CE> (
    defaultAttribute: Builder.() -> Builder
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
        shotState: ShotState,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyShotState(helper, shotState, data, paras, isCopy)

        if (innateTrigger != TriggerType.NONE && paras.drawEnabled && draw > 0) {
            // create substate conditionally // this will save plenty of memory when copy thousands of trigger-cypher with draw-disabled
            // TODO when pierce, collide trigger is infinite, but flag hasn't been ready at this stage
            for (i in 0 until projectileCount) {
                val subState = addProjectileWithTrigger(shotState, innateTrigger, innateTriggerCharge)
                handleDraws(helper, subState, data, paras)
            }
        } else {
            // if no trigger or no payload
            addProjectileAlone(shotState)
            handleDraws(helper, shotState, data, paras)
        }
    }

    open fun addProjectileAlone(shotState: ShotState, count: Int = projectileCount) {
        shotState.addProjectileNode(this, count)
    }

    /**
     * @return the newly created payload-state
     * */
    open fun addProjectileWithTrigger(
        old: ShotState,
        trigger: TriggerType,
        charge: Int = TRIGGER_CHARGE_MAX
    ): ShotState = ShotState(charge).also { old.addProjectileNode(this, it, trigger) }


    fun getAttrOrDefault(attr: CypherAttribute): Double = dataMap().projectile.getAttributeOrDefault(attr)
//    fun getAttrOrDefault(holder: Holder<CypherAttribute>) = getAttrOrDefault(holder.value())

    fun hasAttr(attr: CypherAttribute): Boolean = dataMap().projectile.hasAttribute(attr)
//    fun hasAttr(holder: Holder<CypherAttribute>): Boolean = hasAttr(holder.value())
}