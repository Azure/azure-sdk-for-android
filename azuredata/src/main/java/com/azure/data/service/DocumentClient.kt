package com.azure.data.service

import com.azure.core.http.HttpHeader
import com.azure.core.http.HttpMediaType
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpStatusCode
import com.azure.core.log.d
import com.azure.core.log.e
import com.azure.core.log.i
import com.azure.core.util.DateUtil
import com.azure.data.AzureData
import com.azure.data.constants.HttpHeaderValue
import com.azure.data.constants.MSHttpHeader
import com.azure.data.model.*
import com.google.gson.reflect.TypeToken
import com.azure.data.model.indexing.IndexingPolicy
import com.azure.data.util.*
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

class DocumentClient {

    private var host: String? = null

    private var permissionProvider: PermissionProvider? = null

    private var resourceTokenProvider: ResourceTokenProvider? = null

    val configuredWithMasterKey: Boolean
        get() = resourceTokenProvider != null

    constructor(accountName: String, masterKey: String, permissionMode: PermissionMode) {

        resourceTokenProvider = ResourceTokenProvider(masterKey, permissionMode)

        commonConfigure("$accountName.documents.azure.com")
    }

    constructor(accountUrl: URL, masterKey: String, permissionMode: PermissionMode) {

        resourceTokenProvider = ResourceTokenProvider(masterKey, permissionMode)

        commonConfigure(accountUrl.host)
    }

    constructor(accountName: String, permissionProvider: PermissionProvider) {

        this.permissionProvider = permissionProvider

        commonConfigure("$accountName.documents.azure.com")
    }

    constructor(accountUrl: URL, permissionProvider: PermissionProvider) {

        this.permissionProvider = permissionProvider

        commonConfigure(accountUrl.host)
    }

    val isConfigured: Boolean
        get() = !host.isNullOrEmpty() && (resourceTokenProvider != null || permissionProvider != null)

    // base headers... grab these once and then re-serve
    private val headers: Headers by lazy {
        ContextProvider.appContext.getDefaultHeaders()
    }

    private fun commonConfigure(host: String) {

        if (host.isEmpty()) {
            throw Exception("Host is invalid")
        }

        this.host = host

        ResourceOracle.init(ContextProvider.appContext, host)
        PermissionCache.init(host)
    }

    fun reset () {

        host = null
        permissionProvider = null
        resourceTokenProvider = null
    }

    //region Database

    // create
    fun createDatabase(databaseId: String, callback: (Response<Database>) -> Unit)
            = create(databaseId, ResourceLocation.Database(), callback = callback)

    // list
    fun getDatabases(callback: (ListResponse<Database>) -> Unit) {

        return resources(ResourceLocation.Database(), callback)
    }

    // get
    fun getDatabase(databaseId: String, callback: (Response<Database>) -> Unit) {

        return resource(ResourceLocation.Database(databaseId), callback)
    }

