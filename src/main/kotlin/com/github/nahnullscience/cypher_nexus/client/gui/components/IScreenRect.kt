package com.github.nahnullscience.cypher_nexus.client.gui.components

interface IScreenRect {
    val x: Int
    val y: Int
    val w: Int
    val h: Int

    val left:  Int get() = x
    val right: Int get() = x + w
    val top:   Int get() = y
    val bot:   Int get() = y + h

    fun resize(screenX: Int, screenY: Int)
    fun setResizeFunction(
        x: (screenX: Int) -> Int,
        y: (screenY: Int) -> Int,
        w: (screenX: Int) -> Int,
        h: (screenY: Int) -> Int
    )
}
