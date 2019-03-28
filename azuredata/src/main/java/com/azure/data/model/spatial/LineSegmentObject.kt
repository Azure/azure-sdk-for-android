package com.azure.data.model.spatial

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Base class for geospatial objects consisting of multiple coordinates/line segments.
 */
abstract class LineSegmentObject(vararg coords: DoubleArray): SpatialObject {

    var coordinates: MutableList<DoubleArray>

    init {

        coordinates = coords.toMutableList()
    }

    constructor(coords: List<DoubleArray>) : this() {

        coordinates = coords.toMutableList()
    }
}