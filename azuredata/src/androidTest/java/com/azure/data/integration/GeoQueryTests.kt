package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.integration.common.DocumentTest
import com.azure.data.integration.common.PartitionedCustomDocment
import com.azure.data.model.Query
import com.azure.data.model.spatial.LineString
import com.azure.data.model.spatial.Point
import com.azure.data.model.spatial.Polygon
import com.azure.data.util.json.gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class GeoQueryTests : DocumentTest<PartitionedCustomDocment>("GeoQueryTests", PartitionedCustomDocment::class.java) {

    init {
        partitionKeyPath = "/testKey"
    }

    @Test
    fun testPointAdapter() {

        val point = Point(42.12, 34.4)

        val tree = gson.toJsonTree(point)
        val jObj = tree.asJsonObject

        assertTrue(jObj["type"].asString == "Point")
        assertTrue(jObj["coordinates"].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray[0].asDouble == point.longitude)
        assertTrue(jObj["coordinates"].asJsonArray[1].asDouble == point.latitude)

        val json = gson.toJson(point)

        val newPoint = gson.fromJson(json, Point::class.java)

        assertEquals(point.longitude, newPoint.longitude, 0.0)
        assertEquals(point.latitude, newPoint.latitude, 0.0)
    }

    @Test
    fun testEqualToDistanceQuery() {

        val point = Point(42.12, 34.4)
        val ptJson = gson.toJson(point).replace("\n", "")
        val query = Query().from(collectionId)
                .whereDistanceEqualTo("point", point, 3000)
                .query

        assertEquals("SELECT * FROM $collectionId WHERE ST_DISTANCE($collectionId.point, $ptJson) = 3000", query)
    }

    @Test
    fun testLessThanDistanceQuery() {

        val point = Point(42.12, 34.4)
        val ptJson = gson.toJson(point).replace("\n", "")
        val query = Query().from(collectionId)
                .whereDistanceLessThan("point", point, 3000)
                .query

        assertEquals("SELECT * FROM $collectionId WHERE ST_DISTANCE($collectionId.point, $ptJson) < 3000", query)
    }

    @Test
    fun testLessThanEqualToDistanceQuery() {

        val point = Point(42.12, 34.4)
        val ptJson = gson.toJson(point).replace("\n", "")
        val query = Query().from(collectionId)
                .whereDistanceLessThanEqualTo("point", point, 3000)
                .query

        assertEquals("SELECT * FROM $collectionId WHERE ST_DISTANCE($collectionId.point, $ptJson) <= 3000", query)
    }

    @Test
    fun testGreaterThanDistanceQuery() {

        val point = Point(42.12, 34.4)
        val ptJson = gson.toJson(point).replace("\n", "")
        val query = Query().from(collectionId)
                .whereDistanceGreaterThan("point", point, 3000)
                .query

        assertEquals("SELECT * FROM $collectionId WHERE ST_DISTANCE($collectionId.point, $ptJson) > 3000", query)
    }

    @Test
    fun testGreaterThanEqualToDistanceQuery() {

        val point = Point(42.12, 34.4)
        val ptJson = gson.toJson(point).replace("\n", "")
        val query = Query().from(collectionId)
                .whereDistanceGreaterThanEqualTo("point", point, 3000)
                .query

        assertEquals("SELECT * FROM $collectionId WHERE ST_DISTANCE($collectionId.point, $ptJson) >= 3000", query)
    }

    @Test
    fun testPolygonAdapter() {

        val polygon = Polygon.start(42.12, 34.4)
                .addCoordinate(52.5, 37.8)
                .addCoordinate(57.25, 40.3)
                .end()

        val tree = gson.toJsonTree(polygon)
        val jObj = tree.asJsonObject

        assertTrue(jObj["type"].asString == "Polygon")
        assertTrue(jObj["coordinates"].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray.size() == 4)
        assertTrue(jObj["coordinates"].asJsonArray[0].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray[1].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray[2].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray[3].isJsonArray)

        assertTrue(jObj["coordinates"].asJsonArray[0].asJsonArray[0].asDouble == polygon.coordinates[0][0])
        assertTrue(jObj["coordinates"].asJsonArray[0].asJsonArray[1].asDouble == polygon.coordinates[0][1])
        assertTrue(jObj["coordinates"].asJsonArray[1].asJsonArray[0].asDouble == polygon.coordinates[1][0])
        assertTrue(jObj["coordinates"].asJsonArray[1].asJsonArray[1].asDouble == polygon.coordinates[1][1])
        assertTrue(jObj["coordinates"].asJsonArray[2].asJsonArray[0].asDouble == polygon.coordinates[2][0])
        assertTrue(jObj["coordinates"].asJsonArray[2].asJsonArray[1].asDouble == polygon.coordinates[2][1])
        assertTrue(jObj["coordinates"].asJsonArray[3].asJsonArray[0].asDouble == polygon.coordinates[3][0])
        assertTrue(jObj["coordinates"].asJsonArray[3].asJsonArray[1].asDouble == polygon.coordinates[3][1])
        // first point should equal last
        assertTrue(jObj["coordinates"].asJsonArray[3].asJsonArray[0].asDouble == polygon.coordinates[0][0])
        assertTrue(jObj["coordinates"].asJsonArray[3].asJsonArray[1].asDouble == polygon.coordinates[0][1])

        val json = gson.toJson(polygon)

        val newPolygon = gson.fromJson(json, Polygon::class.java)

        assertEquals(polygon.coordinates[0][0], newPolygon.coordinates[0][0], 0.0)
        assertEquals(polygon.coordinates[0][1], newPolygon.coordinates[0][1], 0.0)
        assertEquals(polygon.coordinates[1][0], newPolygon.coordinates[1][0], 0.0)
        assertEquals(polygon.coordinates[1][1], newPolygon.coordinates[1][1], 0.0)
        assertEquals(polygon.coordinates[2][0], newPolygon.coordinates[2][0], 0.0)
        assertEquals(polygon.coordinates[2][1], newPolygon.coordinates[2][1], 0.0)
        assertEquals(polygon.coordinates[3][0], newPolygon.coordinates[3][0], 0.0)
        assertEquals(polygon.coordinates[3][1], newPolygon.coordinates[3][1], 0.0)
    }

    @Test
    fun testLineStringAdapter() {

        val lineString = LineString.start(42.12, 34.4)
                .addCoordinate(52.5, 37.8)
                .addCoordinate(57.25, 40.3)
                .end()

        val tree = gson.toJsonTree(lineString)
        val jObj = tree.asJsonObject

        assertTrue(jObj["type"].asString == "LineString")
        assertTrue(jObj["coordinates"].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray.size() == 3)
        assertTrue(jObj["coordinates"].asJsonArray[0].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray[1].isJsonArray)
        assertTrue(jObj["coordinates"].asJsonArray[2].isJsonArray)

        assertTrue(jObj["coordinates"].asJsonArray[0].asJsonArray[0].asDouble == lineString.coordinates[0][0])
        assertTrue(jObj["coordinates"].asJsonArray[0].asJsonArray[1].asDouble == lineString.coordinates[0][1])
        assertTrue(jObj["coordinates"].asJsonArray[1].asJsonArray[0].asDouble == lineString.coordinates[1][0])
        assertTrue(jObj["coordinates"].asJsonArray[1].asJsonArray[1].asDouble == lineString.coordinates[1][1])
        assertTrue(jObj["coordinates"].asJsonArray[2].asJsonArray[0].asDouble == lineString.coordinates[2][0])
        assertTrue(jObj["coordinates"].asJsonArray[2].asJsonArray[1].asDouble == lineString.coordinates[2][1])

        val json = gson.toJson(lineString)

        val newLineString = gson.fromJson(json, LineString::class.java)

        assertEquals(lineString.coordinates[0][0], newLineString.coordinates[0][0], 0.0)
        assertEquals(lineString.coordinates[0][1], newLineString.coordinates[0][1], 0.0)
        assertEquals(lineString.coordinates[1][0], newLineString.coordinates[1][0], 0.0)
        assertEquals(lineString.coordinates[1][1], newLineString.coordinates[1][1], 0.0)
        assertEquals(lineString.coordinates[2][0], newLineString.coordinates[2][0], 0.0)
        assertEquals(lineString.coordinates[2][1], newLineString.coordinates[2][1], 0.0)
    }
}