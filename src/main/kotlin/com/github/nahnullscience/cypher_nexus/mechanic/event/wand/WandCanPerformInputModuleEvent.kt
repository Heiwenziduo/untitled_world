package com.github.nahnullscience.cypher_nexus.mechanic.event.wand

import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.ICancellableEvent
import net.neoforged.neoforge.event.entity.living.LivingEvent

/**
 * on both sides, fired when some living try to perform a module.
 * cancelable, if canceled, the module won't function (its state won't change either).
 *
 * Note: the cancellation should be consistent on both sides
 * */
class WandCanPerformInputModuleEvent <Module : AbstractWandModule> (
    entity: LivingEntity,
    override val type: WandModuleType<Module>,
) : LivingEvent(entity), IWandModuleEvent, ICancellableEvent {

}