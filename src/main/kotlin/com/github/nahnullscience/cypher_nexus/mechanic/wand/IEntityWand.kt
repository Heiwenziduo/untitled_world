package com.github.nahnullscience.cypher_nexus.mechanic.wand

import net.minecraft.world.entity.Entity

// TODO
interface IEntityWand<IE> : IWandLike<IE> where IE : Entity {
}