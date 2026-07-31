package com.github.nahnullscience.cypher_nexus.mechanic.event.wand

import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IInputModule
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.ICancellableEvent
import net.neoforged.neoforge.event.entity.living.LivingEvent

/**
 * on both sides, fired when performing state change, e.g. start or end.
 * by default, this is used to trigger [IInputModule.onHoldingStart] and [IInputModule.onHoldingStop].
 * cancelable, if canceled, the logic aforesaid won't apply.
 *
 * */
sealed class WandPerformingStateChangeEvent (
    entity: LivingEntity,
    override val type: WandModuleType<*>,
) : LivingEvent(entity), IWandModuleEvent, ICancellableEvent {

    /**
     *
     * */
    class Start(
        entity: LivingEntity,
        type: WandModuleType<*>,
    ) : WandPerformingStateChangeEvent(entity, type) {}

    /**
     *
     * */
    class End(
        entity: LivingEntity,
        type: WandModuleType<*>,
    ) : WandPerformingStateChangeEvent(entity, type) {}
}