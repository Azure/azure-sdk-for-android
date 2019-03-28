package com.azure.data.model.spatial

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

open class GeoStarter<TSpatialObj : LineSegmentObject, TBuilder : GeoBuilder<TSpatialObj>>(private val clazz: Class<TBuilder>) {

    fun start(coord: DoubleArray): TBuilder {

        val builder = clazz.newInstance()

        builder.addCoordinate(coord)

        return builder
    }

    fun start(y: Double, x: Double): TBuilder =
            start(doubleArrayOf(y, x))

    fun start(point: Point): TBuilder =
            start(point.longitude, point.latitude)
}

open class GeoBuilder<TObject : LineSegmentObject>(private val clazz: Class<TObject>) {

    protected val coords: MutableList<DoubleArray> = mutableListOf()

    fun addCoordinate(coord: DoubleArray): GeoBuilder<TObject> {

        coords.add(coord)

        return this
    }

    fun addCoordinate(y: Double, x: Double): GeoBuilder<TObject> =
            addCoordinate(doubleArrayOf(y, x))

    fun addCoordinate(point: Point): GeoBuilder<TObject> =
            addCoordinate(point.longitude, point.latitude)

    fun insertCoordinate(index: Int, coord: DoubleArray): GeoBuilder<TObject> {

        coords.add(index, coord)

        return this
    }

    fun insertCoordinate(index: Int, y: Double, x: Double): GeoBuilder<TObject> =
            insertCoordinate(index, doubleArrayOf(y, x))

    fun insertCoordinate(index: Int, point: Point): GeoBuilder<TObject> =
            insertCoordinate(index, point.longitude, point.latitude)

    fun removeCoordinateAt(index: Int): GeoBuilder<TObject> {

        coords.removeAt(index)

        return this
    }

    open fun end() : TObject {

        val geoObject = clazz.newInstance()
        geoObject.coordinates.addAll(coords)

        return geoObject
    }
}