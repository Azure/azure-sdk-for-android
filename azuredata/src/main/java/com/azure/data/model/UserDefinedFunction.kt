package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a user defined function in the Azure Cosmos DB service.
 * 
 *  - Remark:
 *    Azure Cosmos DB supports JavaScript user defined functions (UDFs) which are stored in
 *    the database and can be used inside queries.
 *    Refer to [javascript-integration](http://azure.microsoft.com/documentation/articles/documentdb-sql-query/#javascript-integration) for how to use UDFs within queries.
 *    Refer to [udf](http://azure.microsoft.com/documentation/articles/documentdb-programming/#udf) for more details about implementing UDFs in JavaScript.
 */
class UserDefinedFunction(id: String? = null, body: String? = null) : Resource(id) {

    /**
     * Gets or sets the body of the user defined function for the Azure Cosmos DB service.
     * 
     *  - Remark:
     *    This must be a valid JavaScript function
     * 
     *  - Example:
     *    `"function (input) { return input.toLowerCase(); }"`
     */
    var body: String? = body

    companion object {

        const val resourceName = "UserDefinedFunction"
        const val listName = "UserDefinedFunctions"
    }
}