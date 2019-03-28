package com.azure.data.model.spatial

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a geometry consisting of connected line segments.
 */

class LineString : LineSegmentObject() {

    class LineStringBuilder : GeoBuilder<LineString>(LineString::class.java)

    companion object : GeoStarter<LineString, LineStringBuilder>(LineStringBuilder::class.java)
}