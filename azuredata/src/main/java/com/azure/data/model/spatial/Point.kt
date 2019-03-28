package com.azure.data.model.spatial

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a Point geometry.
 */
data class Point(var longitude: Double, var latitude: Double): SpatialObject {

    constructor(longitude: Number, latitude: Number) : this(longitude.toDouble(), latitude.toDouble())
}