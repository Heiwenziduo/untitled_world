package com.github.nahnullscience.cypher_nexus.client.gui.components

class RectBasics() : IScreenRect {
    companion object {
        val ResizeDefault: (Int) -> Int = { v -> v }
    }
    override var x: Int = 0
    override var y: Int = 0
    override var w: Int = 0
    override var h: Int = 0

    private var xResize = ResizeDefault
    private var yResize = ResizeDefault
    private var wResize = ResizeDefault
    private var hResize = ResizeDefault

    override fun resize(screenX: Int, screenY: Int) {
        x = xResize(screenX)
        y = yResize(screenY)
        w = wResize(screenX)
        h = hResize(screenY)
    }

    override fun setResizeFunction(
        x: (screenX: Int) -> Int,
        y: (screenY: Int) -> Int,
        w: (screenX: Int) -> Int,
        h: (screenY: Int) -> Int
    ) {
        xResize = x
        yResize = y
        wResize = w
        hResize = h
    }
}