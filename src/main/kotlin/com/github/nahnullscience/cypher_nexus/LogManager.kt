package com.github.nahnullscience.cypher_nexus

import com.github.nahnullscience.cypher_nexus.CypherNexus.MOD_ID
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.reflect.KProperty

object LogManager {
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    inline fun debugCypher(supplier: () -> String) {
        if (true)
            LOGGER.debug(supplier.invoke())
    }
    inline fun debug(supplier: () -> String) {
        LOGGER.debug(supplier.invoke())
    }
    inline fun info(supplier: () -> String) {
        LOGGER.info(supplier.invoke())
    }
    inline fun warn(supplier: () -> String) {
        LOGGER.warn(supplier.invoke())
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        return LOGGER
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Logger) {

    }
}