package com.azure.data.model.indexing

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Defines the target data type of an index path specification in the Azure Cosmos DB service.
 *
 * - lineString:   Represent a line string data type.
 * - number:       Represent a numeric data type.
 * - point:        Represent a point data type.
 * - polygon:      Represent a polygon data type.
 * - string:       Represent a string data type.
 */
enum class DataType {

    LineString,
    Number,
    Point,
    Polygon,
    String
}