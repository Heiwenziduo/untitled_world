package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity


// check Entity.RemovalReason for more info
// here only for cypher-projectile usage
enum class DiscardReason {
    /** reach its time limit (e.g. naturally expire) */
    EXPIRE,
    /** through a collapse with entity */
    HIT_ENTITY,
    /** through a collapse with block */
    HIT_BLOCK,
    /**  */
    TRANSFORMED,
    /**  */
    CONSUMED,

    /** by some special reason */
    ERASE,
}