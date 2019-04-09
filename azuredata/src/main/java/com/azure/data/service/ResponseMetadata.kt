package com.azure.data.service

import com.azure.core.http.HttpHeader
import com.azure.core.util.dateFromRfc1123
import com.azure.data.constants.MSHttpHeader
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

// https://docs.microsoft.com/en-us/rest/api/cosmos-db/common-cosmosdb-rest-response-headers
open class ResponseMetadata(response: okhttp3.Response?) {

    /**
     * The unique identifier of the operation.
     */
    val activityId: String? = response?.header(MSHttpHeader.MSActivityId.value)

    /**
     * The alternate path to the resource constructed using user-supplied IDs.
     */
    val alternateContentPath: String? = response?.header(MSHttpHeader.MSAltContentPath.value)

    /**
     * The Content-Type is always `application/json`.
     */
    val contentType: String? = response?.header(HttpHeader.ContentType.value)

    /**
     * Represents the intermediate state of query or read-feed execution and is returned when
     * there are additional results aside from what was returned in the response. Clients can
     * resubmit the request with the request header `x-ms-continuation` containing this value.
     */
    val continuation: String? = response?.header(MSHttpHeader.MSContinuation.value)

    /**
     * The date time of the response operation.
     */
    val date: Date? = response?.header(HttpHeader.Date.value)?.let { dateFromRfc1123(it) }

    /**
     * The `etag` of the resource retrieved.
     */
    val etag: String? = response?.header(HttpHeader.ETag.value)

    /**
     * The number of items returned for a query or a read-feed request.
     */
    val itemCount: Int? = response?.header(MSHttpHeader.MSItemCount.value)?.toInt()

    /**
     * The number of request units for the operation.
     */
    val requestCharge: Double? = response?.header(MSHttpHeader.MSRequestCharge.value)?.toDouble()

    /**
     * The allotted quota for a resource in a Azure CosmosDB account.
     */
    val resourceQuota: Metrics? = Metrics(response?.header(MSHttpHeader.MSResourceQuota.value))

    /**
     * The current usage of a resource in a Azure CosmosDB account.
     */
    val resourceUsage: Metrics? = Metrics(response?.header(MSHttpHeader.MSResourceUsage.value))

    /**
     * The number of seconds to wait to retry the operation after an initial operation received the
     * HTTP status code 429 and was throttled.
     */
    val retryAfter: Long? = response?.header(MSHttpHeader.MSRetryAfterMs.value)?.toLong()


    /**
     * The resource schema version.
     */
    val schemaVersion: String? = response?.header(MSHttpHeader.MSSchemaVersion.value)
            ?.split("=")
            ?.let {
                when {
                    (it.size == 1) -> it[0]
                    (it.size > 1) -> it[1]
                    else -> null
                }
            }

    /**
     * The service version number.
     */
    val serviceVersion: String? = response?.header(MSHttpHeader.MSServiceVersion.value)
            ?.split("=")
            ?.let {
                when {
                    (it.size == 1) -> it[0]
                    (it.size > 1) -> it[1]
                    else -> null
                }
            }

    /**
     * The session token of the request.
     */
    val sessionToken: String? = response?.header(MSHttpHeader.MSSessionToken.value)

    class Metrics(usage: String?) {

        /**
         * The number of collections within an Azure CosmosDB account.
         */
        var collections: Int? = null

        /**
         * The size of a collection in kilobytes.
         */
        var collectionSize: Int? = null

        /**
         * The number of documents within a collection.
         */
        var documents: Int? = null

        /**
         * The size of a document within a collection.
         */
        var documentSize: Int? = null

        /**
         * The size of all the documents within a collection.
         */
        var documentsSize: Int? = null

        /**
         * The number of user defined functions within a collection.
         */
        var functions: Int? = null

        /**
         * The number of stored procedures within a collection.
         */
        var storedProcedures: Int? = null

        /**
         * The number of triggers within a collection.
         */
        var triggers: Int? = null

        init {
            usage?.split(";")
                    ?.map { it.split("=") }
                    ?.forEach {
                        when (it[0]) {
                            "collections" -> collections = it[1].toInt()
                            "collectionSize" -> collectionSize = it[1].toInt()
                            "documentsCount" -> documents = it[1].toInt()
                            "documentSize" -> documentSize = it[1].toInt()
                            "documentsSize" -> documentsSize = it[1].toInt()
                            "functions" -> functions = it[1].toInt()
                            "storedProcedures" -> storedProcedures = it[1].toInt()
                            "triggers" -> triggers = it[1].toInt()
                        }
                    }
        }

    }
}