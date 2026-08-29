//package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation
//
//import com.github.nahnullscience.cypher_nexus.CypherNexus
//import net.minecraft.world.entity.projectile.Projectile
//import net.minecraft.world.phys.Vec3
//import net.neoforged.bus.api.EventPriority
//import net.neoforged.bus.api.SubscribeEvent
//import net.neoforged.fml.common.EventBusSubscriber
//import net.neoforged.neoforge.event.tick.EntityTickEvent
//import java.text.DecimalFormat
//
//@EventBusSubscriber(modid = CypherNexus.MOD_ID)
//object Tmp {
//    private val df = DecimalFormat("#.####")
//    private fun Vec3.shortString() = "(${df.format(x)}, ${df.format(y)}, ${df.format(z)})"
//
//    @SubscribeEvent(priority = EventPriority.NORMAL)
//    private fun listenPosition(event: EntityTickEvent.Pre) {
//        val entity = event.entity
//        if (entity is Projectile || entity is ICypherEntity) {
////            CypherNexus.LOGGER.info(
////                "[{}] {} tick {} pos {}",
////                entity::class.simpleName,
////                entity.level().side(),
////                entity.tickCount,
////                entity.position().shortString()
////            )
////            if (entity is ICypherEntity) {
////                CypherNexus.LOGGER.info(
////                    "${entity.level().side()} " +
////                    "first-tick ${entity.firstTick} " +
////                    "speed ${entity.deltaMovement.shortString()} "
//////                    "friction${entity.getSpeedFactor()}" +
//////                    "gravity${entity.getGravityFactor()}"
////                )
////            }
//        }
//    }
//}