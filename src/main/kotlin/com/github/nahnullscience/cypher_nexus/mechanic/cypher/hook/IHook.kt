package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

/**
 * an interface marker that indicate it's a `CypherHook`
 *
 * hooks starts with `Server` or `Client` run on respective sides, otherwise run on both sides.
 *
 * parameters ` index: Int, count: Int, level: Level ` are shared by all hooks
 *
 * make sure that none of the `BEHAVIOR` hook implementation modifies the shot-state,
 * which will cause every cypher-entity of the same shot-state be modified together and de-sync.
 * hooks should only modify the cypher-entity itself that preforms the hook
 *
 * */
interface IHook {
}