    // delete
    fun deleteDatabase(databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Database(databaseId), callback)
    }

    //endregion

    //region Collections

    // create
    fun createCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        return create(collectionId, ResourceLocation.Collection(databaseId), callback = callback)
    }

    // list
    fun getCollectionsIn(databaseId: String, callback: (ListResponse<DocumentCollection>) -> Unit) {

        return resources(ResourceLocation.Collection(databaseId), callback)
    }

    // get
    fun getCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) {

        return resource(ResourceLocation.Collection(databaseId, collectionId), callback)
    }

    // delete
    fun deleteCollection(collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Collection(databaseId, collectionId), callback)
    }

    // replace
    fun replaceCollection(collectionId: String, databaseId: String, indexingPolicy: IndexingPolicy, callback: (Response<DocumentCollection>) -> Unit) {

        return replace(collectionId, ResourceLocation.Collection(databaseId, collectionId), mutableMapOf<String, Any>("indexingPolicy" to indexingPolicy), callback = callback)
    }

    //endregion

    //region Documents

    // create
    fun <T : Document> createDocument(document: T, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) {

        return create(document, ResourceLocation.Document(databaseId, collectionId), callback = callback)
    }

    // create
    fun <T : Document> createDocument (document: T, collection: DocumentCollection, callback: (Response<T>) -> Unit) {

        return create(document, ResourceLocation.Child(ResourceType.Document, collection), callback = callback)
    }

    // list
    fun <T : Document> getDocumentsAs(collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        return resources(ResourceLocation.Document(databaseId, collectionId), callback, documentClass)
    }

    // list
    fun <T : Document> getDocumentsAs(collection: DocumentCollection, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Document, collection), callback, documentClass)
    }

    // get
    fun <T : Document> getDocument(documentId: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (Response<T>) -> Unit) {

        return resource(ResourceLocation.Document(databaseId, collectionId, documentId), callback, documentClass)
    }

    // get
    fun <T : Document> getDocument(documentId: String, collection: DocumentCollection, documentClass: Class<T>, callback: (Response<T>) -> Unit) {

        return resource(ResourceLocation.Child(ResourceType.Document, collection, documentId), callback, documentClass)
    }

    // delete
    fun deleteDocument(documentId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Document(databaseId, collectionId, documentId), callback)
    }

    // delete
    fun deleteDocument(documentId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Document, collection, documentId), callback)
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
    fun <T: Document> queryDocuments (collectionId: String, databaseId: String, query: Query, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        return query(query, ResourceLocation.Document(databaseId, collectionId), callback, documentClass)
    }

    // query
    fun <T: Document> queryDocuments (collection: DocumentCollection, query: Query, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {

        return query(query, ResourceLocation.Child(ResourceType.Document, collection), callback, documentClass)
    }

    //endregion

    //region Attachments

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, callback: (Response<Attachment>) -> Unit) {

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Attachment(databaseId, collectionId, documentId), callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, callback: (Response<Attachment>) -> Unit) {

        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, ResourceLocation.Attachment(databaseId, collectionId, documentId), additionalHeaders = headers, callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) {

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Child(ResourceType.Attachment, document), callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) {

        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, ResourceLocation.Child(ResourceType.Attachment, document), additionalHeaders = headers, callback = callback)
    }

    // list
    fun getAttachments(documentId: String, collectionId: String, databaseId: String, callback: (ListResponse<Attachment>) -> Unit) {

        return resources(ResourceLocation.Attachment(databaseId, collectionId, documentId), callback)
    }

    // list
    fun getAttachments(document: Document, callback: (ListResponse<Attachment>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Attachment, document), callback)
    }

    // delete
    fun deleteAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId), callback)
    }

    // delete
    fun deleteAttachment(attachmentId: String, document: Document, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Attachment, document, attachmentId), callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, callback: (Response<Attachment>) -> Unit) {

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId), callback = callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, callback: (Response<Attachment>) -> Unit) {

        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, ResourceLocation.Attachment(databaseId, collectionId, documentId, attachmentId), replacing = true, additionalHeaders = headers, callback = callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) {

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), ResourceLocation.Child(ResourceType.Attachment, document, attachmentId), callback = callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) {

        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, ResourceLocation.Child(ResourceType.Attachment, document, attachmentId), replacing = true, additionalHeaders = headers, callback = callback)
    }

    //endregion

    //region Stored Procedures

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) {

        return create(storedProcedureId, ResourceLocation.StoredProcedure(databaseId, collectionId), mutableMapOf("body" to procedure), callback = callback)
    }

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) {

        return create(storedProcedureId, ResourceLocation.Child(ResourceType.StoredProcedure, collection), mutableMapOf("body" to procedure), callback = callback)
    }

    // list
    fun getStoredProcedures(collectionId: String, databaseId: String, callback: (ListResponse<StoredProcedure>) -> Unit) {

        return resources(ResourceLocation.StoredProcedure(databaseId, collectionId), callback)
    }

    // list
    fun getStoredProcedures(collection: DocumentCollection, callback: (ListResponse<StoredProcedure>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.StoredProcedure, collection), callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId), callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId), callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) {

        replace(storedProcedureId, ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId), mutableMapOf<String, Any>("body" to procedure), callback = callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) {

        replace(storedProcedureId, ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId), mutableMapOf<String, Any>("body" to procedure), callback = callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return execute(parameters, ResourceLocation.StoredProcedure(databaseId, collectionId, storedProcedureId), callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return execute(parameters, ResourceLocation.Child(ResourceType.StoredProcedure, collection, storedProcedureId), callback)
    }

    //endregion

    //region User Defined Functions

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) {

        return create(userDefinedFunctionId, ResourceLocation.Udf(databaseId, collectionId), mutableMapOf("body" to functionBody), callback = callback)
    }

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) {

        return create(userDefinedFunctionId, ResourceLocation.Child(ResourceType.Udf, collection), mutableMapOf("body" to functionBody), callback = callback)
    }

    // list
    fun getUserDefinedFunctions(collectionId: String, databaseId: String, callback: (ListResponse<UserDefinedFunction>) -> Unit) {

        return resources(ResourceLocation.Udf(databaseId, collectionId), callback)
    }

    // list
    fun getUserDefinedFunctions(collection: DocumentCollection, callback: (ListResponse<UserDefinedFunction>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Udf, collection), callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Udf(databaseId, collectionId, userDefinedFunctionId), callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Udf, collection, userDefinedFunctionId), callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) {

        return replace(userDefinedFunctionId, ResourceLocation.Udf(databaseId, collectionId, userDefinedFunctionId), mutableMapOf<String, Any>("body" to function), callback = callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) {

        return replace(userDefinedFunctionId, ResourceLocation.Child(ResourceType.Udf, collection, userDefinedFunctionId), mutableMapOf<String, Any>("body" to function), callback = callback)
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
    fun getTriggers(collectionId: String, databaseId: String, callback: (ListResponse<Trigger>) -> Unit) {

        return resources(ResourceLocation.Trigger(databaseId, collectionId), callback)
    }

    // list
    fun getTriggers(collection: DocumentCollection, callback: (ListResponse<Trigger>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Trigger, collection), callback)
    }

    // delete
    fun deleteTrigger(triggerId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Trigger(databaseId, collectionId, triggerId), callback)
    }

    // delete
    fun deleteTrigger(triggerId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Trigger, collection, triggerId), callback)
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

        return create(userId, ResourceLocation.User(databaseId), callback = callback)
    }

    // list
    fun getUsers(databaseId: String, callback: (ListResponse<User>) -> Unit) {

        return resources(ResourceLocation.User(databaseId), callback)
    }

    // get
    fun getUser(userId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        return resource(ResourceLocation.User(databaseId, userId), callback)
    }

    // delete
    fun deleteUser(userId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.User(databaseId, userId), callback)
    }

    // replace
    fun replaceUser(userId: String, newUserId: String, databaseId: String, callback: (Response<User>) -> Unit) {

        return replace(newUserId, ResourceLocation.User(databaseId, userId), callback)
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
    fun getPermissions(userId: String, databaseId: String, callback: (ListResponse<Permission>) -> Unit) {

        return resources(ResourceLocation.Permission(databaseId, userId), callback)
    }

    // list
    fun getPermissions(user: User, callback: (ListResponse<Permission>) -> Unit) {

        return resources(ResourceLocation.Child(ResourceType.Permission, user), callback)
    }

    // get
    fun getPermission(permissionId: String, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) {

        return resource(ResourceLocation.Permission(databaseId, userId, permissionId), callback)
    }

    // get
    fun getPermission(permissionId: String, user: User, callback: (Response<Permission>) -> Unit) {

        return resource(ResourceLocation.Child(ResourceType.Permission, user, permissionId), callback)
    }

    // delete
    fun deletePermission(permissionId: String, userId: String, databaseId: String, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Permission(databaseId, userId, permissionId), callback)
    }

    // delete
    fun deletePermission(permissionId: String, user: User, callback: (DataResponse) -> Unit) {

        return delete(ResourceLocation.Child(ResourceType.Permission, user, permissionId), callback)
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
    fun getOffers(callback: (ListResponse<Offer>) -> Unit) {

        return resources(ResourceLocation.Offer(), callback)
    }

    // get
    fun getOffer(offerId: String, callback: (Response<Offer>) -> Unit): Any {

        return resource(ResourceLocation.Offer(offerId), callback)
    }

    //endregion

    //region Resource operations

    // create
    private fun <T : Resource> create(resource: T, resourceLocation: ResourceLocation, additionalHeaders: Headers? = null, callback: (Response<T>) -> Unit) {

        if (!resource.hasValidId()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        createOrReplace(resource, resourceLocation, false, additionalHeaders, callback)
    }

    // create
    private fun <T : Resource> create(resourceId: String, resourceLocation: ResourceLocation, data: MutableMap<String, String>? = null, additionalHeaders: Headers? = null, callback: (Response<T>) -> Unit) {

        if (!resourceId.isValidIdForResource()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        val map = data ?: mutableMapOf()
        map["id"] = resourceId

        createOrReplace(map, resourceLocation, false, additionalHeaders, callback)
    }

    // list
    private fun <T : Resource> resources(resourceLocation: ResourceLocation, callback: (ListResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        if (ContextProvider.isOffline) {
            i{"offline, calling back with cached data"}
            // todo: callback with cached data ...
            // todo: ... then return
        }

        createRequest(HttpMethod.Get, resourceLocation) {

            sendResourceListRequest(it, resourceLocation, callback, resourceClass)
        }
    }

    // get
    private fun <T : Resource> resource(resourceLocation: ResourceLocation, callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null) {

        createRequest(HttpMethod.Get, resourceLocation) {

            sendResourceRequest(it, resourceLocation, callback, resourceClass)
        }
    }

    // refresh
    fun <T : Resource> refresh(resource: T, callback: (Response<T>) -> Unit) {

        return try {

            val resourceLocation = ResourceLocation.Resource(resource)

            // create the request - if we have an etag, we'll set & send the IfNoneMatch header
            if (!resource.etag.isNullOrEmpty()) {

                val headers = Headers.Builder()
                        .add(HttpHeader.IfNoneMatch.value, resource.etag!!)
                        .build()

                createRequest(HttpMethod.Get, resourceLocation, headers) {
                    //send the request!
                    sendResourceRequest(it, resourceLocation, resource, callback)
                }
            } else {

                createRequest(HttpMethod.Get, resourceLocation) {
                    //send the request!
                    sendResourceRequest(it, resourceLocation, resource, callback)
                }
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // delete
    private fun delete(resourceLocation: ResourceLocation, callback: (DataResponse) -> Unit) {

        createRequest(HttpMethod.Delete, resourceLocation) {

            sendRequest(it, callback)
        }
    }

    fun <TResource : Resource> delete(resource: TResource, callback: (DataResponse) -> Unit) {

        return try {

//            val resourceUri = baseUri.forResource(resource)
//            val resourceType = ResourceType.fromType(resource.javaClass)

            createRequest(HttpMethod.Delete, ResourceLocation.Resource(resource)) {

                sendRequest(it, callback)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // replace
    private fun <T : Resource> replace(resource: T, resourceLocation: ResourceLocation, additionalHeaders: Headers? = null, callback: (Response<T>) -> Unit) {

        if (!resource.hasValidId()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        createOrReplace(resource, resourceLocation, true, additionalHeaders, callback)
    }

    // replace
    private fun <T : Resource> replace(resourceId: String, resourceLocation: ResourceLocation, callback: (Response<T>) -> Unit)
            = replace(resourceId, resourceLocation, data = null, additionalHeaders = null, callback = callback)

    // replace
    private fun <T : Resource> replace(resourceId: String, resourceLocation: ResourceLocation, data: MutableMap<String, Any>? = null, additionalHeaders: Headers? = null, callback: (Response<T>) -> Unit) {

        if (!resourceId.isValidIdForResource()) {
            return callback(Response(DataError(DocumentClientError.InvalidId)))
        }

        val map = data ?: mutableMapOf()
        map["id"] = resourceId

        createOrReplace(map, resourceLocation, true, additionalHeaders, callback)
    }

    // create or replace
    private fun <T : Resource> createOrReplace(body: T, resourceLocation: ResourceLocation, replacing: Boolean = false, additionalHeaders: Headers? = null, callback: (Response<T>) -> Unit) {

        try {
            val jsonBody = gson.toJson(body)

            createRequest(if (replacing) HttpMethod.Put else HttpMethod.Post, resourceLocation, additionalHeaders, jsonBody) {

                @Suppress("UNCHECKED_CAST")
                sendResourceRequest(it, resourceLocation, callback, body::class.java as Class<T>)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // create or replace
    private fun <T : Resource> createOrReplace(body: Map<String, Any>, resourceLocation: ResourceLocation, replacing: Boolean = false, additionalHeaders: Headers? = null, callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null) {

        try {
            val jsonBody = gson.toJson(body)

            createRequest(if (replacing) HttpMethod.Put else HttpMethod.Post, resourceLocation, additionalHeaders, jsonBody) {

                sendResourceRequest(it, resourceLocation, callback, resourceClass)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex)))
        }
    }

    // create or replace
    private fun <T : Resource> createOrReplace(body: ByteArray, resourceLocation: ResourceLocation, replacing: Boolean = false, additionalHeaders: Headers? = null, callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null) {

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
    private fun <T : Resource> query(query: Query, resourceLocation: ResourceLocation, callback: (ListResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        d{query.toString()}

        try {
            val json = gson.toJson(query.dictionary)

            createRequest(HttpMethod.Post, resourceLocation, forQuery = true, jsonBody = json) {

                sendResourceListRequest(it, resourceLocation, callback, resourceClass)
            }
        } catch (ex: Exception) {
            e(ex)
            callback(ListResponse(DataError(ex)))
        }
    }

    // execute
    private fun <T> execute(body: T? = null, resourceLocation: ResourceLocation, callback: (DataResponse) -> Unit) {

        try {
            val json = if (body != null) gson.toJson(body) else gson.toJson(arrayOf<String>())

            createRequest(HttpMethod.Post, resourceLocation, jsonBody = json) {

                sendRequest(it, callback)
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

    private fun getTokenforResource(resourceLocation: ResourceLocation, method: HttpMethod, callback: (Response<ResourceToken>) -> Unit) {

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

            permissionProvider?.getPermission(resourceLocation, if (method.isWrite()) PermissionMode.All else PermissionMode.Read) {

                if (it.isErrored) {
                    return callback(Response(it.error!!))
                }

                val dateString = String.format("%s %s", dateFormatter.format(Date()), "GMT")

                it.resource?.token?.let {

                    return callback(Response(ResourceToken(dateString, it)))
                    //val authStringEncoded = URLEncoder.encode(String.format("type=master&ver=%s&sig=%s", tokenVersion, signature), "UTF-8")
                }
            }
        }

        return callback(Response(DataError(DocumentClientError.UnknownError)))
    }

    private inline fun createRequest(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: Headers? = null, crossinline callback: (Request) -> Unit) {

        createRequestBuilder(method, resourceLocation, additionalHeaders) {

            when (method) {
                HttpMethod.Get -> it.get()
                HttpMethod.Head -> it.head()
                HttpMethod.Delete -> it.delete()
                else -> throw Exception("Post and Put requests must use an overload that provides the content body")
            }

            callback(it.build())
        }
    }

    private inline fun createRequest(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: Headers? = null, jsonBody: String, forQuery: Boolean = false, crossinline callback: (Request) -> Unit) {

        createRequestBuilder(method, resourceLocation, additionalHeaders) {

            // For Post on query operations, it must be application/query+json
            // For attachments, must be set to the Mime type of the attachment.
            // For all other tasks, must be application/json.
            var mediaType = jsonMediaType

            if (forQuery) {
                it.addHeader(MSHttpHeader.MSDocumentDBIsQuery.value, "True")
                it.addHeader(HttpHeader.ContentType.value, HttpMediaType.QueryJson.value)
                mediaType = MediaType.parse(HttpMediaType.QueryJson.value)
            }
            else if ((method == HttpMethod.Post || method == HttpMethod.Put) && resourceLocation.resourceType != ResourceType.Attachment) {

                it.addHeader(HttpHeader.ContentType.value, HttpMediaType.Json.value)
            }

            // we convert the json to bytes here rather than allowing OkHttp, as they will tack on
            //  a charset string that does not work well with certain operations (Query)
            val body = jsonBody.toByteArray(Charsets.UTF_8)

            when (method) {
                HttpMethod.Post -> it.post(RequestBody.create(mediaType, body))
                HttpMethod.Put -> it.put(RequestBody.create(mediaType, body))
                else -> throw Exception("Get, Head, and Delete requests must use an overload that without a content body")
            }

            callback(it.build())
        }
    }

    private inline fun createRequest(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: Headers? = null, body: ByteArray, crossinline callback: (Request) -> Unit) {

        createRequestBuilder(method, resourceLocation, additionalHeaders) {

            var mediaType = jsonMediaType

            additionalHeaders?.get(HttpHeader.ContentType.value)?.let {
                mediaType = MediaType.parse(it)
            }

            when (method) {
                HttpMethod.Post -> it.post(RequestBody.create(mediaType, body))
                HttpMethod.Put -> it.put(RequestBody.create(mediaType, body))
                else -> throw Exception("Get, Head, and Delete requests must use an overload that without a content body")
            }

            callback(it.build())
        }
    }

    private inline fun createRequestBuilder(method: HttpMethod, resourceLocation: ResourceLocation, additionalHeaders: Headers? = null, crossinline callback: (Request.Builder) -> Unit) {

        getTokenforResource(resourceLocation, method) {

            when {
                it.isSuccessful -> it.resource?.let {

                    val url = HttpUrl.Builder()
                            .scheme("https")
                            .host(this.host!!)
                            .addPathSegment(resourceLocation.path())
                            .build()

                    val builder = Request.Builder()
                            .headers(headers) //base headers
                            .url(url)

                    // set the api version
                    builder.addHeader(MSHttpHeader.MSVersion.value, HttpHeaderValue.apiVersion)
                    // and the token data
                    builder.addHeader(MSHttpHeader.MSDate.value, it.date)
                    builder.addHeader(HttpHeader.Authorization.value, it.token)

                    // if we have additional headers, let's add them in here
                    additionalHeaders?.let {
                        for (headerName in additionalHeaders.names()) {
                            builder.addHeader(headerName, additionalHeaders[headerName]!!)
                        }
                    }

                    callback(builder)

                } ?: throw DocumentClientError.UnknownError
                it.isErrored -> throw it.error!!
                else -> throw DocumentClientError.UnknownError
            }
        }
    }

    private fun <T : Resource> sendResourceRequest(request: Request, resourceLocation: ResourceLocation, callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null)
            = sendResourceRequest(request, resourceLocation, null, callback = callback, resourceClass = resourceClass)

    private inline fun <T : Resource> sendResourceRequest(request: Request, resourceLocation: ResourceLocation, resource: T?, crossinline callback: (Response<T>) -> Unit, resourceClass: Class<T>? = null) {

        d{"***"}
        d{"Sending ${request.method()} request for Data to ${request.url()}"}
        d{"\tContent : length = ${request.body()?.contentLength()}, type = ${request.body()?.contentType()}"}
        d{"***"}

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, ex: IOException) {
                            e(ex)
                            return callback(Response(DataError(ex), request))
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

    private inline fun sendRequest(request: Request, crossinline callback: (DataResponse) -> Unit) {

        d{"***"}
        d{"Sending ${request.method()} request for Data to ${request.url()}"}
        d{"\tContent : length = ${request.body()?.contentLength()}, type = ${request.body()?.contentType()}"}
        d{"***"}

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, ex: IOException) {
                            e(ex)
                            return callback(Response(DataError(ex), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processDataResponse(request, response))
                    })
        } catch (ex: Exception) {
            e(ex)
            callback(Response(DataError(ex), request))
        }
    }

    private inline fun <T : Resource> sendResourceListRequest(request: Request, resourceLocation: ResourceLocation, crossinline callback: (ListResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        d{"***"}
        d{"Sending ${request.method()} request for Data to ${request.url()}"}
        d{"\tContent : length = ${request.body()?.contentLength()}, type = ${request.body()?.contentType()}"}
        d{"***"}

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        // only transport errors handled here
                        override fun onFailure(call: Call, e: IOException) {
                            ContextProvider.isOffline = true
                            // todo: callback with cached data instead of the callback with the error below
                            callback(ListResponse(DataError(e)))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processListResponse(request, response, resourceLocation.resourceType, resourceClass))
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
            val json = body.string().also{d{it}}

            //check http return code/success
            when {
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

    private fun <T : Resource> processListResponse(request: Request, response: okhttp3.Response, resourceType: ResourceType, resourceClass: Class<T>? = null): ListResponse<T> {

        try {
            val body = response.body()
                    ?: return ListResponse(DataError("Empty response body received"), request, response)
            val json = body.string().also{d{it}}

            if (response.isSuccessful) {

                //TODO: see if there's any benefit to caching these type tokens performance wise (or for any other reason)
                val type = resourceClass ?: resourceType.type
                val listType = TypeToken.getParameterized(ResourceList::class.java, type).type
                val resourceList = gson.fromJson<ResourceList<T>>(json, listType)
                        ?: return ListResponse(json.toError(), request, response, json)

                setResourceMetadata(response, resourceList, resourceType)

                return ListResponse(request, response, json, Result(resourceList))
            } else {
                return ListResponse(json.toError(), request, response, json)
            }
        } catch (e: Exception) {
            return ListResponse(DataError(e), request, response)
        }
    }

    private fun setResourceMetadata(response: okhttp3.Response, resource: ResourceBase, resourceType: ResourceType) {

        //grab & store alt Link and persist alt link <-> self link mapping
        val altContentPath = response.header(MSHttpHeader.MSAltContentPath.value, null)
        resource.setAltContentLink(resourceType.path, altContentPath)
        ResourceOracle.shared.storeLinks(resource)
    }

    private fun processDataResponse(request: Request, response: okhttp3.Response): DataResponse {

        try {
            val body = response.body()
                    ?: return Response(DataError("Empty response body received"), request, response)
            val responseBodyString = body.string().also{d{it}}

            //check http return code
            return if (response.isSuccessful) {

//                if (request.method() == HttpMethod.Delete.toString()) {
////                    ResourceOracle.shared.removeLinks(request.)
//                    //TODO: figure this out!
//                    val i = 999
//                }

                DataResponse(request, response, responseBodyString, Result(responseBodyString))
            } else {
                Response(responseBodyString.toError(), request, response, responseBodyString)
            }
        } catch (e: Exception) {
            return Response(DataError(e), request, response)
        }
    }

    //endregion

    companion object {

        val client = OkHttpClient()

        val jsonMediaType = MediaType.parse(HttpMediaType.Json.value)
    }
}