package com.github.nahnullscience.cypher_nexus.content.entity.projectiles

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.FIREWORK_ROCKET
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.RANDOM_FIREWORK_ROCKET
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.utility.component1
import com.github.nahnullscience.cypher_nexus.utility.component2
import com.github.nahnullscience.cypher_nexus.utility.component3
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.FireworkExplosion.Shape
import net.minecraft.world.level.Level

open class FireworkRocket(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = FIREWORK_ROCKET

    override fun doEntitySetup() {
        this.level().playSound(
            null,
            this.x,
            this.y,
            this.z,
            SoundEvents.FIREWORK_ROCKET_LAUNCH,
            SoundSource.PLAYERS,
            3.0f,
            1.0f
        )
    }

    // TODO handle innate explosion & deal with bounce
    override fun discardVisualEffect() {
        if (level().isClientSide) {
            val (xv, yv, zv) = deltaMovement
            level().createFireworks(x, y, z, xv, yv, zv, fireworkExplosions())
        }
    }

    open fun fireworkExplosions(): List<FireworkExplosion> {
        val hueColor = hue?.let { IntList.of(it) } ?: IntList.of(DyeColor.WHITE.fireworkColor)
        return listOf(
            FireworkExplosion(
                Shape.BURST,
                hueColor,
                IntList.of( DyeColor.WHITE.fireworkColor),
                false,
                false
            )
        )
    }

    class RandomFireRocket(
        entityType: EntityType<out AbstractDedicatedCypherProjectile>,
        level: Level
    ) : FireworkRocket(entityType, level) {
        override val cypherHolder = RANDOM_FIREWORK_ROCKET
        override fun fireworkExplosions(): List<FireworkExplosion> {
            val shape = random.nextInt(5)
            val colors = IntArrayList(4)
            var i = 0
            do {
                val color = DyeColor.byId(random.nextInt(15)).fireworkColor
                colors.add(color)
            } while (i++ < 3 && random.nextBoolean())
            val fade = hue?.let { IntList.of(it) } ?: IntList.of()
            return listOf(
                FireworkExplosion(
                    Shape.byId(shape),
                    colors,
                    fade,
                    random.nextBoolean(),
                    random.nextBoolean()
                )
            )
        }
    }
}