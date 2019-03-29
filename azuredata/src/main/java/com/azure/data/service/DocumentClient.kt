package com.azure.data.service

import com.azure.core.http.HttpHeader
import com.azure.core.http.HttpMediaType
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpStatusCode
import com.azure.core.log.configureNetworkLogging
import com.azure.core.log.d
import com.azure.core.log.e
import com.azure.core.network.NetworkConnectivity
import com.azure.core.network.NetworkConnectivityManager
import com.azure.core.util.ContextProvider
import com.azure.core.util.DateUtil
import com.azure.data.constants.HttpHeaderValue
import com.azure.data.constants.MSHttpHeader
import com.azure.data.model.*
import com.azure.data.model.indexing.IndexingPolicy
import com.azure.data.model.partition.PartitionKeyRange
import com.azure.data.model.partition.PartitionKeyResource
import com.azure.data.util.*
import com.azure.data.util.json.ResourceListJsonDeserializer
import com.azure.data.util.json.gson
import getDefaultHeaders
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type
import java.net.URL
import java.net.URLEncoder
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

        return throughput?.let {

            if (!it.isValidThroughput()) {
                return callback(Response(DataError(DocumentClientError.InvalidThroughputError)))
            }

            create(Database(databaseId), ResourceLocation.Database(), mutableMapOf(MSHttpHeader.MSOfferThroughput.value to "$it"), callback = callback)
        } ?: create(Database(databaseId), ResourceLocation.Database(), callback = callback)
    }

    // list
    fun getDatabases(maxPerPage: Int? = null, callback: (ListResponse<Database>) -> Unit) {

        return resources(ResourceLocation.Database(), Database::class.java, maxPerPage, null, callback)
    }

    // get
    fun getDatabase(databaseId: String, callback: (Response<Database>) -> Unit) {

        return resource(ResourceLocation.Database(databaseId), null, Database::class.java, callback)
    }

    // delete
    fun deleteDatabase(databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Database(databaseId), null, callback)
    }

    //endregion

    //region Collections

    // create
    @Deprecated("Creating a collection without a partition key is deprecated and will be removed in a future version of AzureData")
    fun createCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        return create(DocumentCollection(collectionId), ResourceLocation.Collection(databaseId), callback = callback)
    }

    // create
    fun createCollection(collectionId: String, throughput: Int? = null, partitionKey: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        return throughput?.let {

            if (!it.isValidThroughput()) {
                return callback(Response(DataError(DocumentClientError.InvalidThroughputError)))
            }

            create(DocumentCollection(collectionId, partitionKey), ResourceLocation.Collection(databaseId), mutableMapOf(MSHttpHeader.MSOfferThroughput.value to "$it"), callback = callback)
        } ?: create(DocumentCollection(collectionId, partitionKey), ResourceLocation.Collection(databaseId), callback = callback)
    }

    // list
    fun getCollectionsIn(databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<DocumentCollection>) -> Unit) {

        return resources(ResourceLocation.Collection(databaseId), DocumentCollection::class.java, maxPerPage, null, callback)
    }

    // get
    fun getCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        return resource(ResourceLocation.Collection(databaseId, collectionId), null, DocumentCollection::class.java, callback)
    }

    // delete
    fun deleteCollection(collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Collection(databaseId, collectionId), null, callback)
    }

    // replace
    fun replaceCollection(collection: DocumentCollection, databaseId: String, indexingPolicy: IndexingPolicy, callback: (Response<DocumentCollection>) -> Unit) {

        collection.indexingPolicy = indexingPolicy

        return replace(collection, ResourceLocation.Collection(databaseId, collection.id), callback = callback)
    }

    // get partition key ranges
    fun getCollectionPartitionKeyRanges(collectionId: String, databaseId: String, callback: (ListResponse<PartitionKeyRange>) -> Unit) {

        return resources(ResourceLocation.PkRanges(databaseId, collectionId), PartitionKeyRange::class.java, null, null, callback)
    }

    //endregion

    //region Documents

    // create
    fun <T : Document> createDocument(document: T, partitionKey: String? = null, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) {

        return create(document, ResourceLocation.Document(databaseId, collectionId), setPartitionKeyHeader(partitionKey), callback = callback)
    }

    // create
    fun <T : Document> createDocument (document: T, partitionKey: String? = null, collection: DocumentCollection, callback: (Response<T>) -> Unit) {

        return create(document, ResourceLocation.Child(ResourceType.Document, collection), setPartitionKeyHeader(partitionKey), callback = callback)
    }

    // createOrReplace
    fun <T : Document> createOrUpdateDocument(document: T, partitionKey: String? = null, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) {

        return create(document, ResourceLocation.Document(databaseId, collectionId), setPartitionKeyHeader(partitionKey, mutableMapOf(MSHttpHeader.MSDocumentDBIsUpsert.value to HttpHeaderValue.trueValue)), callback = callback)
    }

    // createOrReplace
    fun <T : Document> createOrUpdateDocument (document: T, partitionKey: String? = null, collection: DocumentCollection, callback: (Response<T>) -> Unit) {

        return create(document, ResourceLocation.Child(ResourceType.Document, collection), setPartitionKeyHeader(partitionKey, mutableMapOf(MSHttpHeader.MSDocumentDBIsUpsert.value to HttpHeaderValue.trueValue)), callback = callback)
    }

    // list
    fun <T : Document> getDocumentsAs(collectionId: String, databaseId: String, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        return resources(ResourceLocation.Document(databaseId, collectionId), documentClass, maxPerPage, null, callback)
    }

    // list
    fun <T : Document> getDocumentsAs(collection: DocumentCollection, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Document, collection), documentClass, maxPerPage, null, callback)
    }

    // get
    fun <T : Document> getDocument(documentId: String, partitionKey: String?, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (Response<T>) -> Unit) {

        return resource(ResourceLocation.Document(databaseId, collectionId, documentId), setPartitionKeyHeader(partitionKey), documentClass, callback)
    }

    // get
    fun <T : Document> getDocument(documentId: String, partitionKey: String?, collection: DocumentCollection, documentClass: Class<T>, callback: (Response<T>) -> Unit) {

        return resource(ResourceLocation.Child(ResourceType.Document, collection, documentId), setPartitionKeyHeader(partitionKey), documentClass, callback)
    }

    // delete
    @Deprecated("Deleting a document without a partition key is deprecated and will be removed in a future version of AzureData")
    fun deleteDocument(documentId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Document(databaseId, collectionId, documentId), null, callback)
    }

    // delete
    @Deprecated("Deleting a document without a partition key is deprecated and will be removed in a future version of AzureData")
    fun deleteDocument(documentId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Document, collection, documentId), null, callback)
    }

    // delete
    fun deleteDocument(documentId: String, partitionKey: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Document(databaseId, collectionId, documentId), setPartitionKeyHeader(partitionKey), callback)
    }

    // delete
    fun deleteDocument(documentId: String, partitionKey: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Document, collection, documentId), setPartitionKeyHeader(partitionKey), callback)
    }

    // replace
    fun <T : Document> replaceDocument(document: T, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) {

        return replace(document, ResourceLocation.Document(databaseId, collectionId, document.id), callback = callback)
    }

    // replace
    fun <T : Document> replaceDocument(document: T, collection: DocumentCollection, callback: (Response<T>) -> Unit) {

        return replace(document, ResourceLocation.Child(ResourceType.Document, collection, document.id), callback = callback)
    }

    // query
    fun <T : Document> queryDocuments (collectionId: String, databaseId: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        return query(query, ResourceLocation.Document(databaseId, collectionId), maxPerPage, null, documentClass, callback)
    }

    // query
    fun <T : Document> queryDocuments (collectionId: String, partitionKey: String, databaseId: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        return query(query, ResourceLocation.Document(databaseId, collectionId), maxPerPage, partitionKey, documentClass, callback)
    }

    // query
    fun <T : Document> queryDocuments (collection: DocumentCollection, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        return query(query, ResourceLocation.Child(ResourceType.Document, collection), maxPerPage, null, documentClass, callback)
    }

    // query
    fun <T : Document> queryDocuments (collection: DocumentCollection, partitionKey: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {

        return query(query, ResourceLocation.Child(ResourceType.Document, collection), maxPerPage, partitionKey, documentClass, callback)
    }

    // get/query a single doc
    fun <T : Document> findDocument(documentId: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        // create query
        val query = Query.select()
                .from(collectionId)
                .where(Resource.Companion.Keys.idKey, documentId)

        return query(query, ResourceLocation.Document(databaseId, collectionId), 1, null, documentClass, callback)
    }

    // get/query a single doc
    fun <T : Document> findDocument(documentId: String, collection: DocumentCollection, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        // create query
        val query = Query.select()
                .from(collection.id)
                .where(Resource.Companion.Keys.idKey, documentId)

        return query(query, ResourceLocation.Child(ResourceType.Document, collection), 1, null, documentClass, callback)
    }

    //endregion

    //region Attachments

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Attachment(databaseId, collectionId, documentId), setPartitionKeyHeader(partitionKey), callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        val headers = mutableMapOf(HttpHeader.ContentType.value to contentType, HttpHeader.Slug.value to attachmentId)

        return createOrReplace(media, ResourceLocation.Attachment(databaseId, collectionId, documentId), false, setPartitionKeyHeader(partitionKey, headers), callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) {

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Child(ResourceType.Attachment, document), setResourcePartitionKey(document), callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) {

        val headers = mutableMapOf(HttpHeader.ContentType.value to contentType, HttpHeader.Slug.value to attachmentId)

        return createOrReplace(media, ResourceLocation.Child(ResourceType.Attachment, document), false, setResourcePartitionKey(document, headers), callback)
    }

    // list
    fun getAttachments(documentId: String, collectionId: String, databaseId: String, partitionKey: String, maxPerPage: Int? = null, callback: (ListResponse<Attachment>) -> Unit) {

        return resources(ResourceLocation.Attachment(databaseId, collectionId, documentId), Attachment::class.java, maxPerPage, setPartitionKeyHeader(partitionKey), callback)
    }

    // list
    fun getAttachments(document: Document, maxPerPage: Int? = null, callback: (ListResponse<Attachment>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Attachment, document), Attachment::class.java, maxPerPage, setResourcePartitionKey(document), callback)
    }

    // delete
    fun deleteAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId), setPartitionKeyHeader(partitionKey), callback)
    }

    // delete
    fun deleteAttachment(attachmentId: String, document: Document, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Attachment, document, attachmentId), setResourcePartitionKey(document), callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId), setPartitionKeyHeader(partitionKey), callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) {

        val headers = mutableMapOf(HttpHeader.ContentType.value to contentType, HttpHeader.Slug.value to attachmentId)

        return createOrReplace(media, ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId), true, setPartitionKeyHeader(partitionKey, headers), callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) {

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Child(ResourceType.Attachment, document, attachmentId), setResourcePartitionKey(document), callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) {

        val headers = mutableMapOf(HttpHeader.ContentType.value to contentType, HttpHeader.Slug.value to attachmentId)

        return createOrReplace(media, ResourceLocation.Child(ResourceType.Attachment, document, attachmentId), true, setResourcePartitionKey(document, headers), callback)
    }

    //endregion

    //region Stored Procedures

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) {

        return create(StoredProcedure(storedProcedureId, procedure), ResourceLocation.StoredProcedure(databaseId, collectionId), callback = callback)
    }

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) {

        return create(StoredProcedure(storedProcedureId, procedure), ResourceLocation.Child(ResourceType.StoredProcedure, collection), callback = callback)
    }

    // list
    fun getStoredProcedures(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<StoredProcedure>) -> Unit) {

        return resources(ResourceLocation.StoredProcedure(databaseId, collectionId), StoredProcedure::class.java, maxPerPage,null, callback)
    }

    // list
    fun getStoredProcedures(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<StoredProcedure>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.StoredProcedure, collection), StoredProcedure::class.java, maxPerPage, null, callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId), null, callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId), null, callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) {

        return replace(StoredProcedure(storedProcedureId, procedure), ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId), callback = callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) {

        return replace(StoredProcedure(storedProcedureId, procedure), ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId), callback = callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, partitionKey: String? = null, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return execute(parameters, ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId), partitionKey, callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, partitionKey: String? = null, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return execute(parameters, ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId), partitionKey, callback)
    }

    //endregion

    //region User Defined Functions

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) {

        return create(UserDefinedFunction(userDefinedFunctionId, functionBody), ResourceLocation.Udf(databaseId, collectionId), callback = callback)
    }

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) {

        return create(UserDefinedFunction(userDefinedFunctionId, functionBody), ResourceLocation.Child(ResourceType.Udf, collection), callback = callback)
    }

    // list
    fun getUserDefinedFunctions(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<UserDefinedFunction>) -> Unit) {

        return resources(ResourceLocation.Udf(databaseId, collectionId), UserDefinedFunction::class.java, maxPerPage, null, callback)
    }

    // list
    fun getUserDefinedFunctions(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<UserDefinedFunction>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Udf, collection), UserDefinedFunction::class.java, maxPerPage, null, callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Udf(databaseId, collectionId, userDefinedFunctionId), null, callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Udf, collection, userDefinedFunctionId), null, callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) {

        return replace(UserDefinedFunction(userDefinedFunctionId, function), ResourceLocation.Udf(databaseId, collectionId, userDefinedFunctionId), callback = callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) {

        return replace(UserDefinedFunction(userDefinedFunctionId, function), ResourceLocation.Child(ResourceType.Udf, collection, userDefinedFunctionId), callback = callback)
    }

    //endregion

    //region Triggers

    // create
    fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (Response<Trigger>) -> Unit) {

        return create(Trigger(triggerId, triggerBody, operation, triggerType), ResourceLocation.Trigger(databaseId, collectionId), callback = callback)
    }

    // create
    fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (Response<Trigger>) -> Unit) {

        return create(Trigger(triggerId, triggerBody, operation, triggerType), ResourceLocation.Child(ResourceType.Trigger, collection), callback = callback)
    }

    // list
    fun getTriggers(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<Trigger>) -> Unit) {

        return resources(ResourceLocation.Trigger(databaseId, collectionId), Trigger::class.java, maxPerPage, null, callback)
    }

    // list
    fun getTriggers(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<Trigger>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Trigger, collection), Trigger::class.java, maxPerPage, null, callback)
    }

    // delete
    fun deleteTrigger(triggerId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Trigger(databaseId, collectionId, triggerId), null, callback)
    }

    // delete
    fun deleteTrigger(triggerId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Trigger, collection, triggerId), null, callback)
    }

    // replace
    fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (Response<Trigger>) -> Unit) {

        return replace(Trigger(triggerId, triggerBody, operation, triggerType), ResourceLocation.Trigger(databaseId, collectionId, triggerId), callback = callback)
    }

    // replace
    fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (Response<Trigger>) -> Unit) {

        return replace(Trigger(triggerId, triggerBody, operation, triggerType), ResourceLocation.Child(ResourceType.Trigger, collection, triggerId), callback = callback)
    }

    //endregion

    //region Users

    // create
    fun createUser(userId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        return create(User(userId), ResourceLocation.User(databaseId), callback = callback)
    }

    // list
    fun getUsers(databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<User>) -> Unit) {

        return resources(ResourceLocation.User(databaseId), User::class.java, maxPerPage, null, callback)
    }

    // get
    fun getUser(userId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        return resource(ResourceLocation.User(databaseId, userId), null, User::class.java, callback)
    }

    // delete
    fun deleteUser(userId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.User(databaseId, userId), null, callback)
    }

    // replace
    fun replaceUser(userId: String, newUserId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        return replace(User(newUserId), ResourceLocation.User(databaseId, userId), callback = callback)
    }

    //endregion

    //region Permissions

    // create
    fun createPermission(permissionId: String, permissionMode: PermissionMode, resource: Resource, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) {

        val permission = Permission(permissionId, permissionMode, resource.selfLink!!)

        return create(permission, ResourceLocation.Permission(databaseId, userId), callback = callback)
    }

    // create
    fun createPermission(permissionId: String, permissionMode: PermissionMode, resource: Resource, user: User, callback: (Response<Permission>) -> Unit) {

        val permission = Permission(permissionId, permissionMode, resource.selfLink!!)

        return create(permission, ResourceLocation.Child(ResourceType.Permission, user), callback = callback)
    }

    // list
    fun getPermissions(userId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<Permission>) -> Unit) {

        return resources(ResourceLocation.Permission(databaseId, userId), Permission::class.java, maxPerPage, null, callback)
    }

    // list
    fun getPermissions(user: User, maxPerPage: Int? = null, callback: (ListResponse<Permission>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Permission, user), Permission::class.java, maxPerPage, null, callback)
    }

    // get
    fun getPermission(permissionId: String, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) {

        return resource(ResourceLocation.Permission(databaseId, userId, permissionId), null, Permission::class.java, callback)
    }

    // get
    fun getPermission(permissionId: String, user: User, callback: (Response<Permission>) -> Unit) {

        return resource(ResourceLocation.Child(ResourceType.Permission, user, permissionId), null, Permission::class.java, callback)
    }

    // delete
    fun deletePermission(permissionId: String, userId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Permission(databaseId, userId, permissionId), null, callback)
    }

    // delete
    fun deletePermission(permissionId: String, user: User, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Permission, user, permissionId), null, callback)
    }

    // replace
    fun replacePermission(permissionId: String, permissionMode: PermissionMode, resourceSelfLink: String, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) {

        return replace(Permission(permissionId, permissionMode, resourceSelfLink), ResourceLocation.Permission(databaseId, userId, permissionId), callback = callback)
    }

    // replace
    fun replacePermission(permissionId: String, permissionMode: PermissionMode, resourceSelfLink: String, user: User, callback: (Response<Permission>) -> Unit) {

        return replace(Permission(permissionId, permissionMode, resourceSelfLink), ResourceLocation.Child(ResourceType.Permission, user, permissionId), callback = callback)
    }

    //endregion

    //region Offers

    // list
    fun getOffers(maxPerPage: Int? = null, callback: (ListResponse<Offer>) -> Unit) {

        return resources(ResourceLocation.Offer(), Offer::class.java, maxPerPage, null, callback)
    }

    // get
    fun getOffer(offerId: String, callback: (Response<Offer>) -> Unit): Any {

        return resource(ResourceLocation.Offer(offerId), null, Offer::class.java, callback)
    }

    //endregion

    //region Resource operations

    // create
    private fun <T : Resource> create(resource: T, resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, replace: Boolean = false, callback: (Response<T>) -> Unit) {

        if (!resource.hasValidId()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        createOrReplace(resource, resourceLocation, replace, additionalHeaders, callback)
    }

    // list
    private fun <T : Resource> resources(resourceLocation: ResourceLocation, resourceClass: Class<T>? = null, maxPerPage: Int? = null, additionalHeaders: MutableMap<String, String>? = null, callback: (ListResponse<T>) -> Unit) {

        createRequest(HttpMethod.Get, resourceLocation, additionalHeaders, maxPerPage) {

            sendResourceListRequest(
                    request = it,
                    resourceLocation = resourceLocation,
                    resourceClass = resourceClass,
                    callback = { response ->
                        processResourceListResponse(
                                resourceLocation = resourceLocation,
                                response = response,
                                callback = callback,
                                resourceClass = resourceClass
                        )
                    }
            )
        }
    }

    // get
    private fun <T : Resource> resource(resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, resourceClass: Class<T>? = null, callback: (Response<T>) -> Unit) {

        createRequest(HttpMethod.Get, resourceLocation, additionalHeaders) {

            sendResourceRequest(
                    request = it,
                    resourceLocation = resourceLocation,
                    callback = { response ->
                        processResourceGetResponse(
                                resourceLocation = resourceLocation,
                                response = response,
                                callback = callback,
                                resourceClass = resourceClass
                        )
                    },
                    resourceClass = resourceClass
            )
        }
    }

    // refresh
    fun <T : Resource> refresh(resource: T, callback: (Response<T>) -> Unit) {

        return try {

            val resourceLocation = ResourceLocation.Resource(resource)

            //look for partition key property(ies) to send for this resource type
            val headers = setResourcePartitionKey(resource) ?: mutableMapOf()

            // if we have an etag, we'll set & send the IfNoneMatch header
            if (!resource.etag.isNullOrEmpty()) {

                headers[HttpHeader.IfNoneMatch.value] = resource.etag!!
            }

            createRequest(HttpMethod.Get, resourceLocation, headers) {
                //send the request!
                sendResourceRequest(it, resourceLocation, resource, callback)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // delete
    internal fun delete(resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, callback: (DataResponse) -> Unit) {

        createRequest(HttpMethod.Delete, resourceLocation, additionalHeaders) {

            sendRequest(
                request = it,
                resourceLocation = resourceLocation,
                callback = { response: DataResponse ->
                    processDeleteResponse(
                        resourceLocation = resourceLocation,
                        additionalHeaders = additionalHeaders,
                        response = response,
                        callback = callback
                    )
                }
            )
        }
    }

    fun <TResource : Resource> delete(resource: TResource, callback: (DataResponse) -> Unit) {

        return try {

            //look for partition key property(ies) to send for this resource type
            val headers = setResourcePartitionKey(resource)
            val resourceLocation = ResourceLocation.Resource(resource)

            delete(resourceLocation, headers, callback)

        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // replace
    private fun <T : Resource> replace(resource: T, resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, callback: (Response<T>) -> Unit) {

        if (!resource.hasValidId()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        createOrReplace(resource, resourceLocation, true, additionalHeaders, callback)
    }

    // create or replace
    internal fun <T : Resource> createOrReplace(body: T, resourceLocation: ResourceLocation, replacing: Boolean = false, additionalHeaders: MutableMap<String, String>? = null, callback: (Response<T>) -> Unit) {

        try {
            //serialize the resource
            val jsonBody = gson.toJson(body)

            //look for partition key property(ies) to send for this resource type
            val headers = setResourcePartitionKey(body, additionalHeaders)

            //create and send the request
            createRequest(if (replacing) HttpMethod.Put else HttpMethod.Post, resourceLocation, headers, jsonBody) {

                @Suppress("UNCHECKED_CAST")
                sendResourceRequest(
                        request = it,
                        resourceLocation = resourceLocation,
                        callback = { response: Response<T> ->
                            processCreateOrReplaceResponse(
                                 resource = body,
                                 location = resourceLocation,
                                 replace = replacing,
                                 additionalHeaders = headers,
                                 response = response,
                                 callback = callback
                            )
                        },
                        resourceClass = body::class.java as Class<T>
                )
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // create or replace
    private fun <T : Resource> createOrReplace(body: ByteArray, resourceLocation: ResourceLocation, replacing: Boolean = false, additionalHeaders: MutableMap<String, String>? = null, callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null) {

        try {
            createRequest(if (replacing) HttpMethod.Put else HttpMethod.Post, resourceLocation, additionalHeaders, body) {

                sendResourceRequest(it, resourceLocation, callback, resourceClass)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // query
    private fun <T : Resource> query(query: Query, resourceLocation: ResourceLocation, maxPerPage: Int?, partitionKey: String?, resourceClass: Class<T>? = null, callback: (ListResponse<T>) -> Unit) {

        try {
            val json = gson.toJson(query.dictionary)

            // do we have a partition key to send?  If not, then this query will be a cross partition query
            val headers = partitionKey?.let { setPartitionKeyHeader(partitionKey) }
                    ?: mutableMapOf(MSHttpHeader.MSDocumentDBQueryEnableCrossPartition.value to HttpHeaderValue.trueValue)

            createRequest(HttpMethod.Post, resourceLocation, headers, json, true, maxPerPage) { request ->

                sendResourceListRequest(request, resourceLocation, resourceClass) { response ->

                    processQueryResponse(query, resourceLocation, response, resourceClass) { processedResponse ->

                        if (processedResponse.isSuccessful) { // success case

                            callback(processedResponse)

                        } else if (processedResponse.isErrored && processedResponse.error!!.isInvalidCrossPartitionQueryError()) {

                            // if we've tried to query cross partition but have a TOP or ORDER BY, we'll get a specific error we can work around by using partition key range Ids
                            // reference: https://stackoverflow.com/questions/50240232/cosmos-db-rest-api-order-by-with-partitioning

                            // we will grab the partition key ranges for the collection we're querying
                            val dbId = resourceLocation.ancestorIds().getValue(ResourceType.Database)
                            val collId = resourceLocation.ancestorIds().getValue(ResourceType.Collection)

                            getCollectionPartitionKeyRanges(collId, dbId) { pkRanges ->

                                // THEN, we can retry our request after setting the range Id header
                                // TODO:  Note, we may need to do more here if there are additional PartitionKeyRange items that come back... can't find docs on this format
                                headers[MSHttpHeader.MSDocumentDBPartitionKeyRangeId.value] = "${pkRanges.resource!!.resourceId!!},${pkRanges.resource.items[0].id}"

                                createRequest(HttpMethod.Post, resourceLocation, headers, json, true, maxPerPage) { retryRequest ->

                                    sendResourceListRequest(retryRequest, resourceLocation, resourceClass) { retryResponse ->

                                        processQueryResponse(query, resourceLocation, retryResponse, resourceClass) {
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
    internal fun <T : Resource> next(response : ListResponse<T>, resourceType: Type?, callback: (ListResponse<T>) -> Unit) {

        try {
            val request = response.request
                ?: return callback(ListResponse(DataError(DocumentClientError.NextCalledTooEarlyError)))

            val resourceLocation = response.resourceLocation
                ?: return callback(ListResponse(DataError(DocumentClientError.NextCalledTooEarlyError)))

            val type = resourceType
                    ?: return callback(ListResponse(DataError(DocumentClientError.NextCalledTooEarlyError)))

            val continuation = response.metadata.continuation
                ?: return callback(ListResponse(DataError(DocumentClientError.NoMoreResultsError)))

            val newRequest = request.newBuilder()
                    .header(MSHttpHeader.MSContinuation.value,continuation)
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
                                callback(processListResponse(request, resp, resourceLocation, type))

                    })
        } catch (ex: Exception) {
            e(ex)
            callback(ListResponse(DataError(ex)))
        }
    }

    // execute
    private fun <T> execute(body: T? = null, resourceLocation: ResourceLocation, partitionKey: String? = null, callback: (DataResponse) -> Unit) {

        try {
            val json = if (body != null) gson.toJson(body) else gson.toJson(arrayOf<String>())

            val headers = setPartitionKeyHeader(partitionKey)

            createRequest(HttpMethod.Post, resourceLocation, headers, json) {

                sendRequest(it, resourceLocation, callback)
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

    private inline fun getTokenForResource(resourceLocation: ResourceLocation, method: HttpMethod, crossinline callback: (Response<ResourceToken>) -> Unit) {

        if (!isConfigured) {
            return callback(Response(DataError(DocumentClientError.ConfigureError)))
        }

        if (resourceLocation.id?.isValidIdForResource() == false) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        if (resourceTokenProvider != null) {

            resourceTokenProvider!!.getToken(resourceLocation, method)?.let {
                return callback(Response(it))
            }
        } else {

            if (!resourceLocation.supportsPermissionToken) {
                return callback(Response(DataError(DocumentClientError.PermissionError)))
            }

            return permissionProvider?.getPermission(resourceLocation, if (method.isWrite()) PermissionMode.All else PermissionMode.Read) {

                if (it.isSuccessful) {

                    val dateString = String.format("%s %s", dateFormatter.format(Date()), "GMT")

                    it.resource?.token?.let {

                        callback(Response(ResourceToken(URLEncoder.encode(it, "UTF-8"), dateString)))

                    } ?: callback(Response(DataError(DocumentClientError.PermissionError)))
                } else {
                    callback(Response(it.error!!))
                }
            } ?: callback(Response(DataError(DocumentClientError.UnknownError)))
        }

        return callback(Response(DataError(DocumentClientError.UnknownError)))
    }

    private inline fun createRequest(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, maxPerPage: Int? = null, crossinline callback: (Request) -> Unit) {

        createRequestBuilder(method, resourceLocation, additionalHeaders, maxPerPage) {

            when (method) {
                HttpMethod.Get -> it.get()
                HttpMethod.Head -> it.head()
                HttpMethod.Delete -> it.delete()
                else -> throw Exception("Post and Put requests must use an overload that provides the content body")
            }

            callback(it.build())
        }
    }

    private inline fun createRequest(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, jsonBody: String, forQuery: Boolean = false, maxPerPage: Int? = null, crossinline callback: (Request) -> Unit) {

        createRequestBuilder(method, resourceLocation, additionalHeaders, maxPerPage) {

            // For Post on query operations, it must be application/query+json
            // For attachments, must be set to the Mime type of the attachment.
            // For all other tasks, must be application/json.
            var mediaType = jsonMediaType

            if (forQuery) {

                it.addHeader(MSHttpHeader.MSDocumentDBIsQuery.value, HttpHeaderValue.trueValue)
                it.addHeader(HttpHeader.ContentType.value, HttpMediaType.QueryJson.value)
                mediaType = MediaType.parse(HttpMediaType.QueryJson.value)
            }
            else if (method == HttpMethod.Post || method == HttpMethod.Put) {

                if (resourceLocation.resourceType != ResourceType.Attachment) {

                    it.addHeader(HttpHeader.ContentType.value, HttpMediaType.Json.value)
                }
            }

            // we convert the json to bytes here rather than allowing OkHttp, as they will tack on
            //  a charset string that does not work well with certain operations (Query!)
            val body = jsonBody.toByteArray(Charsets.UTF_8)

            when (method) {
                HttpMethod.Post -> it.post(RequestBody.create(mediaType, body))
                HttpMethod.Put -> it.put(RequestBody.create(mediaType, body))
                else -> throw Exception("Get, Head, and Delete requests must use an overload that without a content body")
            }

            callback(it.build())
        }
    }

    private inline fun createRequest(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, body: ByteArray, maxPerPage: Int? = null, crossinline callback: (Request) -> Unit) {

        createRequestBuilder(method, resourceLocation, additionalHeaders, maxPerPage) {

            var mediaType = jsonMediaType

            additionalHeaders?.get(HttpHeader.ContentType.value)?.let { contentType ->
                mediaType = MediaType.parse(contentType)
            }

            when (method) {
                HttpMethod.Post -> it.post(RequestBody.create(mediaType, body))
                HttpMethod.Put -> it.put(RequestBody.create(mediaType, body))
                else -> throw Exception("Get, Head, and Delete requests must use an overload that without a content body")
            }

            callback(it.build())
        }
    }

    private inline fun createRequestBuilder(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, maxPerPage: Int? = null, crossinline callback: (Request.Builder) -> Unit) {

        getTokenForResource(resourceLocation, method) {

            when {
                it.isSuccessful -> it.resource?.let { token ->

                    val url = HttpUrl.Builder()
                            .scheme("https")
                            .host(this.host!!)
                            .addPathSegment(resourceLocation.path())
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
                    maxPerPage?.let { max ->
                        if ((1..1000).contains(max)) {
                            headersBuilder.add(MSHttpHeader.MSMaxItemCount.value, max.toString())
                        } else {
                            throw DocumentClientError.InvalidMaxPerPageError
                        }
                    }

                    // if we have additional headers, let's add them in here
                    additionalHeaders?.let {
                        for (headerName in additionalHeaders.keys) {
                            headersBuilder.add(headerName, additionalHeaders[headerName]!!)
                        }
                    }

                    val builder = Request.Builder()
                            .headers(headersBuilder.build())
                            .url(url)

                    callback(builder)

                } ?: throw DocumentClientError.UnknownError

                it.isErrored -> throw it.error!!

                else -> throw DocumentClientError.UnknownError
            }
        }
    }

    private inline fun <T : Resource> sendResourceRequest(request: Request, resourceLocation: ResourceLocation, crossinline callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null)
            = sendResourceRequest(request, resourceLocation, null, callback = callback, resourceClass = resourceClass)

    private inline fun <T : Resource> sendResourceRequest(request: Request, resourceLocation: ResourceLocation, resource: T?, crossinline callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null) {

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
                                callback(processResponse(request, response, resourceLocation.resourceType, resource, resourceClass))
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex), request))
        }
    }

    private inline fun sendRequest(request: Request, resourceLocation: ResourceLocation, crossinline callback: (DataResponse) -> Unit) {

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
                                callback(processDataResponse(request, resourceLocation, response))
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex), request))
        }
    }

    private inline fun <T : Resource> sendResourceListRequest(request: Request, resourceLocation: ResourceLocation, resourceClass: Class<T>? = null, crossinline callback: (ListResponse<T>) -> Unit) {

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
                                callback(processListResponse(request, response, resourceLocation, resourceClass))
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(ListResponse(DataError(ex), request))
        }
    }

    private fun <T : Resource> processResponse(request: Request, response: okhttp3.Response, resourceType: ResourceType, resource: T?, resourceClass: Class<T>? = null): Response<T> {

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

                    val type = resourceClass ?: resource?.javaClass ?: resourceType.type
                    val returnedResource = gson.fromJson<T>(json, type)
                            ?: return Response(json.toError())

                    setResourceMetadata(response, returnedResource, resourceType)

                    return Response(request, response, json, Result(returnedResource))
                }

                response.code() == HttpStatusCode.NotModified.code -> {

                    resource?.let {
                        setResourceMetadata(response, it, resourceType)
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

    private fun <T : Resource> processListResponse(request: Request, response: okhttp3.Response, resourceLocation: ResourceLocation, resourceType: Type? = null): ListResponse<T> {

        return try {
            val body = response.body()
                    ?: return ListResponse(DataError("Empty response body received"), request, response)
            val json = body.string()

            if (response.isSuccessful) {

                //TODO: see if there's any benefit to caching these type tokens performance wise (or for any other reason)
                val type = resourceType ?: resourceLocation.resourceType.type
                val resourceList = ResourceListJsonDeserializer<T>().deserialize(json, type)

                setResourceMetadata(response, resourceList, resourceLocation.resourceType)

                ResourceCache.shared.cache(resourceList)

                ListResponse(request, response, json, Result(resourceList), resourceLocation, type)
            } else {
                ListResponse(json.toError(), request, response, json)
            }
        } catch (e: Exception) {
            ListResponse(DataError(e), request, response)
        }
    }

    private fun <T : Resource> processCreateOrReplaceResponse(resource: T, location: ResourceLocation, replace: Boolean, additionalHeaders: MutableMap<String, String>? = null, response: Response<T>, callback: (Response<T>) -> Unit) {

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
                    ResourceWriteOperationQueue.shared.addCreateOrReplace(resource, location, additionalHeaders, replace, callback)
                    return
                }

                callback(response)
            }

            else -> {
                callback(response)
            }
        }
    }

    private fun <T : Resource> processResourceGetResponse(resourceLocation: ResourceLocation, response: Response<T>, callback: (Response<T>) -> Unit, resourceClass: Class<T>?) {

        when {
            response.isSuccessful -> {

                callback(response)

                response.resource?.let { ResourceCache.shared.cache(it) }
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError() && resourceClass != null)  {
                    cachedResource(resourceLocation, response, callback, resourceClass)
                    return
                }

                if (response.is404()) {
                    ResourceCache.shared.remove(resourceLocation)
                }

                callback(response)
            }

            else -> { callback(response) }
        }
    }

    private fun <T : Resource> processResourceListResponse(resourceLocation: ResourceLocation, response: ListResponse<T>, callback: (ListResponse<T>) -> Unit, resourceClass: Class<T>?) {

        when {
            response.isSuccessful -> {

                callback(response)

                response.resource?.let { ResourceCache.shared.cache(it) }
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError() && resourceClass != null) {
                    cachedResources(resourceLocation, response, callback, resourceClass)
                    return
                }

                callback(response)
            }

            else -> { callback(response) }
        }
    }

    private fun <T : Resource> processQueryResponse(query: Query, resourceLocation: ResourceLocation, response: ListResponse<T>, resourceClass: Class<T>?, callback: (ListResponse<T>) -> Unit) {

        when {
            response.isSuccessful -> {

                callback(response)

                response.resource?.let { ResourceCache.shared.cache(it, query, resourceLocation.link()) }
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError() && resourceClass != null) {
                    cachedResources(query, resourceLocation, response, callback, resourceClass)
                    return
                }

                callback(response)
            }

            else -> { callback(response) }
        }
    }

    private fun processDeleteResponse(resourceLocation: ResourceLocation, additionalHeaders: MutableMap<String, String>? = null, response: DataResponse, callback: (DataResponse) -> Unit) {

        when {
            response.isSuccessful -> {

                callback(response)

                ResourceCache.shared.remove(resourceLocation)
            }

            response.isErrored -> {

                if (response.error!!.isConnectivityError()) {
                    ResourceWriteOperationQueue.shared.addDelete(resourceLocation, additionalHeaders, callback)
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

    private fun <T : Resource> cachedResource(resourceLocation: ResourceLocation, response: Response<T>? = null, callback: (Response<T>) -> Unit, resourceClass: Class<T>) {

       return ResourceCache.shared.getResourceAt(resourceLocation, resourceClass)?.let { resource ->

            callback(Response(response?.request, response?.response, response?.jsonData, Result(resource), resourceLocation, response?.resourceType, true))

        } ?: callback(Response(DataError(DocumentClientError.NotFound)))
    }

    private fun <T : Resource> cachedResources(resourceLocation: ResourceLocation, response: ListResponse<T>? = null, callback: (ListResponse<T>) -> Unit, resourceClass: Class<T>) {

        return ResourceCache.shared.getResourcesAt(resourceLocation, resourceClass)?.let { resources ->

            callback(ListResponse(response?.request, response?.response, response?.jsonData, Result(resources), resourceLocation, response?.resourceType, true))

        } ?: callback(ListResponse(DataError(DocumentClientError.SerciceUnavailableError)))
    }

    private fun <T : Resource> cachedResources(query: Query, resourceLocation: ResourceLocation, response: ListResponse<T>? = null, callback: (ListResponse<T>) -> Unit, resourceClass: Class<T>) {

        return ResourceCache.shared.getResourcesForQuery(query, resourceClass)?.let { resources ->

            callback(ListResponse(response?.request, response?.response, response?.jsonData, Result(resources), resourceLocation, response?.resourceType, true))

        } ?: callback(ListResponse(DataError(DocumentClientError.SerciceUnavailableError)))
    }

    //endregion

    companion object {

        val shared = DocumentClient()

        lateinit var client: OkHttpClient

        val jsonMediaType = MediaType.parse(HttpMediaType.Json.value)
    }
}