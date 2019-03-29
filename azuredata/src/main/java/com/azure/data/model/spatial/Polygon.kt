package com.azure.data.model.spatial

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a Polygon geometry.
 *
 * The GeoJSON specification requires that for valid Polygons, the last coordinate pair provided should be the same as the first, to create a closed shape.
 *
 * Points within a Polygon must be specified in counter-clockwise order. A Polygon specified in clockwise order represents the inverse of the region within it.
 */

class Polygon : LineSegmentObject() {

    class PolygonBuilder : GeoBuilder<Polygon>(Polygon::class.java) {

        override fun end(): Polygon {

            // close the polygon; "the last coordinate pair provided should be the same as the first, to create a closed shape."
            addCoordinate(coords[0])

            return super.end()
        }
    }

    companion object : GeoStarter<Polygon, PolygonBuilder>(PolygonBuilder::class.java)
}