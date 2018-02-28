package com.azure.data.model

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
    private var fromFragment: String? = null
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

                //fromFragment = type!

                query = "SELECT $selectFragment FROM $type"

                if (whereCalled && !whereFragment.isNullOrEmpty()) {

                    query += " WHERE $type.$whereFragment"

                    if (andCalled && !andFragments.isEmpty()) {
                        query += " AND $type."
                        query += andFragments.joinToString(" AND $type.")
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

    private fun whereAny(property: String, value: Any, operator: String = "=", quoteValue: Boolean = true) : Query {

        if (whereCalled) throw Exception("you can only call `where` once, to add more constraints use `and`")

        whereCalled = true
        whereFragment = if (quoteValue) "$property $operator '$value'" else "$property $operator $value"

        return this
    }

    fun where(property: String, value: String) : Query = whereAny(property, value)

    fun where(property: String, value: Int) : Query = whereAny(property, value, quoteValue = false)

    fun whereNot(property: String, value: String) : Query = whereAny(property, value, operator = "!=")

    fun whereNot(property: String, value: Int) : Query = whereAny(property, value, operator = "!=", quoteValue = false)

    fun whereGreaterThan(property: String, value: String) : Query = whereAny(property, value, operator = ">")

    fun whereGreaterThan(property: String, value: Int) : Query = whereAny(property, value, operator = ">", quoteValue = false)

    fun whereLessThan(property: String, value: String) : Query = whereAny(property, value, operator = "<")

    fun whereLessThan(property: String, value: Int) : Query = whereAny(property, value, operator = "<", quoteValue = false)

    private fun andWhereAny(property: String, value: Any, operator: String = "=", quoteValue: Boolean = true) : Query {

        if (!whereCalled) throw Exception("must call `where` before calling `and`")

        andCalled = true
        andFragments.add(if (quoteValue) "$property $operator '$value'" else "$property $operator $value")

        return this
    }

    fun andWhere(property: String, value: String) : Query = andWhereAny(property, value)

    fun andWhere(property: String, value: Int) : Query = andWhereAny(property, value, quoteValue = false)

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

    fun orderBy(property: String, descending: Boolean = false) : Query {

        if (orderByCalled) throw Exception("you can only call `orderBy` once, to order on an additional level use `thenBy`")

        orderByFragment = property

        if (descending) {
            orderByFragment += " DESC"
        }

        return this
    }

    override fun toString(): String = query

    companion object {

        fun select() : Query {
            //assert(!selectCalled, "you can only call select once")
            //selectCalled = true;

            return Query()
        }

        fun select(vararg strings: String) : Query {
            //        assert(selectCalled, "you can only call `select` once")
            //        selectCalled = true;

            //        self.selectProperties = properties

            return Query()
        }
    }
}