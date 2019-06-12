package com.azure.data.model.service

import com.azure.core.http.*
import com.azure.data.constants.HttpHeaderValue
import com.azure.data.constants.MSHttpHeader
import com.azure.data.model.Resource
import com.azure.data.model.ResourceList
import com.azure.data.model.partition.PartitionKeyRange
import com.azure.data.model.partition.PartitionKeyResource
import com.azure.data.service.PartitionKeyPropertyCache
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.Request
import java.lang.reflect.Type

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

data class RequestDetails(val resourceLocation: ResourceLocation) {

    constructor(resourceLocation: ResourceLocation, partitionKey: String?) : this(resourceLocation) {

        partitionKey?.let {

            this.partitionKey = listOf(partitionKey)
        }
    }

    lateinit var method: HttpMethod

    var partitionKey: List<String>? = null

    var maxPerPage: Int? = null

    //  !NOTE!: we only accept a ByteArray body on RequestDetails due to a feature/bug in OkHttp that will tack on
    //  a charset string that does not work well with certain operations (Query!) when converting a json body
    //  A json body can be used by calling json.toByteArray() first
    @Transient
    var body: ByteArray? = null

    var contentType: String = HttpMediaType.Json.value

    @Transient
    var resourceType: Type? = null

    var isQuery: Boolean = false

    var partitionKeyRange: ResourceList<PartitionKeyRange>? = null

    val preTriggers: MutableSet<String> by lazy {
        mutableSetOf<String>()
    }

    val postTriggers: MutableSet<String> by lazy {
        mutableSetOf<String>()
    }

    var ifNoneMatchETag: String? = null

    var offerThroughput: Int? = null

    var isUpsert: Boolean? = null

    var slug: String? = null

    var acceptEncoding: String? = null

    var cacheControl: String? = null

    fun <T : Resource> setResourcePartitionKey(resource: T) {

        if (resource is PartitionKeyResource && this.partitionKey.isNullOrEmpty()) {

            this.partitionKey = PartitionKeyPropertyCache.getPartitionKeyValues(resource)
        }
    }

    fun fillHeaders(headersBuilder: Headers.Builder) {

        // add max item count, if needed (queries/feeds)
        this.maxPerPage?.let { max ->

            if ((1..1000).contains(max)) {

                headersBuilder.add(MSHttpHeader.MSMaxItemCount.value, max.toString())
            } else {

                throw DocumentClientError.InvalidMaxPerPageError
            }
        }

        // if we have a body to send, we need to also send the content type
        if (this.body != null) {

            //  !NOTE!: we only accept a ByteArray body on RequestDetails due to a feature/bug in OkHttp that will tack on
            //  a charset string that does not work well with certain operations (Query!) when converting a json body
            //  A json body can be used by calling json.toByteArray() first

            headersBuilder.add(HttpHeader.ContentType.value, contentType)
        }

        // partition key specification
        if (!partitionKey.isNullOrEmpty()) {

            //send the partition key(s) in the form of header: x-ms-documentdb-partitionkey: ["<My Partition Key>"]
            val partitionKeyValue = partitionKey!!.joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")

            headersBuilder.add(MSHttpHeader.MSDocumentDBPartitionKey.value, partitionKeyValue)
        }

        // Query-specific headers
        if (isQuery) {

            // need to set the special query headers
            headersBuilder.add(MSHttpHeader.MSDocumentDBIsQuery.value, HttpHeaderValue.trueValue)
            headersBuilder.add(HttpHeader.ContentType.value, HttpMediaType.QueryJson.value)

            if (partitionKey.isNullOrEmpty()) {

                // if we're querying, do we have a partition key set?  If not, then this query will be a cross partition query
                headersBuilder.add(MSHttpHeader.MSDocumentDBQueryEnableCrossPartition.value, HttpHeaderValue.trueValue)
            }
        }

        // partition key range specification
        partitionKeyRange?.let {

            if (it.items.isNotEmpty()) {

                // TODO:  Note, we may need to do more here if there are additional PartitionKeyRange items that come back... can't find docs on this format
                headersBuilder.add(MSHttpHeader.MSDocumentDBPartitionKeyRangeId.value, "${it.resourceId!!},${it.items[0].id}")
            }
        }

        // pre/post triggers
        if (this.postTriggers.isNotEmpty()) {

            val postTriggerInclude = this.postTriggers.joinToString(",")
            headersBuilder.add(MSHttpHeader.MSDocumentDBPostTriggerInclude.value, postTriggerInclude)
        }

        if (this.preTriggers.isNotEmpty()) {

            val preTriggerInclude = this.preTriggers.joinToString(",")
            headersBuilder.add(MSHttpHeader.MSDocumentDBPreTriggerInclude.value, preTriggerInclude)
        }

        // if we have an eTag, we'll set & send the IfNoneMatch header
        if (!ifNoneMatchETag.isNullOrEmpty()) {

            headersBuilder.add(HttpHeader.IfNoneMatch.value, ifNoneMatchETag!!)
        }

        offerThroughput?.let {

            headersBuilder.add(MSHttpHeader.MSOfferThroughput.value, "$it")
        }

        isUpsert?.let {

            headersBuilder.add(MSHttpHeader.MSDocumentDBIsUpsert.value, isUpsert.toString())
        }

        slug?.let {

            headersBuilder.add(HttpHeader.Slug.value, it)
        }

        acceptEncoding?.let {

            headersBuilder.add(HttpHeader.Accept.value, it)
        }

        cacheControl?.let {

            headersBuilder.add(HttpHeader.CacheControl.value, it)
        }
    }

    fun buildRequest(url: HttpUrl, headersBuilder: Headers.Builder): Request {

        val builder = Request.Builder()
                .headers(headersBuilder.build())
                .url(url)

        return builder.withMethod(this.method, this.body.toRequestBody(MediaType.parse(this.contentType))).build()
    }

    companion object {

        fun <T : Resource> fromResource(resource: T, partitionKey: String? = null): RequestDetails {

            val details = RequestDetails(ResourceLocation.Resource(resource))
            details.resourceType = resource::class.java

            //look for partition key property(ies) to send for this resource type
            partitionKey?.let {

                details.partitionKey = listOf(it)

            } ?: run { // otherwise check the resource itself

                details.setResourcePartitionKey(resource)
            }

            return details
        }
    }
}