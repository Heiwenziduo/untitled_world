package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

interface InputModule {
    /**
     * whether the original event should be canceled, if canceled, further process from other source will not perform
     * */
    val takeoverInput: Boolean

}