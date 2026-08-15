package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.IWandData
import com.github.nahnullscience.cypher_nexus.utility.linear_space.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

/**
 * Any Item/Entity implemented the interface here should be able to conduct the power of cyphers through #tryConduct
 * */
sealed interface IWandLike <DataProvider : Any> {
    fun getWandData(dataProvider: DataProvider): IWandData
    fun getInvokingRecipe(dataProvider: DataProvider): ArrayOfCyphers
    fun getWandState(level: Level, invoker: Entity, dataProvider: DataProvider): HelperDataBundle
    fun tryInvoke(level: Level, invoker: Entity, coordinate: CoordinateDefinition, dataProvider: DataProvider): InvokingState
}