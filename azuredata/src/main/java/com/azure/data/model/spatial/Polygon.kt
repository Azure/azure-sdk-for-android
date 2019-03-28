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



//class Polygon(vararg coords: DoubleArray): SpatialObject {
//
//    var coordinates: MutableList<DoubleArray>
//
//    init {
//
//        coordinates = coords.toMutableList()
//    }
//
//    constructor(coords: List<DoubleArray>) : this() {
//
//        coordinates = coords.toMutableList()
//    }
//
//    class PolygonBuilder {
//
//        private val coords: MutableList<DoubleArray> = mutableListOf()
//
//        fun addCoordinate(coord: DoubleArray): PolygonBuilder {
//
//            coords.add(coord)
//
//            return this
//        }
//
//        fun addCoordinate(y: Double, x: Double): PolygonBuilder =
//                addCoordinate(doubleArrayOf(y, x))
//
//        fun addCoordinate(point: Point): PolygonBuilder =
//                addCoordinate(point.longitude, point.latitude)
//
//        fun insertCoordinate(index: Int, coord: DoubleArray): PolygonBuilder {
//
//            coords.add(index, coord)
//
//            return this
//        }
//
//        fun insertCoordinate(index: Int, y: Double, x: Double): PolygonBuilder =
//                insertCoordinate(index, doubleArrayOf(y, x))
//
//        fun insertCoordinate(index: Int, point: Point): PolygonBuilder =
//                insertCoordinate(index, point.longitude, point.latitude)
//
//        fun removeCoordinateAt(index: Int): PolygonBuilder {
//
//            coords.removeAt(index)
//
//            return this
//        }
//
//        fun end(): Polygon {
//
//            // close the polygon; "the last coordinate pair provided should be the same as the first, to create a closed shape."
//            addCoordinate(coords[0])
//
//            return Polygon(coords)
//        }
//    }
//
//    companion object {
//
//        fun start(coord: DoubleArray): PolygonBuilder {
//
//            val builder = PolygonBuilder()
//            builder.addCoordinate(coord)
//
//            return builder
//        }
//
//        fun start(y: Double, x: Double): PolygonBuilder =
//                start(doubleArrayOf(y, x))
//
//        fun start(point: Point): PolygonBuilder =
//                start(point.longitude, point.latitude)
//    }
//}