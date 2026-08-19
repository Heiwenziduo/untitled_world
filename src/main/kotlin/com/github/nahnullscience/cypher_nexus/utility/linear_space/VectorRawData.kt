package com.github.nahnullscience.cypher_nexus.utility.linear_space

interface IVectorRawDataViewer {
    val dimension: Int
    val size: Int
}

class VectorRawData(
    val count: Int,
    val dimension: Int = 3
) {
    val size = count * dimension
    private val doubles = DoubleArray(size) { Double.NaN }
}