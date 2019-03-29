package com.azure.data.model

import com.azure.data.model.spatial.SpatialObject
import com.azure.data.util.json.gson

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class Query(properties: ArrayList<String>? = null) {

    private var selectCalled = false
    private var fromCalled = false
    private var whereCalled = false
    private var andCalled = false
    private var orderByCalled = false

    private var selectProperties: ArrayList<String> = ArrayList()
    private var whereFragment: String? = null
    private var andFragments: ArrayList<String> = ArrayList()
    private var orderByFragment: String? = null

    private var type: String? = null

    init {
        selectCalled = true

        properties?.let {
            if (!properties.isEmpty()) {
                selectProperties = properties
            }
        }
    }

    val query: String
        get() {

            var query = ""

            if (selectCalled && fromCalled && !type.isNullOrEmpty()) {

                val selectFragment = if (selectProperties.isEmpty()) "*" else "$type.${selectProperties.joinToString(", $type.")}"

                query = "SELECT $selectFragment FROM $type"

                if (whereCalled && !whereFragment.isNullOrEmpty()) {

                    query += " WHERE $whereFragment"

                    if (andCalled && !andFragments.isEmpty()) {
                        query += " AND ${andFragments.joinToString(" AND ")}"
                    }
                }

                if (orderByCalled && !orderByFragment.isNullOrEmpty()) {

                    query += " ORDER BY $type.$orderByFragment"
                }
            }

            return query
        }

    val parameters: Map<String, String>
        get() {
            return mapOf()
        }

    val dictionary: Map<String, Any>
        get() {
            return mapOf("query" to query,
                    "parameters" to if (parameters.isEmpty()) arrayOf() else arrayOf(parameters))
        }

    fun from(type: String) : Query {

        if (!selectCalled) throw Exception("must call `select` before calling `from`")
        if (fromCalled) throw Exception("you can only call `from` once")

        fromCalled = true
        this.type = type

        return this
    }

    private fun whereAny(property: String, value: Any, operator: String = "=", quoteValue: Boolean = value is String) : Query {

        if (whereCalled) throw Exception("you can only call `where` once, to add more constraints use `and`")

        whereCalled = true
        whereFragment = if (quoteValue) "$type.$property $operator '$value'" else "$type.$property $operator $value"

        return this
    }

    fun where(property: String, value: String) : Query = whereAny(property, value, quoteValue = true)

    fun where(property: String, value: Int) : Query = whereAny(property, value, quoteValue = false)

    fun where(property: String, value: Any) : Query = whereAny(property, value)

    fun whereNot(property: String, value: String) : Query = whereAny(property, value, operator = "!=")

    fun whereNot(property: String, value: Int) : Query = whereAny(property, value, operator = "!=", quoteValue = false)

    fun whereGreaterThan(property: String, value: String) : Query = whereAny(property, value, operator = ">")

    fun whereGreaterThan(property: String, value: Int) : Query = whereAny(property, value, operator = ">", quoteValue = false)

    fun whereLessThan(property: String, value: String) : Query = whereAny(property, value, operator = "<")

    fun whereLessThan(property: String, value: Int) : Query = whereAny(property, value, operator = "<", quoteValue = false)

    private fun andWhereAny(property: String, value: Any, operator: String = "=", quoteValue: Boolean = value is String) : Query {

        if (!whereCalled) throw Exception("must call `where` before calling `and`")

        andCalled = true
        andFragments.add(if (quoteValue) "$type.$property $operator '$value'" else "$type.$property $operator $value")

        return this
    }

    fun andWhere(property: String, value: String) : Query = andWhereAny(property, value, quoteValue = true)

    fun andWhere(property: String, value: Int) : Query = whereAny(property, value, quoteValue = false)

    fun andWhere(property: String, value: Any) : Query = andWhereAny(property, value)

    fun andWhereNot(property: String, value: String) : Query = andWhereAny(property, value, operator = "!=")

    fun andWhereNot(property: String, value: Int) : Query = andWhereAny(property, value, operator = "!=", quoteValue = false)

    fun andWhereGreaterThan(property: String, value: String) : Query = andWhereAny(property, value, operator = ">")

    fun andWhereGreaterThan(property: String, value: Int) : Query = andWhereAny(property, value, operator = ">", quoteValue = false)

    fun andWhereGreaterThanEqualTo(property: String, value: String) : Query = andWhereAny(property, value, operator = ">=")

    fun andWhereGreaterThanEqualTo(property: String, value: Int) : Query = andWhereAny(property, value, operator = ">=", quoteValue = false)

    fun andWhereLessThan(property: String, value: String) : Query = andWhereAny(property, value, operator = "<")

    fun andWhereLessThan(property: String, value: Int) : Query = andWhereAny(property, value, operator = "<", quoteValue = false)

    fun andWhereLessThanEqualTo(property: String, value: String) : Query = andWhereAny(property, value, operator = "<=")

    fun andWhereLessThanEqualTo(property: String, value: Int) : Query = andWhereAny(property, value, operator = "<=", quoteValue = false)

    private fun whereDistance(property: String, toSpatial: SpatialObject, operator: String = "=", distance: Number) : Query {

        if (whereCalled) throw Exception("you can only call `where` once, to add more constraints use `and`")

        val spatialJson = gson.toJson(toSpatial).replace("\n", "")

        whereFragment = "ST_DISTANCE($type.$property, $spatialJson) $operator $distance"
        whereCalled = true

        return this
    }

    fun whereDistanceEqualTo(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            whereDistance(property, toSpatial, "=", distance)

    fun whereDistanceLessThan(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            whereDistance(property, toSpatial, "<", distance)

    fun whereDistanceLessThanEqualTo(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            whereDistance(property, toSpatial, "<=", distance)

    fun whereDistanceGreaterThan(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            whereDistance(property, toSpatial, ">", distance)

    fun whereDistanceGreaterThanEqualTo(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            whereDistance(property, toSpatial, ">=", distance)

    private fun andWhereDistance(property: String, toSpatial: SpatialObject, operator: String = "=", distance: Number) : Query {

        if (!whereCalled) throw Exception("must call `where` before calling `and`")

        val spatialJson = gson.toJson(toSpatial).replace("\n", "")

        andCalled = true
        andFragments.add("ST_DISTANCE($type.$property, $spatialJson) $operator $distance")

        return this
    }

    fun andWhereDistanceEqualTo(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            andWhereDistance(property, toSpatial, "=", distance)

    fun andWhereDistanceLessThan(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            andWhereDistance(property, toSpatial, "<", distance)

    fun andWhereDistanceLessThanEqualTo(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            andWhereDistance(property, toSpatial, "<=", distance)

    fun andWhereDistanceGreaterThan(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            andWhereDistance(property, toSpatial, ">", distance)

    fun andWhereDistanceGreaterThanEqualTo(property: String, toSpatial: SpatialObject, distance: Number) : Query =
            andWhereDistance(property, toSpatial, ">=", distance)

    fun whereGeoIntersectsWith(spatialProperty: String, spatial: SpatialObject) : Query {

        if (whereCalled) throw Exception("you can only call `where` once, to add more constraints use `and`")

        val spatialJson = gson.toJson(spatial).replace("\n", "")

        whereFragment = "ST_INTERSECTS($type.$spatialProperty, $spatialJson)"
        whereCalled = true

        return this
    }

    fun andWhereGeoIntersectsWith(spatialProperty: String, spatial: SpatialObject) : Query {

        if (!whereCalled) throw Exception("must call `where` before calling `and`")

        val spatialJson = gson.toJson(spatial).replace("\n", "")

        andCalled = true
        andFragments.add("ST_INTERSECTS($type.$spatialProperty, $spatialJson)")

        return this
    }

    fun whereGeoWithin(spatialProperty: String, withinSpatial: SpatialObject) : Query {

        if (whereCalled) throw Exception("you can only call `where` once, to add more constraints use `and`")

        val spatialJson = gson.toJson(withinSpatial).replace("\n", "")

        whereFragment = "ST_WITHIN($type.$spatialProperty, $spatialJson)"
        whereCalled = true

        return this
    }

    fun andWhereGeoWithin(spatialProperty: String, withinSpatial: SpatialObject) : Query {

        if (!whereCalled) throw Exception("must call `where` before calling `and`")

        val spatialJson = gson.toJson(withinSpatial).replace("\n", "")

        andCalled = true
        andFragments.add("ST_WITHIN($type.$spatialProperty, $spatialJson)")

        return this
    }

    fun orderBy(property: String, descending: Boolean = false) : Query {

        if (orderByCalled) throw Exception("you can only call `orderBy` once, to order on an additional level use `thenBy`")

        orderByFragment = property

        if (descending) {
            orderByFragment += " DESC"
        }

        orderByCalled = true

        return this
    }

    override fun hashCode(): Int {

        // generated query is what we want to determine equality from
        return this.query.hashCode()
    }

    override fun equals(other: Any?): Boolean {

        if (other is Query) {
            return this.query == other.query
        }

        return super.equals(other)
    }

    override fun toString(): String = query

    companion object {

        fun select() : Query {
            //assert(!selectCalled, "you can only call select once")
            //selectCalled = true;

            return Query()
        }

//        fun select(vararg strings: String) : Query {
//            //        assert(selectCalled, "you can only call `select` once")
//            //        selectCalled = true;
//
//            //        self.selectProperties = properties
//
//            return Query()
//        }
    }
}