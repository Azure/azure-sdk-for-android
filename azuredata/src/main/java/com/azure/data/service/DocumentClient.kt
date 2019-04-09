package com.azure.data.service

import com.azure.core.http.*
import com.azure.core.log.configureNetworkLogging
import com.azure.core.log.d
import com.azure.core.log.e
import com.azure.core.network.NetworkConnectivity
import com.azure.core.network.NetworkConnectivityManager
import com.azure.core.util.ContextProvider
import com.azure.core.util.DateUtil
import com.azure.core.util.urlEncode
import com.azure.data.constants.HttpHeaderValue
import com.azure.data.constants.MSHttpHeader
import com.azure.data.model.*
import com.azure.data.model.indexing.IndexingPolicy
import com.azure.data.model.partition.PartitionKeyRange
import com.azure.data.model.partition.PartitionKeyResource
import com.azure.data.model.service.DataError
import com.azure.data.model.service.DocumentClientError
import com.azure.data.model.service.RequestDetails
import com.azure.data.model.service.Result
import com.azure.data.util.*
import com.azure.data.util.json.ResourceListJsonDeserializer
import com.azure.data.util.json.gson
import getDefaultHeaders
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DocumentClient private constructor() {

    private var host: String? = null

    private var permissionProvider: PermissionProvider? = null

    private var resourceTokenProvider: ResourceTokenProvider? = null

    private var isOffline = false

    var connectivityManager: NetworkConnectivityManager? = null
        set(value) {
            if (isConfigured && value != null) {
                value.registerListener(networkConnectivityChanged)
                value.startListening()
            }
        }

    //region Configuration

    val configuredWithMasterKey: Boolean
        get() = resourceTokenProvider != null

    fun configure(accountName: String, masterKey: String, permissionMode: PermissionMode) {

        resourceTokenProvider = ResourceTokenProvider(masterKey, permissionMode)

        commonConfigure("$accountName.documents.azure.com")
    }

    fun configure(accountUrl: URL, masterKey: String, permissionMode: PermissionMode) {

        resourceTokenProvider = ResourceTokenProvider(masterKey, permissionMode)

        commonConfigure(accountUrl.host)
    }

    fun configure(accountUrl: HttpUrl, masterKey: String, permissionMode: PermissionMode) {

        resourceTokenProvider = ResourceTokenProvider(masterKey, permissionMode)

        commonConfigure(accountUrl.host())
    }

    fun configure(accountName: String, permissionProvider: PermissionProvider) {

        this.permissionProvider = permissionProvider

        commonConfigure("$accountName.documents.azure.com")
    }

    fun configure(accountUrl: URL, permissionProvider: PermissionProvider) {

        this.permissionProvider = permissionProvider

        commonConfigure(accountUrl.host)
    }

    fun configure(accountUrl: HttpUrl, permissionProvider: PermissionProvider) {

        this.permissionProvider = permissionProvider

        commonConfigure(accountUrl.host())
    }

    val isConfigured: Boolean
        get() = !host.isNullOrEmpty() && (resourceTokenProvider != null || permissionProvider != null)

    // base headers... grab these once and then re-serve
    private val defaultHeaders: Headers by lazy {
        ContextProvider.appContext.getDefaultHeaders()
    }

    private fun commonConfigure(host: String) {

        if (host.isEmpty()) {
            throw Exception("Host is invalid")
        }

        this.host = host

        ResourceOracle.init(ContextProvider.appContext, host)
        PermissionCache.init(host)

        connectivityManager = NetworkConnectivity.manager

        // create client and configure OkHttp logging if logLevel is low enough
        val builder = OkHttpClient.Builder()

        configureNetworkLogging(builder)

        client = builder.build()
    }

    fun reset () {

        host = null
        permissionProvider = null
        resourceTokenProvider = null
    }

    //endregion

    //region Network Connectivity

    private val networkConnectivityChanged: (Boolean) -> Unit = { isConnected ->

        d { "Network Status Changed: ${if (isConnected) "Connected" else "Not Connected"}" }
        this.isOffline = !isConnected

        if (isConnected) {
            ResourceWriteOperationQueue.shared.sync()
        }
    }

    //endregion

    //region Database

    // create
    fun createDatabase(databaseId: String, throughput: Int?, callback: (Response<Database>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Database())

        return throughput?.let {

            if (!it.isValidThroughput()) {
                return callback(Response(DataError(DocumentClientError.InvalidThroughputError)))
            }

            requestDetails.headers = mutableMapOf(MSHttpHeader.MSOfferThroughput.value to "$it")

            create(Database(databaseId), requestDetails, callback)
        } ?: create(Database(databaseId), requestDetails, callback)
    }

    // list
    fun getDatabases(maxPerPage: Int? = null, callback: (ListResponse<Database>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Database())
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // get
    fun getDatabase(databaseId: String, callback: (Response<Database>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Database(databaseId))

        return resource(requestDetails, callback)
    }

    // delete
    fun deleteDatabase(databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Database(databaseId))

        return delete(requestDetails, callback)
    }

    //endregion

    //region Collections

    // create
    @Deprecated("Creating a collection without a partition key is deprecated and will be removed in a future version of AzureData", ReplaceWith("createCollection(collectionId: String, throughput: Int? = null, partitionKey: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit)"))
    fun createCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Collection(databaseId))

        return create(DocumentCollection(collectionId), requestDetails, callback)
    }

    // create
    fun createCollection(collectionId: String, throughput: Int? = null, partitionKey: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Collection(databaseId))

        return throughput?.let {

            if (!it.isValidThroughput()) {
                return callback(Response(DataError(DocumentClientError.InvalidThroughputError)))
            }

            requestDetails.headers = mutableMapOf(MSHttpHeader.MSOfferThroughput.value to "$it")

            create(DocumentCollection(collectionId, partitionKey), requestDetails, callback)
        } ?: create(DocumentCollection(collectionId, partitionKey), requestDetails, callback)
    }

    // list
    fun getCollectionsIn(databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<DocumentCollection>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Collection(databaseId))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // get
    fun getCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Collection(databaseId, collectionId))

        return resource(requestDetails, callback)
    }

    // delete
    fun deleteCollection(collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Collection(databaseId, collectionId))

        return delete(requestDetails, callback)
    }

    // replace
    fun replaceCollection(collection: DocumentCollection, databaseId: String, indexingPolicy: IndexingPolicy, callback: (Response<DocumentCollection>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Collection(databaseId, collection.id))

        collection.indexingPolicy = indexingPolicy

        return replace(collection, requestDetails, callback)
    }

    // get partition key ranges
    fun getCollectionPartitionKeyRanges(collectionId: String, databaseId: String, callback: (ListResponse<PartitionKeyRange>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.PkRanges(databaseId, collectionId))

        return resources(requestDetails, callback)
    }

    //endregion

    //region Documents

    // create
    fun <T : Document> createDocument(document: T, partitionKey: String? = null, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return create(document, requestDetails, callback)
    }

    // create
    fun <T : Document> createDocument (document: T, partitionKey: String? = null, collection: DocumentCollection, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return create(document, requestDetails, callback)
    }

    // createOrReplace
    fun <T : Document> createOrUpdateDocument(document: T, partitionKey: String? = null, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)
        requestDetails.addHeader(MSHttpHeader.MSDocumentDBIsUpsert.value, HttpHeaderValue.trueValue)

        return create(document, requestDetails, callback)
    }

    // createOrReplace
    fun <T : Document> createOrUpdateDocument (document: T, partitionKey: String? = null, collection: DocumentCollection, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)
        requestDetails.addHeader(MSHttpHeader.MSDocumentDBIsUpsert.value, HttpHeaderValue.trueValue)

        return create(document, requestDetails, callback)
    }

    // list
    fun <T : Document> getDocumentsAs(collectionId: String, databaseId: String, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId))
        requestDetails.maxPerPage = maxPerPage
        requestDetails.resourceType = documentClass

        return resources(requestDetails, callback)
    }

    // list
    fun <T : Document> getDocumentsAs(collection: DocumentCollection, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection))
        requestDetails.maxPerPage = maxPerPage
        requestDetails.resourceType = documentClass

        return resources(requestDetails, callback)
    }

    // get
    fun <T : Document> getDocument(documentId: String, partitionKey: String?, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId, documentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)
        requestDetails.resourceType = documentClass

        return resource(requestDetails, callback)
    }

    // get
    fun <T : Document> getDocument(documentId: String, partitionKey: String?, collection: DocumentCollection, documentClass: Class<T>, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection, documentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)
        requestDetails.resourceType = documentClass

        return resource(requestDetails, callback)
    }

    // delete
    @Deprecated("Deleting a document without a partition key is deprecated and will be removed in a future version of AzureData", ReplaceWith("deleteDocument(documentId: String, partitionKey: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit)"))
    fun deleteDocument(documentId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId, documentId))

        return delete(requestDetails, callback)
    }

    // delete
    @Deprecated("Deleting a document without a partition key is deprecated and will be removed in a future version of AzureData", ReplaceWith("deleteDocument(documentId: String, partitionKey: String, collection: DocumentCollection, callback: (DataResponse) -> Unit)"))
    fun deleteDocument(documentId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection, documentId))

        return delete(requestDetails, callback)
    }

    // delete
    fun deleteDocument(documentId: String, partitionKey: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId, documentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return delete(requestDetails, callback)
    }

    // delete
    fun deleteDocument(documentId: String, partitionKey: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection, documentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return delete(requestDetails, callback)
    }

    // replace
    fun <T : Document> replaceDocument(document: T, partitionKey: String? = null, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId, document.id), partitionKey)
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return replace(document, requestDetails, callback)
    }

    // replace
    fun <T : Document> replaceDocument(document: T, partitionKey: String? = null, collection: DocumentCollection, callback: (Response<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection, document.id), partitionKey)
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return replace(document, requestDetails, callback)
    }

    // query
    fun <T : Document> queryDocuments (collectionId: String, databaseId: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId))
        requestDetails.maxPerPage = maxPerPage
        requestDetails.resourceType = documentClass

        return query(query, requestDetails, callback)
    }

    // query
    fun <T : Document> queryDocuments (collectionId: String, partitionKey: String, databaseId: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId))
        requestDetails.maxPerPage = maxPerPage
        requestDetails.partitionKey = partitionKey
        requestDetails.resourceType = documentClass

        return query(query, requestDetails, callback)
    }

    // query
    fun <T : Document> queryDocuments (collection: DocumentCollection, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection))
        requestDetails.maxPerPage = maxPerPage
        requestDetails.resourceType = documentClass

        return query(query, requestDetails, callback)
    }

    // query
    fun <T : Document> queryDocuments (collection: DocumentCollection, partitionKey: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection))
        requestDetails.maxPerPage = maxPerPage
        requestDetails.partitionKey = partitionKey
        requestDetails.resourceType = documentClass

        return query(query, requestDetails, callback)
    }

    // get/query a single doc
    fun <T : Document> findDocument(documentId: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        // create query
        val query = Query.select()
                .from(collectionId)
                .where(Resource.Companion.Keys.idKey, documentId)

        val requestDetails = RequestDetails(ResourceLocation.Document(databaseId, collectionId))
        requestDetails.maxPerPage = 1
        requestDetails.resourceType = documentClass

        return query(query, requestDetails, callback)
    }

    // get/query a single doc
    fun <T : Document> findDocument(documentId: String, collection: DocumentCollection, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        // create query
        val query = Query.select()
                .from(collection.id)
                .where(Resource.Companion.Keys.idKey, documentId)

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Document, collection))
        requestDetails.maxPerPage = 1
        requestDetails.resourceType = documentClass

        return query(query, requestDetails, callback)
    }

    //endregion

    //region Attachments

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Attachment(databaseId, collectionId, documentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), requestDetails, callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Attachment(databaseId, collectionId, documentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)
        requestDetails.addHeader(HttpHeader.ContentType.value, contentType)
        requestDetails.addHeader(HttpHeader.Slug.value, attachmentId)
        requestDetails.body = media

        return createOrReplace(requestDetails, false, callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Attachment, document))
        requestDetails.headers = setResourcePartitionKey(document)

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), requestDetails, callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Attachment, document))
        requestDetails.headers = setResourcePartitionKey(document)
        requestDetails.addHeader(HttpHeader.ContentType.value, contentType)
        requestDetails.addHeader(HttpHeader.Slug.value, attachmentId)
        requestDetails.body = media

        return createOrReplace(requestDetails, false, callback)
    }

    // list
    fun getAttachments(documentId: String, collectionId: String, databaseId: String, partitionKey: String, maxPerPage: Int? = null, callback: (ListResponse<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Attachment(databaseId, collectionId, documentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // list
    fun getAttachments(document: Document, maxPerPage: Int? = null, callback: (ListResponse<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Attachment, document))
        requestDetails.headers = setResourcePartitionKey(document)
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // delete
    fun deleteAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return delete(requestDetails, callback)
    }

    // delete
    fun deleteAttachment(attachmentId: String, document: Document, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Attachment, document, attachmentId))
        requestDetails.headers = setResourcePartitionKey(document)

        return delete(requestDetails, callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), requestDetails, callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId))
        requestDetails.headers = setPartitionKeyHeader(partitionKey)
        requestDetails.addHeader(HttpHeader.ContentType.value, contentType)
        requestDetails.addHeader(HttpHeader.Slug.value, attachmentId)
        requestDetails.body = media

        return createOrReplace(requestDetails, true, callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Attachment, document, attachmentId))
        requestDetails.headers = setResourcePartitionKey(document)

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), requestDetails, callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Attachment, document, attachmentId))
        requestDetails.headers = setResourcePartitionKey(document)
        requestDetails.addHeader(HttpHeader.ContentType.value, contentType)
        requestDetails.addHeader(HttpHeader.Slug.value, attachmentId)
        requestDetails.body = media

        return createOrReplace(requestDetails, true, callback)
    }

    //endregion

    //region Stored Procedures

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.StoredProcedure(databaseId, collectionId))

        return create(StoredProcedure(storedProcedureId, procedure), requestDetails, callback)
    }

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.StoredProcedure, collection))

        return create(StoredProcedure(storedProcedureId, procedure), requestDetails, callback)
    }

    // list
    fun getStoredProcedures(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<StoredProcedure>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.StoredProcedure(databaseId, collectionId))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // list
    fun getStoredProcedures(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<StoredProcedure>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.StoredProcedure, collection))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId))

        return delete(requestDetails, callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId))

        return delete(requestDetails, callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId))

        return replace(StoredProcedure(storedProcedureId, procedure), requestDetails, callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId))

        return replace(StoredProcedure(storedProcedureId, procedure), requestDetails, callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, partitionKey: String? = null, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId), partitionKey)

        return execute(requestDetails, parameters, callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, partitionKey: String? = null, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId), partitionKey)

        return execute(requestDetails, parameters, callback)
    }

    //endregion

    //region User Defined Functions

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Udf(databaseId, collectionId))

        return create(UserDefinedFunction(userDefinedFunctionId, functionBody), requestDetails, callback)
    }

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Udf, collection))

        return create(UserDefinedFunction(userDefinedFunctionId, functionBody), requestDetails, callback)
    }

    // list
    fun getUserDefinedFunctions(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<UserDefinedFunction>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Udf(databaseId, collectionId))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // list
    fun getUserDefinedFunctions(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<UserDefinedFunction>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Udf, collection))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Udf(databaseId, collectionId, userDefinedFunctionId))

        return delete(requestDetails, callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Udf, collection, userDefinedFunctionId))

        return delete(requestDetails, callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Udf(databaseId, collectionId, userDefinedFunctionId))

        return replace(UserDefinedFunction(userDefinedFunctionId, function), requestDetails, callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Udf, collection, userDefinedFunctionId))

        return replace(UserDefinedFunction(userDefinedFunctionId, function), requestDetails, callback)
    }

    //endregion

    //region Triggers

    // create
    fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (Response<Trigger>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Trigger(databaseId, collectionId))

        return create(Trigger(triggerId, triggerBody, operation, triggerType), requestDetails, callback)
    }

    // create
    fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (Response<Trigger>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Trigger, collection))

        return create(Trigger(triggerId, triggerBody, operation, triggerType), requestDetails, callback)
    }

    // list
    fun getTriggers(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<Trigger>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Trigger(databaseId, collectionId))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // list
    fun getTriggers(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<Trigger>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Trigger, collection))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // delete
    fun deleteTrigger(triggerId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Trigger(databaseId, collectionId, triggerId))

        return delete(requestDetails, callback)
    }

    // delete
    fun deleteTrigger(triggerId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Trigger, collection, triggerId))

        return delete(requestDetails, callback)
    }

    // replace
    fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (Response<Trigger>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Trigger(databaseId, collectionId, triggerId))

        return replace(Trigger(triggerId, triggerBody, operation, triggerType), requestDetails, callback)
    }

    // replace
    fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (Response<Trigger>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Trigger, collection, triggerId))

        return replace(Trigger(triggerId, triggerBody, operation, triggerType), requestDetails, callback)
    }

    //endregion

    //region Users

    // create
    fun createUser(userId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.User(databaseId))

        return create(User(userId), requestDetails, callback)
    }

    // list
    fun getUsers(databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<User>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.User(databaseId))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // get
    fun getUser(userId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.User(databaseId, userId))

        return resource(requestDetails, callback)
    }

    // delete
    fun deleteUser(userId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.User(databaseId, userId))

        return delete(requestDetails, callback)
    }

    // replace
    fun replaceUser(userId: String, newUserId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.User(databaseId, userId))

        return replace(User(newUserId), requestDetails, callback)
    }

    //endregion

    //region Permissions

    // create
    fun createPermission(permissionId: String, permissionMode: PermissionMode, resource: Resource, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) {

        val permission = Permission(permissionId, permissionMode, resource.selfLink!!)
        val requestDetails = RequestDetails(ResourceLocation.Permission(databaseId, userId))

        return create(permission, requestDetails, callback)
    }

    // create
    fun createPermission(permissionId: String, permissionMode: PermissionMode, resource: Resource, user: User, callback: (Response<Permission>) -> Unit) {

        val permission = Permission(permissionId, permissionMode, resource.selfLink!!)
        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Permission, user))

        return create(permission, requestDetails, callback)
    }

    // list
    fun getPermissions(userId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<Permission>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Permission(databaseId, userId))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // list
    fun getPermissions(user: User, maxPerPage: Int? = null, callback: (ListResponse<Permission>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Permission, user))
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // get
    fun getPermission(permissionId: String, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Permission(databaseId, userId, permissionId))

        return resource(requestDetails, callback)
    }

    // get
    fun getPermission(permissionId: String, user: User, callback: (Response<Permission>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Permission, user, permissionId))

        return resource(requestDetails, callback)
    }

    // delete
    fun deletePermission(permissionId: String, userId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Permission(databaseId, userId, permissionId))

        return delete(requestDetails, callback)
    }

    // delete
    fun deletePermission(permissionId: String, user: User, callback: (DataResponse) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Permission, user, permissionId))

        return delete(requestDetails, callback)
    }

    // replace
    fun replacePermission(permissionId: String, permissionMode: PermissionMode, resourceSelfLink: String, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Permission(databaseId, userId, permissionId))

        return replace(Permission(permissionId, permissionMode, resourceSelfLink), requestDetails, callback)
    }

    // replace
    fun replacePermission(permissionId: String, permissionMode: PermissionMode, resourceSelfLink: String, user: User, callback: (Response<Permission>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Child(ResourceType.Permission, user, permissionId))

        return replace(Permission(permissionId, permissionMode, resourceSelfLink), requestDetails, callback)
    }

    //endregion

    //region Offers

    // list
    fun getOffers(maxPerPage: Int? = null, callback: (ListResponse<Offer>) -> Unit) {

        val requestDetails = RequestDetails(ResourceLocation.Offer())
        requestDetails.maxPerPage = maxPerPage

        return resources(requestDetails, callback)
    }

    // get
    fun getOffer(offerId: String, callback: (Response<Offer>) -> Unit): Any {

        return resource(RequestDetails(ResourceLocation.Offer(offerId)), callback)
    }

    //endregion

    //region Resource operations

    // create
    private fun <T : Resource> create(resource: T, requestDetails: RequestDetails, callback: (Response<T>) -> Unit) {

        if (!resource.hasValidId()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        createOrReplace(resource, requestDetails, false, callback)
    }

    // list
    private fun <T : Resource> resources(requestDetails: RequestDetails, callback: (ListResponse<T>) -> Unit) {

        requestDetails.method = HttpMethod.Get

        createRequest(requestDetails) { request ->

            sendResourceListRequest<T>(
                    request,
                    requestDetails,
                    callback = { response ->
                        processResourceListResponse(
                                requestDetails,
                                response,
                                callback
                        )
                    }
            )
        }
    }

    // get
    private fun <T : Resource> resource(requestDetails: RequestDetails, callback: (Response<T>) -> Unit) {

        requestDetails.method = HttpMethod.Get

        createRequest(requestDetails) { request ->

            sendResourceRequest<T>(
                    request,
                    requestDetails,
                    callback = { response ->
                        processResourceGetResponse(
                                requestDetails,
                                response,
                                callback
                        )
                    }
            )
        }
    }

    // refresh
    fun <T : Resource> refresh(resource: T, partitionKey: String? = null, callback: (Response<T>) -> Unit) {

        return try {

            val requestDetails = RequestDetails.fromResource(resource)
            requestDetails.method = HttpMethod.Get

            //look for partition key property(ies) to send for this resource type
            requestDetails.headers = setPartitionKeyHeader(partitionKey)
                    ?: setResourcePartitionKey(resource) ?: mutableMapOf()

            // if we have an eTag, we'll set & send the IfNoneMatch header
            if (!resource.etag.isNullOrEmpty()) {

                requestDetails.addHeader(HttpHeader.IfNoneMatch.value, resource.etag!!)
            }

            createRequest(requestDetails) { request ->

                //send the request!
                sendResourceRequest(request, requestDetails, resource, callback)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // delete
    private fun delete(requestDetails: RequestDetails, callback: (DataResponse) -> Unit) {

        requestDetails.method = HttpMethod.Delete

        createRequest(requestDetails) { request ->

            sendRequest(
                    request,
                    requestDetails,
                    callback = { response: DataResponse ->
                        processDeleteResponse(
                                requestDetails,
                                response,
                                callback
                        )
                    }
            )
        }
    }

    fun <TResource : Resource> delete(resource: TResource, partitionKey: String? = null, callback: (DataResponse) -> Unit) {

        return try {

            val requestDetails = RequestDetails.fromResource(resource, partitionKey)

            //look for partition key property(ies) to send for this resource type
            requestDetails.headers = setPartitionKeyHeader(partitionKey)
                    ?: setResourcePartitionKey(resource) ?: mutableMapOf()

            delete(requestDetails, callback)

        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // replace
    private fun <T : Resource> replace(resource: T, requestDetails: RequestDetails, callback: (Response<T>) -> Unit) {

        if (!resource.hasValidId()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        createOrReplace(resource, requestDetails, true, callback)
    }

    // create or replace
    internal fun <T : Resource> createOrReplace(body: T, requestDetails: RequestDetails, replacing: Boolean = false, callback: (Response<T>) -> Unit) {

        try {
            requestDetails.method = if (replacing) HttpMethod.Put else HttpMethod.Post
            //serialize the resource
            requestDetails.body = gson.toJson(body).toByteArray()
            requestDetails.resourceType = body::class.java
            //look for partition key property(ies) to send for this resource type
            requestDetails.headers = setResourcePartitionKey(body, requestDetails.headers)

            createRequest(requestDetails) { request ->

                sendResourceRequest(
                        request,
                        requestDetails,
                        callback = { response: Response<T> ->
                            processCreateOrReplaceResponse(
                                    body,
                                    requestDetails,
                                    replacing,
                                    response,
                                    callback
                            )
                        }
                )
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // create or replace
    private fun <T : Resource> createOrReplace(requestDetails: RequestDetails, replacing: Boolean = false, callback: (Response<T>) -> Unit) {

        try {
            requestDetails.method = if (replacing) HttpMethod.Put else HttpMethod.Post

            createRequest(requestDetails) { request ->

                sendResourceRequest(request, requestDetails, callback)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // query
    private fun <T : Resource> query(query: Query, requestDetails: RequestDetails, callback: (ListResponse<T>) -> Unit) {

        try {
            // do we have a partition key to send?  If not, then this query will be a cross partition query
            requestDetails.headers = requestDetails.partitionKey?.let { setPartitionKeyHeader(requestDetails.partitionKey) }
                    ?: mutableMapOf(MSHttpHeader.MSDocumentDBQueryEnableCrossPartition.value to HttpHeaderValue.trueValue)

            // need to set the special query headers
            requestDetails.addHeader(MSHttpHeader.MSDocumentDBIsQuery.value, HttpHeaderValue.trueValue)
            requestDetails.addHeader(HttpHeader.ContentType.value, HttpMediaType.QueryJson.value)

            requestDetails.method = HttpMethod.Post
            requestDetails.body = gson.toJson(query.dictionary).toByteArray()

            createRequest(requestDetails) { request ->

                sendResourceListRequest<T>(request, requestDetails) { response ->

                    processQueryResponse(query, requestDetails, response) { processedResponse ->

                        if (processedResponse.isSuccessful) { // success case

                            callback(processedResponse)

                        } else if (processedResponse.isErrored && processedResponse.error!!.isInvalidCrossPartitionQueryError()) {

                            // if we've tried to query cross partition but have a TOP or ORDER BY, we'll get a specific error we can work around by using partition key range Ids
                            // reference: https://stackoverflow.com/questions/50240232/cosmos-db-rest-api-order-by-with-partitioning

                            // we will grab the partition key ranges for the collection we're querying
                            val dbId = requestDetails.resourceLocation.ancestorIds().getValue(ResourceType.Database)
                            val collId = requestDetails.resourceLocation.ancestorIds().getValue(ResourceType.Collection)

                            getCollectionPartitionKeyRanges(collId, dbId) { pkRanges ->

                                // THEN, we can retry our request after setting the range Id header
                                // TODO:  Note, we may need to do more here if there are additional PartitionKeyRange items that come back... can't find docs on this format
                                requestDetails.addHeader(MSHttpHeader.MSDocumentDBPartitionKeyRangeId.value, "${pkRanges.resource!!.resourceId!!},${pkRanges.resource.items[0].id}")

                                createRequest(requestDetails) { retryRequest ->

                                    sendResourceListRequest<T>(retryRequest, requestDetails) { retryResponse ->

                                        processQueryResponse(query, requestDetails, retryResponse) {
                                            // DO NOT try to inline this into the method call above.  Bad. Things. Happen.
                                            callback(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            e(ex)
            callback(ListResponse(DataError(ex)))
        }
    }

    // next
    internal fun <T : Resource> next(response : ListResponse<T>, callback: (ListResponse<T>) -> Unit) {

        try {
            val request = response.request
                ?: return callback(ListResponse(DataError(DocumentClientError.NextCalledTooEarlyError)))

            val continuation = response.metadata.continuation
                    ?: return callback(ListResponse(DataError(DocumentClientError.NoMoreResultsError)))

            val resourceLocation = response.resourceLocation
                ?: return callback(ListResponse(DataError(DocumentClientError.NextCalledTooEarlyError)))

            val resourceType = response.resourceType
                    ?: return callback(ListResponse(DataError(DocumentClientError.NextCalledTooEarlyError)))

            val requestDetails = RequestDetails(resourceLocation)
            requestDetails.resourceType = resourceType

            val newRequest = request.newBuilder()
                    .header(MSHttpHeader.MSContinuation.value, continuation)
                    .build()

            client.newCall(newRequest)
                    .enqueue(object : Callback {

                        // only transport errors handled here
                        override fun onFailure(call: Call, e: IOException) {
                            isOffline = true
                            // todo: callback with cached data instead of the callback with the error below
                            callback(ListResponse(DataError(e)))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, resp: okhttp3.Response)  =
                                callback(processListResponse(request, resp, requestDetails))

                    })
        } catch (ex: Exception) {
            e(ex)
            callback(ListResponse(DataError(ex)))
        }
    }

    // execute
    private fun <T> execute(requestDetails: RequestDetails, body: T? = null, callback: (DataResponse) -> Unit) {

        try {
            requestDetails.method = HttpMethod.Post
            requestDetails.headers = setPartitionKeyHeader(requestDetails.partitionKey)
            requestDetails.body = body?.let { gson.toJson(body).toByteArray() } ?: gson.toJson(arrayOf<String>()).toByteArray()

            createRequest(requestDetails) { request ->

                sendRequest(request, requestDetails, callback)
            }

        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    //endregion

    //region Network plumbing

    private val dateFormatter : SimpleDateFormat by lazy {
        DateUtil.getDateFromatter(DateUtil.Format.Rfc1123Format)
    }

    private fun setPartitionKeyHeader(partitionKey: List<String>?, headers: MutableMap<String, String>? = null) : MutableMap<String, String>? {

        return if (!partitionKey.isNullOrEmpty()) {

            //send the partition key(s) in the form of header: x-ms-documentdb-partitionkey: ["<My Partition Key>"]
            val partitionKeyValue = partitionKey.joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")

            if (headers != null) {
                headers[MSHttpHeader.MSDocumentDBPartitionKey.value] = partitionKeyValue
                headers
            } else {
                mutableMapOf(MSHttpHeader.MSDocumentDBPartitionKey.value to partitionKeyValue)
            }
        } else headers
    }

    private fun setPartitionKeyHeader(partitionKey: String?, headers: MutableMap<String, String>? = null) : MutableMap<String, String>? {

        return if (partitionKey != null) {

            setPartitionKeyHeader(listOf(partitionKey), headers)

        } else headers
    }

    private fun <T : Resource> setResourcePartitionKey(resource: T, headers: MutableMap<String, String>? = null) : MutableMap<String, String>? {

        return if (resource is PartitionKeyResource && headers?.contains(MSHttpHeader.MSDocumentDBPartitionKey.value) != true) {

            val keyValues = PartitionKeyPropertyCache.getPartitionKeyValues(resource)

            return setPartitionKeyHeader(keyValues, headers)

        } else headers
    }

    private inline fun getTokenForResource(requestDetails: RequestDetails, crossinline callback: (Response<ResourceToken>) -> Unit) {

        if (!isConfigured) {
            return callback(Response(DataError(DocumentClientError.ConfigureError)))
        }

        if (requestDetails.resourceLocation.id?.isValidIdForResource() == false) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        if (resourceTokenProvider != null) {

            resourceTokenProvider!!.getToken(requestDetails.resourceLocation, requestDetails.method)?.let {
                return callback(Response(it))
            }
        } else {

            if (!requestDetails.resourceLocation.supportsPermissionToken) {
                return callback(Response(DataError(DocumentClientError.PermissionError)))
            }

            return permissionProvider?.getPermission(requestDetails.resourceLocation, if (requestDetails.method.isWrite()) PermissionMode.All else PermissionMode.Read) {

                if (it.isSuccessful) {

                    val dateString = String.format("%s %s", dateFormatter.format(Date()), "GMT")

                    it.resource?.token?.let { token ->

                        callback(Response(ResourceToken(token.urlEncode(), dateString)))

                    } ?: callback(Response(DataError(DocumentClientError.PermissionError)))
                } else {
                    callback(Response(it.error!!))
                }
            } ?: callback(Response(DataError(DocumentClientError.UnknownError)))
        }

        return callback(Response(DataError(DocumentClientError.UnknownError)))
    }

    private inline fun createRequest(requestDetails: RequestDetails, crossinline callback: (Request) -> Unit) {

        getTokenForResource(requestDetails) {

            when {
                it.isSuccessful -> it.resource?.let { token ->

                    val url = HttpUrl.Builder()
                            .scheme(HttpScheme.Https.toString())
                            .host(this.host!!)
                            .addPathSegment(requestDetails.resourceLocation.path())
                            .build()

                    val headersBuilder = Headers.Builder()
                    // add base headers
                    headersBuilder.addAll(defaultHeaders)

                    // set the api version
                    headersBuilder.add(MSHttpHeader.MSVersion.value, HttpHeaderValue.apiVersion)
                    // and the token data
                    headersBuilder.add(MSHttpHeader.MSDate.value, token.date)
                    headersBuilder.add(HttpHeader.Authorization.value, token.token)

                    // add the count
                    requestDetails.maxPerPage?.let { max ->
                        if ((1..1000).contains(max)) {
                            headersBuilder.add(MSHttpHeader.MSMaxItemCount.value, max.toString())
                        } else {
                            throw DocumentClientError.InvalidMaxPerPageError
                        }
                    }

                    // if we have additional headers, let's add them in here
                    requestDetails.headers?.let { headers ->
                        for (headerName in headers.keys) {
                            headersBuilder.add(headerName, headers[headerName]!!)
                        }
                    }

                    val builder = Request.Builder()
                            .headers(headersBuilder.build())
                            .url(url)

                    var mediaType = jsonMediaType

                    if (requestDetails.body != null) {

                        //  !NOTE!: we only accept a ByteArray body on RequestDetails due to a feature/bug in OkHttp that will tack on
                        //  a charset string that does not work well with certain operations (Query!) when converting a json body
                        //  A json body can be used by calling json.toByteArray() first

                        // do we have a content type set in our headers?  If so, we'll use this for the body
                        requestDetails.headers?.get(HttpHeader.ContentType.value)?.let { contentType ->

                            mediaType = MediaType.parse(contentType)

                        } ?: run {
                            // otherwise, default to json & set the content type in our headers

                            builder.addHeader(HttpHeader.ContentType.value, HttpMediaType.Json.value)
                        }
                    }

                    callback(builder.withMethod(requestDetails.method, requestDetails.body.toRequestBody(mediaType)).build())

                } ?: throw DocumentClientError.UnknownError

                it.isErrored -> throw it.error!!

                else -> throw DocumentClientError.UnknownError
            }
        }
    }

    private inline fun <T : Resource> sendResourceRequest(request: Request, requestDetails: RequestDetails, crossinline callback: (Response<T>) -> Unit)
            = sendResourceRequest(request, requestDetails, null, callback)

    private inline fun <T : Resource> sendResourceRequest(request: Request, requestDetails: RequestDetails, resource: T?, crossinline callback: (Response<T>) -> Unit) {

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, ex: IOException) {
                            e(ex)
                            isOffline = true

                            callback(Response(DataError(DocumentClientError.InternetConnectivityError), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processResponse(request, response, requestDetails, resource))
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex), request))
        }
    }

    private inline fun sendRequest(request: Request, requestDetails: RequestDetails, crossinline callback: (DataResponse) -> Unit) {

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, ex: IOException) {
                            e(ex)
                            isOffline = true

                            return callback(Response(DataError(DocumentClientError.InternetConnectivityError), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processDataResponse(request, requestDetails.resourceLocation, response))
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex), request))
        }
    }

    private inline fun <T : Resource> sendResourceListRequest(request: Request, requestDetails: RequestDetails, crossinline callback: (ListResponse<T>) -> Unit) {

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        // only transport errors handled here
                        override fun onFailure(call: Call, ex: IOException) {
                            e(ex)
                            isOffline = true

                            callback(ListResponse(DataError(DocumentClientError.InternetConnectivityError), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processListResponse(request, response, requestDetails))
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(ListResponse(DataError(ex), request))
        }
    }

    private fun <T : Resource> processResponse(request: Request, response: okhttp3.Response, requestDetails: RequestDetails, resource: T?): Response<T> {

        try {
            val body = response.body()
                    ?: return Response(DataError("Empty response body received"))
            val json = body.string()

            //check http return code/success
            when {
            // HttpStatusCode.Created: // cache locally
            // HttpStatusCode.NoContent: // DELETEing a resource remotely should delete the cached version (if the delete was successful indicated by a response status code of 204 No Content)
            // HttpStatusCode.Unauthorized:
            // HttpStatusCode.Forbidden: // reauth
            // HttpStatusCode.Conflict: // conflict callback
            // HttpStatusCode.NotFound: // (indicating the resource has been deleted/no longer exists in the remote database), confirm that resource does not exist locally, and if it does, delete it
            // HttpStatusCode.PreconditionFailure: // The operation specified an eTag that is different from the version available at the server, that is, an optimistic concurrency error. Retry the request after reading the latest version of the resource and updating the eTag on the request.

                response.isSuccessful -> {

                    val type = requestDetails.resourceType ?: resource?.javaClass ?: requestDetails.resourceLocation.resourceType.type
                    val returnedResource = gson.fromJson<T>(json, type)
                            ?: return Response(json.toError())

                    setResourceMetadata(response, returnedResource, requestDetails.resourceLocation.resourceType)

                    return Response(request, response, json, Result(returnedResource))
                }

                response.code() == HttpStatusCode.NotModified.code -> {

                    resource?.let {
                        setResourceMetadata(response, it, requestDetails.resourceLocation.resourceType)
                    }

                    //return the original resource
                    return Response(request, response, json, Result(resource))
                }

                else -> return Response(json.toError(), request, response, json)
            }
        } catch (e: Exception) {
            return Response(DataError(e), request, response)
        }
    }

    private fun <T : Resource> processListResponse(request: Request, response: okhttp3.Response, requestDetails: RequestDetails): ListResponse<T> {

        return try {
            val body = response.body()
                    ?: return ListResponse(DataError("Empty response body received"), request, response)
            val json = body.string()

            if (response.isSuccessful) {

                val type = requestDetails.resourceType ?: requestDetails.resourceLocation.resourceType.type
                val resourceList = ResourceListJsonDeserializer<T>().deserialize(json, type)

                setResourceMetadata(response, resourceList, requestDetails.resourceLocation.resourceType)

                ResourceCache.shared.cache(resourceList)

                ListResponse(request, response, json, Result(resourceList), requestDetails.resourceLocation, type)
            } else {
                ListResponse(json.toError(), request, response, json)
            }
        } catch (e: Exception) {
            ListResponse(DataError(e), request, response)
        }
    }

    private fun <T : Resource> processCreateOrReplaceResponse(resource: T, requestDetails: RequestDetails, replace: Boolean, response: Response<T>, callback: (Response<T>) -> Unit) {

        when {
            response.isSuccessful -> {

                callback(response)

                when (replace) {
                    true -> response.resource?.let { ResourceCache.shared.replace(it) }
                    false -> response.resource?.let { ResourceCache.shared.cache(it) }
                }
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError()) {

                    ResourceWriteOperationQueue.shared.addCreateOrReplace(resource, requestDetails.resourceLocation, requestDetails.headers, replace, callback)
                    return
                }

                callback(response)
            }

            else -> {
                callback(response)
            }
        }
    }

    private fun <T : Resource> processResourceGetResponse(requestDetails: RequestDetails, response: Response<T>, callback: (Response<T>) -> Unit) {

        when {
            response.isSuccessful -> {

                callback(response)

                response.resource?.let { ResourceCache.shared.cache(it) }
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError())  {
                    cachedResource(requestDetails, response, callback)
                    return
                }

                if (response.is404()) {
                    ResourceCache.shared.remove(requestDetails.resourceLocation)
                }

                callback(response)
            }

            else -> { callback(response) }
        }
    }

    private fun <T : Resource> processResourceListResponse(requestDetails: RequestDetails, response: ListResponse<T>, callback: (ListResponse<T>) -> Unit) {

        when {
            response.isSuccessful -> {

                callback(response)

                response.resource?.let { ResourceCache.shared.cache(it) }
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError()) {
                    cachedResources(requestDetails, response, callback)
                    return
                }

                callback(response)
            }

            else -> { callback(response) }
        }
    }

    private fun <T : Resource> processQueryResponse(query: Query, requestDetails: RequestDetails, response: ListResponse<T>, callback: (ListResponse<T>) -> Unit) {

        when {
            response.isSuccessful -> {

                callback(response)

                response.resource?.let { ResourceCache.shared.cache(it, query, requestDetails.resourceLocation.link()) }
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError()) {
                    cachedResources(query, requestDetails, response, callback)
                    return
                }

                callback(response)
            }

            else -> { callback(response) }
        }
    }

    private fun processDeleteResponse(requestDetails: RequestDetails, response: DataResponse, callback: (DataResponse) -> Unit) {

        when {
            response.isSuccessful -> {

                callback(response)

                ResourceCache.shared.remove(requestDetails.resourceLocation)
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError()) {
                    ResourceWriteOperationQueue.shared.addDelete(requestDetails.resourceLocation, requestDetails.headers, callback)
                    return
                }

                callback(response)
            }
        }
    }

    private fun setResourceMetadata(response: okhttp3.Response, resource: ResourceBase, resourceType: ResourceType) {

        //grab & store alt Link and persist alt link <-> self link mapping
        val altContentPath = response.header(MSHttpHeader.MSAltContentPath.value, null)
        resource.setAltContentLink(resourceType.path, altContentPath)
        ResourceOracle.shared.storeLinks(resource)
    }

    private fun processDataResponse(request: Request, resourceLocation: ResourceLocation, response: okhttp3.Response): DataResponse {

        try {
            val body = response.body()
                    ?: return Response(DataError("Empty response body received"), request, response)
            val responseBodyString = body.string()

            //check http return code
            return if (response.isSuccessful) {

                if (request.method() == HttpMethod.Delete.toString()) {
                    ResourceCache.shared.remove(resourceLocation)
                }

                DataResponse(request, response, responseBodyString, Result(responseBodyString))
            } else {
                Response(responseBodyString.toError(), request, response, responseBodyString)
            }
        } catch (e: Exception) {
            return Response(DataError(e), request, response)
        }
    }

    //endregion

    //region Cache Responses

    private fun <T : Resource> cachedResource(requestDetails: RequestDetails, response: Response<T>? = null, callback: (Response<T>) -> Unit) {

        val type = requestDetails.resourceType ?: requestDetails.resourceLocation.resourceType.type

        return ResourceCache.shared.getResourceAt<T>(requestDetails.resourceLocation, type)?.let { resource ->

            callback(Response(response?.request, response?.response, response?.jsonData, Result(resource), requestDetails.resourceLocation, response?.resourceType, true))

        } ?: callback(Response(DataError(DocumentClientError.NotFound)))
    }

    private fun <T : Resource> cachedResources(requestDetails: RequestDetails, response: ListResponse<T>? = null, callback: (ListResponse<T>) -> Unit) {

        val type = requestDetails.resourceType ?: requestDetails.resourceLocation.resourceType.type

        return ResourceCache.shared.getResourcesAt<T>(requestDetails.resourceLocation, type)?.let { resources ->

            callback(ListResponse(response?.request, response?.response, response?.jsonData, Result(resources), requestDetails.resourceLocation, response?.resourceType, true))

        } ?: callback(ListResponse(DataError(DocumentClientError.SerciceUnavailableError)))
    }

    private fun <T : Resource> cachedResources(query: Query, requestDetails: RequestDetails, response: ListResponse<T>? = null, callback: (ListResponse<T>) -> Unit) {

        val type = requestDetails.resourceType ?: requestDetails.resourceLocation.resourceType.type

        return ResourceCache.shared.getResourcesForQuery<T>(query, type)?.let { resources ->

            callback(ListResponse(response?.request, response?.response, response?.jsonData, Result(resources), requestDetails.resourceLocation, response?.resourceType, true))

        } ?: callback(ListResponse(DataError(DocumentClientError.SerciceUnavailableError)))
    }

    //endregion

    companion object {

        val shared = DocumentClient()

        lateinit var client: OkHttpClient

        val jsonMediaType = MediaType.parse(HttpMediaType.Json.value)
    }
}