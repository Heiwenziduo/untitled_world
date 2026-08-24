package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

/**
 * can be found through [net.neoforged.neoforge.attachment.AttachmentHolder.getData]
 * on entities that can be `hit` by cyphers.
 * */
class CETargetStateTracker {

    private var xLeast: Int = 0
    private var yLeast: Int = 0
    private var zLeast: Int = 0
    private var xMost: Int = 0
    private var yMost: Int = 0
    private var zMost: Int = 0

    var sectionKeyMin: Long = 0L
        private set
    var sectionKeyMax: Long = 0L
        private set

    /**
     * false until the first [updateSectionKey] lands. guards two things:
     * 1. without it, [needGridPosUpdate] false-negatives on an entity's very first tick whenever
     *    its coarse grid coords happen to equal the zeroed defaults — it would silently never
     *    register.
     * 2. tells the manager there's no previous section range to retract the entity from yet.
     * */
    var isTracked: Boolean = false
        private set

    fun needGridPosUpdate(xl: Int, yl: Int, zl: Int, xm: Int, ym: Int, zm: Int): Boolean {
        val gxl = xl shr 2
        val gyl = yl shr 2
        val gzl = zl shr 2
        val gxm = xm shr 2
        val gym = ym shr 2
        val gzm = zm shr 2
        val changed = !isTracked ||
                gxl != xLeast || gyl != yLeast || gzl != zLeast ||
                gxm != xMost || gym != yMost || gzm != zMost
        if (changed) { xLeast = gxl; yLeast = gyl; zLeast = gzl; xMost = gxm; yMost = gym; zMost = gzm }
        return changed
    }

    fun updateSectionKey(min: Long, max: Long) {
        sectionKeyMin = min
        sectionKeyMax = max
        isTracked = true
    }

    fun updateDimension() {

    }
}
