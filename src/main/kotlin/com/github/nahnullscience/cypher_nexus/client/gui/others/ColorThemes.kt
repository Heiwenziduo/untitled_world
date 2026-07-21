package com.github.nahnullscience.cypher_nexus.client.gui.others

interface ColorTheme {
    val primary: Int
    val secondary: Int
    val light: Int
    val middle: Int
    val dark: Int
}

object Classic : ColorTheme {
    override val primary: Int = GUIConstants.BLACK
    override val secondary: Int = GUIConstants.DARK
    override val light: Int = 0xCC333333.toInt()
    override val middle: Int = 0xCC333333.toInt()
    override val dark: Int = 0xCC333333.toInt()
}