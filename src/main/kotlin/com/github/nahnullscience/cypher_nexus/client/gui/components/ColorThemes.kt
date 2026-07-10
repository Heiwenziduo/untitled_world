package com.github.nahnullscience.cypher_nexus.client.gui.components

import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.BLACK
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.DARK

interface ColorTheme {
    val primary: Int
    val secondary: Int
    val light: Int
    val middle: Int
    val dark: Int
}

object Classic : ColorTheme {
    override val primary: Int = BLACK
    override val secondary: Int = DARK
    override val light: Int = 0xCC333333.toInt()
    override val middle: Int = 0xCC333333.toInt()
    override val dark: Int = 0xCC333333.toInt()
}