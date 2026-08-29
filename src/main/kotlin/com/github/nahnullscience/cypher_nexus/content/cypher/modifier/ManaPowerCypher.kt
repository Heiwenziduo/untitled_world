package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.FirstTickHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.GeneralOnHitHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.forEachEntityRayCast
import com.github.nahnullscience.cypher_nexus.utility.isServerSide
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.golem.IronGolem
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.arrow.Arrow
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.HitResult.Type
import kotlin.math.floor
import kotlin.math.max

class ManaPowerCypher(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute) {
    override val resource = CypherNexus.modResource("mana_power")
    override fun modifyShotState(
        helper: InvokingHelper,
        shotState: ShotState,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        isCopy: Boolean
    ) {
        super.modifyShotState(helper, shotState, data, paras, isCopy)
        if (data.manaCurrent > 51f) {
            val delta = data.manaCurrent - 50f
            val boost = floor(delta / 40).toDouble()
            shotState.accessor.addRaw(CypherAttributes.DAMAGE, AttributeOperator.ADD, boost)
            data.manaCurrent = 50f
        }
    }
}
