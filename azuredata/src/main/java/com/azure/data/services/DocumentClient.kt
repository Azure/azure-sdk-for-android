package com.azure.data.services

import com.azure.core.http.HttpMediaType
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpStatusCode
import com.azure.data.constants.TokenType
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

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DocumentClient(private val baseUri: ResourceUri, key: String, keyType: TokenType = TokenType.MASTER) {

    private val tokenProvider: TokenProvider = TokenProvider(key, keyType, "1.0")

    // base headers... grab these once and then re-serve
    private val headers: Headers by lazy {
        ContextProvider.appContext.getDefaultHeaders()
    }

    //region Database

    // create
    fun createDatabase(databaseId: String, callback: (ResourceResponse<Database>) -> Unit) {

        val resourceUri = baseUri.forDatabase()

        return create(databaseId, resourceUri, ResourceType.Database, callback = callback)
    }

    // list
    fun databases(callback: (ResourceListResponse<Database>) -> Unit) {

        val resourceUri = baseUri.forDatabase()

        return resources(resourceUri, ResourceType.Database, callback)
    }

    // get
    fun getDatabase(databaseId: String, callback: (ResourceResponse<Database>) -> Unit) {

        val resourceUri = baseUri.forDatabase(databaseId)

        return resource(resourceUri, ResourceType.Database, callback)
    }

    // delete
    fun deleteDatabase(databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forDatabase(databaseId)

        return delete(resourceUri, ResourceType.Database, callback)
    }

    // delete
    fun deleteDatabase(database: Database, callback: (Response) -> Unit) {

        deleteDatabase(database.id, callback)
    }

    //endregion

    //region Collections

    // create
    fun createCollection(collectionId: String, databaseId: String, callback: (ResourceResponse<DocumentCollection>) -> Unit) {

        val resourceUri = baseUri.forCollection(databaseId)

        return create(collectionId, resourceUri, ResourceType.Collection, callback = callback)
    }

    // list
    fun getCollectionsIn(databaseId: String, callback: (ResourceListResponse<DocumentCollection>) -> Unit) {

        val resourceUri = baseUri.forCollection(databaseId)

        return resources(resourceUri, ResourceType.Collection, callback)
    }

    // get
    fun getCollection(collectionId: String, databaseId: String, callback: (ResourceResponse<DocumentCollection>) -> Unit) {

        val resourceUri = baseUri.forCollection(databaseId, collectionId)

        return resource(resourceUri, ResourceType.Collection, callback)
    }

    // delete
    fun deleteCollection(collectionId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forCollection(databaseId, collectionId)

        return delete(resourceUri, ResourceType.Collection, callback)
    }

    // replace
    fun replaceCollection(collectionId: String, databaseId: String, indexingPolicy: IndexingPolicy, callback: (ResourceResponse<DocumentCollection>) -> Unit) {

        val resourceUri = baseUri.forCollection(databaseId, collectionId)

        return replace(collectionId, mutableMapOf<String, Any>("indexingPolicy" to indexingPolicy), resourceUri, ResourceType.Collection, callback = callback)
    }

    //endregion

    //region Documents

    // create
    fun <T : Document> createDocument(document: T, collectionId: String, databaseId: String, callback: (ResourceResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(databaseId, collectionId)

        return create(document, resourceUri, ResourceType.Document, callback = callback)
    }

    // create
    fun <T : Document> createDocument (document: T, collection: DocumentCollection, callback: (ResourceResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(collection.selfLink!!)

        return create(document, resourceUri, ResourceType.Document, callback = callback)
    }

    // list
    fun <T : Document> getDocumentsAs(collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(databaseId, collectionId)

        return resources(resourceUri, ResourceType.Document, callback, documentClass)
    }

    // list
    fun <T : Document> getDocumentsAs(collection: DocumentCollection, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(collection.selfLink!!)

        return resources(resourceUri, ResourceType.Document, callback, documentClass)
    }

    // get
    fun <T : Document> getDocument(documentId: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ResourceResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(databaseId, collectionId, documentId)

        return resource(resourceUri, ResourceType.Document, callback, documentClass)
    }

    // get
    fun <T : Document> getDocument(documentResourceId: String, collection: DocumentCollection, documentClass: Class<T>, callback: (ResourceResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(collection.selfLink!!, documentResourceId = documentResourceId)

        return resource(resourceUri, ResourceType.Document, callback, documentClass)
    }

    // delete
    fun deleteDocument(documentId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forDocument(databaseId, collectionId, documentId)

        return delete(resourceUri, ResourceType.Document, callback)
    }

    // delete
    fun deleteDocument(documentResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forDocument(collection.selfLink!!, documentResourceId = documentResourceId)

        return delete(resourceUri, ResourceType.Document, callback)
    }

    // replace
    fun <T : Document> replaceDocument(document: T, collectionId: String, databaseId: String, callback: (ResourceResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(databaseId, collectionId, document.id)

        return replace(document, resourceUri, ResourceType.Document, callback = callback)
    }

    // replace
    fun <T : Document> replaceDocument(document: T, collection: DocumentCollection, callback: (ResourceResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(collection.selfLink!!, document.resourceId)

        return replace(document, resourceUri, ResourceType.Document, callback = callback)
    }

    // query
    fun <T: Document> queryDocuments (collectionId: String, databaseId: String, query: Query, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(databaseId, collectionId)

        return query(query, resourceUri, ResourceType.Document, callback, documentClass)
    }

    // query
    fun <T: Document> queryDocuments (collection: DocumentCollection, query: Query, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) {

        val resourceUri = baseUri.forDocument(collection.selfLink!!)

        return query(query, resourceUri, ResourceType.Document, callback, documentClass)
    }

    //endregion

    //region Attachments

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(databaseId, collectionId, documentId)

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), resourceUri, ResourceType.Attachment, callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(databaseId, collectionId, documentId)
        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, resourceUri, ResourceType.Attachment, additionalHeaders = headers, callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(document.selfLink!!)

        return create(Attachment(attachmentId, contentType, mediaUrl.toString()), resourceUri, ResourceType.Attachment, callback = callback)
    }

    // create
    fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(document.selfLink!!)
        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, resourceUri, ResourceType.Attachment, additionalHeaders = headers, callback = callback)
    }

    // list
    fun getAttachments(documentId: String, collectionId: String, databaseId: String, callback: (ResourceListResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(databaseId, collectionId, documentId)

        return resources(resourceUri, ResourceType.Attachment, callback)
    }

    // list
    fun getAttachments(document: Document, callback: (ResourceListResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(document.selfLink!!)

        return resources(resourceUri, ResourceType.Attachment, callback)
    }

    // delete
    fun deleteAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forAttachment(databaseId, collectionId, documentId, attachmentId)

        return delete(resourceUri, ResourceType.Attachment, callback)
    }

    // delete
    fun deleteAttachment(attachmentResourceId: String, document: Document, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forAttachment(document.selfLink!!, attachmentResourceId)

        return delete(resourceUri, ResourceType.Attachment, callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(databaseId, collectionId, documentId, attachmentId)

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), resourceUri, ResourceType.Attachment, callback = callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(databaseId, collectionId, documentId, attachmentId)
        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, resourceUri, ResourceType.Attachment, replacing = true, additionalHeaders = headers, callback = callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, attachmentResourceId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(document.selfLink!!, attachmentResourceId)

        return replace(Attachment(attachmentId, contentType, mediaUrl.toString()), resourceUri, ResourceType.Attachment, callback = callback)
    }

    // replace
    fun replaceAttachment(attachmentId: String, attachmentResourceId: String, contentType: String, media: ByteArray, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) {

        val resourceUri = baseUri.forAttachment(document.selfLink!!, attachmentResourceId)
        val headers = Headers.Builder()
                .add(HttpHeader.ContentType.value, contentType)
                .add(HttpHeader.Slug.value, attachmentId)
                .build()

        return createOrReplace(media, resourceUri, ResourceType.Attachment, replacing = true, additionalHeaders = headers, callback = callback)
    }

    //endregion

    //region Stored Procedures

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (ResourceResponse<StoredProcedure>) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(databaseId, collectionId)

        return create(storedProcedureId, resourceUri, ResourceType.StoredProcedure, mutableMapOf("body" to procedure), callback = callback)
    }

    // create
    fun createStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (ResourceResponse<StoredProcedure>) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(collection.selfLink!!)

        return create(storedProcedureId, resourceUri, ResourceType.StoredProcedure, mutableMapOf("body" to procedure), callback = callback)
    }

    // list
    fun getStoredProcedures(collectionId: String, databaseId: String, callback: (ResourceListResponse<StoredProcedure>) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(databaseId, collectionId)

        return resources(resourceUri, ResourceType.StoredProcedure, callback)
    }

    // list
    fun getStoredProcedures(collection: DocumentCollection, callback: (ResourceListResponse<StoredProcedure>) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(collection.selfLink!!)

        return resources(resourceUri, ResourceType.StoredProcedure, callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(databaseId, collectionId, storedProcedureId)

        return delete(resourceUri, ResourceType.StoredProcedure, callback)
    }

    // delete
    fun deleteStoredProcedure(storedProcedureResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(collection.selfLink!!, storedProcedureResourceId = storedProcedureResourceId)

        return delete(resourceUri, ResourceType.StoredProcedure, callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (ResourceResponse<StoredProcedure>) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(databaseId, collectionId, storedProcedureId)

        replace(storedProcedureId, mutableMapOf<String, Any>("body" to procedure), resourceUri, ResourceType.StoredProcedure, callback = callback)
    }

    // replace
    fun replaceStoredProcedure(storedProcedureId: String, storedProcedureResourceId: String, procedure: String, collection: DocumentCollection, callback: (ResourceResponse<StoredProcedure>) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(collection.selfLink!!, storedProcedureResourceId = storedProcedureResourceId)

        replace(storedProcedureId, mutableMapOf<String, Any>("body" to procedure), resourceUri, ResourceType.StoredProcedure, callback = callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, collectionId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(databaseId, collectionId, storedProcedureId)

        return execute(parameters, resourceUri, ResourceType.StoredProcedure, callback)
    }

    // execute
    fun executeStoredProcedure(storedProcedureResourceId: String, parameters: List<String>?, collection: DocumentCollection, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forStoredProcedure(collection.selfLink!!, storedProcedureResourceId = storedProcedureResourceId)

        return execute(parameters, resourceUri, ResourceType.StoredProcedure, callback)
    }

    //endregion

    //region User Defined Functions

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collectionId: String, databaseId: String, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {

        val resourceUri = baseUri.forUdf(databaseId, collectionId)

        return create(userDefinedFunctionId, resourceUri, ResourceType.Udf, mutableMapOf("body" to functionBody), callback = callback)
    }

    // create
    fun createUserDefinedFunction(userDefinedFunctionId: String, functionBody: String, collection: DocumentCollection, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {

        val resourceUri = baseUri.forUdf(collection.selfLink!!)

        return create(userDefinedFunctionId, resourceUri, ResourceType.Udf, mutableMapOf("body" to functionBody), callback = callback)
    }

    // list
    fun getUserDefinedFunctions(collectionId: String, databaseId: String, callback: (ResourceListResponse<UserDefinedFunction>) -> Unit) {

        val resourceUri = baseUri.forUdf(databaseId, collectionId)

        return resources(resourceUri, ResourceType.Udf, callback)
    }

    // list
    fun getUserDefinedFunctions(collection: DocumentCollection, callback: (ResourceListResponse<UserDefinedFunction>) -> Unit) {

        val resourceUri = baseUri.forUdf(collection.selfLink!!)

        return resources(resourceUri, ResourceType.Udf, callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forUdf(databaseId, collectionId, udfId = userDefinedFunctionId)

        return delete(resourceUri, ResourceType.Udf, callback)
    }

    // delete
    fun deleteUserDefinedFunction(userDefinedFunctionResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forUdf(collection.selfLink!!, udfResourceId = userDefinedFunctionResourceId)

        return delete(resourceUri, ResourceType.Udf, callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collectionId: String, databaseId: String, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {

        val resourceUri = baseUri.forUdf(databaseId, collectionId, udfId = userDefinedFunctionId)

        return replace(userDefinedFunctionId, mutableMapOf<String, Any>("body" to function), resourceUri, ResourceType.Udf, callback = callback)
    }

    // replace
    fun replaceUserDefinedFunction(userDefinedFunctionId: String, userDefinedFunctionResourceId: String, function: String, collection: DocumentCollection, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {

        val resourceUri = baseUri.forUdf(collection.selfLink!!, udfResourceId = userDefinedFunctionResourceId)

        return replace(userDefinedFunctionId, mutableMapOf<String, Any>("body" to function), resourceUri, ResourceType.Udf, callback = callback)
    }

    //endregion

    //region Triggers

    // create
    fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Trigger>) -> Unit) {

        val resourceUri = baseUri.forTrigger(databaseId, collectionId)

        return create(Trigger(triggerId, triggerBody, operation, triggerType), resourceUri, ResourceType.Trigger, callback = callback)
    }

    // create
    fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (ResourceResponse<Trigger>) -> Unit) {

        val resourceUri = baseUri.forTrigger(collection.selfLink!!)

        return create(Trigger(triggerId, triggerBody, operation, triggerType), resourceUri, ResourceType.Trigger, callback = callback)
    }

    // list
    fun getTriggers(collectionId: String, databaseId: String, callback: (ResourceListResponse<Trigger>) -> Unit) {

        val resourceUri = baseUri.forTrigger(databaseId, collectionId)

        return resources(resourceUri, ResourceType.Trigger, callback)
    }

    // list
    fun getTriggers(collection: DocumentCollection, callback: (ResourceListResponse<Trigger>) -> Unit) {

        val resourceUri = baseUri.forTrigger(collection.selfLink!!)

        return resources(resourceUri, ResourceType.Trigger, callback)
    }

    // delete
    fun deleteTrigger(triggerId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forTrigger(databaseId, collectionId, triggerId)

        return delete(resourceUri, ResourceType.Trigger, callback)
    }

    // delete
    fun deleteTrigger(triggerResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forTrigger(collection.selfLink!!, triggerResourceId = triggerResourceId)

        return delete(resourceUri, ResourceType.Trigger, callback)
    }

    // replace
    fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Trigger>) -> Unit) {

        val resourceUri = baseUri.forTrigger(databaseId, collectionId, triggerId)

        return replace(Trigger(triggerId, triggerBody, operation, triggerType), resourceUri, ResourceType.Trigger, callback = callback)
    }

    // replace
    fun replaceTrigger(triggerId: String, triggerResourceId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (ResourceResponse<Trigger>) -> Unit) {

        val resourceUri = baseUri.forTrigger(collection.selfLink!!, triggerResourceId = triggerResourceId)

        return replace(Trigger(triggerId, triggerBody, operation, triggerType), resourceUri, ResourceType.Trigger, callback = callback)
    }

    //endregion

    //region Users

    // create
    fun createUser(userId: String, databaseId: String, callback: (ResourceResponse<User>) -> Unit) {

        val resourceUri = baseUri.forUser(databaseId)

        return create(userId, resourceUri, ResourceType.User, callback = callback)
    }

    // list
    fun getUsers(databaseId: String, callback: (ResourceListResponse<User>) -> Unit) {

        val resourceUri = baseUri.forUser(databaseId)

        return resources(resourceUri, ResourceType.User, callback)
    }

    // get
    fun getUser(userId: String, databaseId: String, callback: (ResourceResponse<User>) -> Unit) {

        val resourceUri = baseUri.forUser(databaseId, userId)

        return resource(resourceUri, ResourceType.User, callback)
    }

    // delete
    fun deleteUser(userId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forUser(databaseId, userId)

        return delete(resourceUri, ResourceType.User, callback)
    }

    // replace
    fun replaceUser(userId: String, newUserId: String, databaseId: String, callback: (ResourceResponse<User>) -> Unit) {

        val resourceUri = baseUri.forUser(databaseId, userId)

        return replace(newUserId, resourceUri, ResourceType.User, callback)
    }

    //endregion

    //region Permissions

    // create
    fun createPermission(permissionId: String, permissionMode: Permission.PermissionMode, resource: Resource, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(databaseId, userId, null)

        val permission = Permission(permissionId, permissionMode, resource.selfLink!!)

        return create(permission, resourceUri, ResourceType.Permission, callback = callback)
    }

    // create
    fun createPermission(permissionId: String, permissionMode: Permission.PermissionMode, resource: Resource, user: User, callback: (ResourceResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(user.selfLink!!, null)

        val permission = Permission(permissionId, permissionMode, resource.selfLink!!)

        return create(permission, resourceUri, ResourceType.Permission, callback = callback)
    }

    // list
    fun getPermissions(userId: String, databaseId: String, callback: (ResourceListResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(databaseId, userId, null)

        return resources(resourceUri, ResourceType.Permission, callback)
    }

    // list
    fun getPermissions(user: User, callback: (ResourceListResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(user.selfLink!!, null)

        return resources(resourceUri, ResourceType.Permission, callback)
    }

    // get
    fun getPermission(permissionId: String, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(databaseId, userId, permissionId)

        return resource(resourceUri, ResourceType.Permission, callback)
    }

    // get
    fun getPermission(permissionResourceId: String, user: User, callback: (ResourceResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(user.selfLink!!, permissionResourceId)

        return resource(resourceUri, ResourceType.Permission, callback)
    }

    // delete
    fun deletePermission(permissionId: String, userId: String, databaseId: String, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forPermission(databaseId, userId, permissionId)

        return delete(resourceUri, ResourceType.Permission, callback)
    }

    // delete
    fun deletePermission(permissionResourceId: String, user: User, callback: (Response) -> Unit) {

        val resourceUri = baseUri.forPermission(user.selfLink!!, permissionResourceId)

        return delete(resourceUri, ResourceType.Permission, callback)
    }

    // replace
    fun replacePermission(permissionId: String, permissionMode: Permission.PermissionMode, resourceSelfLink: String, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(databaseId, userId, permissionId)

        return replace(Permission(permissionId, permissionMode, resourceSelfLink), resourceUri, ResourceType.Permission, callback = callback)
    }

    // replace
    fun replacePermission(permissionId: String, permissionResourceId: String, permissionMode: Permission.PermissionMode, resourceSelfLink: String, user: User, callback: (ResourceResponse<Permission>) -> Unit) {

        val resourceUri = baseUri.forPermission(user.selfLink!!, permissionResourceId = permissionResourceId)

        return replace(Permission(permissionId, permissionMode, resourceSelfLink), resourceUri, ResourceType.Permission, callback = callback)
    }

    //endregion

    //region Offers

    // list
    fun getOffers(callback: (ResourceListResponse<Offer>) -> Unit) {

        val resourceUri = baseUri.forOffer()

        return resources(resourceUri, ResourceType.Offer, callback)
    }

    // get
    fun getOffer(offerId: String, callback: (ResourceResponse<Offer>) -> Unit): Any {

        val resourceUri = baseUri.forOffer(offerId)

        return resource(resourceUri, ResourceType.Offer, callback)
    }

    //endregion

    //region Resource operations

    // create
    private fun <T : Resource> create(resource: T, resourceUri: UrlLink, resourceType: ResourceType, additionalHeaders: Headers? = null, callback: (ResourceResponse<T>) -> Unit) {

        if (!resource.id.isValidResourceId()) {
            return callback(ResourceResponse(DataError.fromType(ErrorType.InvalidId)))
        }

        createOrReplace(resource, resourceUri, resourceType, false, additionalHeaders, callback)
    }

    // create
    private fun <T : Resource> create(resourceId: String, resourceUri: UrlLink, resourceType: ResourceType, data: MutableMap<String, String>? = null, additionalHeaders: Headers? = null, callback: (ResourceResponse<T>) -> Unit) {

        if (!resourceId.isValidResourceId()) {
            return callback(ResourceResponse(DataError.fromType(ErrorType.InvalidId)))
        }

        val map = data ?: mutableMapOf()
        map["id"] = resourceId

        createOrReplace(map, resourceUri, resourceType, false, additionalHeaders, callback)
    }

    // list
    private fun <T : Resource> resources(resourceUri: UrlLink, resourceType: ResourceType, callback: (ResourceListResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        val request = createRequest(HttpMethod.Get, resourceUri, resourceType)

        return sendResourceListRequest(request, resourceType, callback, resourceClass)
    }

    // get
    private fun <T : Resource> resource(resourceUri: UrlLink, resourceType: ResourceType, callback: (ResourceResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        val request = createRequest(HttpMethod.Get, resourceUri, resourceType)

        return sendResourceRequest(request, resourceType, callback, resourceClass)
    }

    // refresh
    fun <T : Resource> refresh(resource: T, callback: (ResourceResponse<T>) -> Unit) {

        return try {

            val resourceUri = baseUri.forResource(resource)
            val resourceType = ResourceType.fromType(resource.javaClass)

            val headers = Headers.Builder()
                    .add(HttpHeader.IfNoneMatch.value, resource.etag!!)
                    .build()

            val request = createRequest(HttpMethod.Get, resourceUri, resourceType, headers)

            sendResourceRequest(request, resourceType, resource, callback)
        } catch (e: Exception) {
            callback(ResourceResponse(DataError(e)))
        }
    }

    // delete
    private fun delete(resourceUri: UrlLink, resourceType: ResourceType, callback: (Response) -> Unit) {

        val request = createRequest(HttpMethod.Delete, resourceUri, resourceType)

        return sendRequest(request, callback)
    }

    fun <TResource : Resource> delete(resource: TResource, callback: (Response) -> Unit) {

        return try {

            val resourceUri = baseUri.forResource(resource)
            val resourceType = ResourceType.fromType(resource.javaClass)

            val request = createRequest(HttpMethod.Delete, resourceUri, resourceType)

            sendRequest(request, callback)
        } catch (e: Exception) {
            callback(Response(DataError(e)))
        }
    }

    // replace
    private fun <T : Resource> replace(resource: T, resourceUri: UrlLink, resourceType: ResourceType, additionalHeaders: Headers? = null, callback: (ResourceResponse<T>) -> Unit) {

        if (!resource.id.isValidResourceId()) {
            return callback(ResourceResponse(DataError.fromType(ErrorType.InvalidId)))
        }

        createOrReplace(resource, resourceUri, resourceType, true, additionalHeaders, callback)
    }

    // replace
    private fun <T : Resource> replace(resourceId: String, resourceUri: UrlLink, resourceType: ResourceType, callback: (ResourceResponse<T>) -> Unit)
            = replace(resourceId, data = null, resourceUri = resourceUri, resourceType = resourceType, callback = callback)

    // replace
    private fun <T : Resource> replace(resourceId: String, data: MutableMap<String, Any>? = null, resourceUri: UrlLink, resourceType: ResourceType, additionalHeaders: Headers? = null, callback: (ResourceResponse<T>) -> Unit) {

        if (!resourceId.isValidResourceId()) {
            return callback(ResourceResponse(DataError.fromType(ErrorType.InvalidId)))
        }

        val map = data ?: mutableMapOf()
        map["id"] = resourceId

        createOrReplace(map, resourceUri, resourceType, true, additionalHeaders, callback)
    }

    // create or replace
    private fun <T : Resource> createOrReplace(body: T, resourceUri: UrlLink, resourceType: ResourceType, replacing: Boolean = false, additionalHeaders: Headers? = null, callback: (ResourceResponse<T>) -> Unit) {

        try {
            val jsonBody = gson.toJson(body)

            val request = createRequest(if (replacing) HttpMethod.Put else HttpMethod.Post, resourceUri, resourceType, additionalHeaders, jsonBody)

            @Suppress("UNCHECKED_CAST")
            return sendResourceRequest(request, resourceType, callback, body::class.java as Class<T>)

        } catch (e: Exception) {
            callback(ResourceResponse(DataError(e)))
        }
    }

    // create or replace
    private fun <T : Resource> createOrReplace(body: Map<String, Any>, resourceUri: UrlLink, resourceType: ResourceType, replacing: Boolean = false, additionalHeaders: Headers? = null, callback: (ResourceResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        try {
            val jsonBody = gson.toJson(body)

            val request = createRequest(if (replacing) HttpMethod.Put else HttpMethod.Post, resourceUri, resourceType, additionalHeaders, jsonBody)

            return sendResourceRequest(request, resourceType, callback, resourceClass)
        } catch (e: Exception) {
            callback(ResourceResponse(DataError(e)))
        }
    }

    // create or replace
    private fun <T : Resource> createOrReplace(body: ByteArray, resourceUri: UrlLink, resourceType: ResourceType, replacing: Boolean = false, additionalHeaders: Headers? = null, callback: (ResourceResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        try {
            val request = createRequest(if (replacing) HttpMethod.Put else HttpMethod.Post, resourceUri, resourceType, additionalHeaders, body)

            return sendResourceRequest(request, resourceType, callback, resourceClass)
        } catch (e: Exception) {
            callback(ResourceResponse(DataError(e)))
        }
    }

    // query
    private fun <T : Resource> query(query: Query, resourceUri: UrlLink, resourceType: ResourceType, callback: (ResourceListResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        logIfVerbose(query)

        try {
            val json = gson.toJson(query.dictionary)

            val request = createRequest(HttpMethod.Post, resourceUri, resourceType, forQuery = true, jsonBody = json)

            return sendResourceListRequest(request, resourceType, callback, resourceClass)

        } catch (e: Exception) {
            callback(ResourceListResponse(DataError(e)))
        }
    }

    // execute
    private fun <T> execute(body: T? = null, resourceUri: UrlLink, resourceType: ResourceType, callback: (Response) -> Unit) {

        try {
            val json = if (body != null) gson.toJson(body) else gson.toJson(arrayOf<String>())

            val request = createRequest(HttpMethod.Post, resourceUri, resourceType, forQuery = true, jsonBody = json)

            return sendRequest(request, callback)

        } catch (e: Exception) {
            callback(Response(DataError(e)))
        }
    }

    //endregion

    //region Network plumbing

    private fun createRequest(method: HttpMethod, resourceUri: UrlLink, resourceType: ResourceType, additionalHeaders: Headers? = null): Request {

        try {
            val builder = createRequestBuilder(method, resourceUri, resourceType, additionalHeaders)

            when (method) {
                HttpMethod.Get -> builder.get()
                HttpMethod.Head -> builder.head()
                HttpMethod.Delete -> builder.delete()
                else -> throw Exception("Post and Put requests must use an overload that provides the content body")
            }

            return builder.build()
        } catch (e: Exception) {

            e.printStackTrace()

            throw e
        }
    }

    private fun createRequest(method: HttpMethod, resourceUri: UrlLink, resourceType: ResourceType, additionalHeaders: Headers? = null, jsonBody: String, forQuery: Boolean = false): Request {

        try {
            val builder = createRequestBuilder(method, resourceUri, resourceType, additionalHeaders)

            // For Post on query operations, it must be application/query+json
            // For attachments, must be set to the Mime type of the attachment.
            // For all other tasks, must be application/json.
            var mediaType = jsonMediaType

            if (forQuery) {
                builder.addHeader(MSHttpHeader.MSDocumentDBIsQuery.value, "True")
                builder.addHeader(HttpHeader.ContentType.value, HttpMediaType.QueryJson.value)
                mediaType = MediaType.parse(HttpMediaType.QueryJson.value)
            }
            else if ((method == HttpMethod.Post || method == HttpMethod.Put) && resourceType != ResourceType.Attachment) {

                builder.addHeader(HttpHeader.ContentType.value, HttpMediaType.Json.value)
            }

            // we convert the json to bytes here rather than allowing OkHttp, as they will tack on
            //  a charset string that does not work well with certain operations (Query)
            val body = jsonBody.toByteArray(Charsets.UTF_8)

            when (method) {
                HttpMethod.Post -> builder.post(RequestBody.create(mediaType, body))
                HttpMethod.Put -> builder.put(RequestBody.create(mediaType, body))
                else -> throw Exception("Get, Head, and Delete requests must use an overload that without a content body")
            }

            return builder.build()
        } catch (e: Exception) {

            e.printStackTrace()

            throw e
        }
    }

    private fun createRequest(method: HttpMethod, resourceUri: UrlLink, resourceType: ResourceType, additionalHeaders: Headers? = null, body: ByteArray): Request {

        try {
            val builder = createRequestBuilder(method, resourceUri, resourceType, additionalHeaders)
            var mediaType = jsonMediaType

            additionalHeaders?.get(HttpHeader.ContentType.value)?.let {
                mediaType = MediaType.parse(it)
            }

            when (method) {
                HttpMethod.Post -> builder.post(RequestBody.create(mediaType, body))
                HttpMethod.Put -> builder.put(RequestBody.create(mediaType, body))
                else -> throw Exception("Get, Head, and Delete requests must use an overload that without a content body")
            }

            return builder.build()
        } catch (e: Exception) {

            e.printStackTrace()

            throw e
        }
    }

    private fun createRequestBuilder(method: HttpMethod, resourceUri: UrlLink, resourceType: ResourceType, additionalHeaders: Headers? = null): Request.Builder {

        val token = tokenProvider.getToken(method, resourceType, resourceUri.link)

        val builder = Request.Builder()
                .headers(headers) //base headers
                .url(resourceUri.url)

        // set the api version
        builder.addHeader(MSHttpHeader.MSVersion.value, HttpHeaderValue.apiVersion)
        // and the token data
        builder.addHeader(MSHttpHeader.MSDate.value, token.date)
        builder.addHeader(HttpHeader.Authorization.value, token.authString)

        // if we have additional headers, let's add them in here
        additionalHeaders?.let {
            for (headerName in additionalHeaders.names()) {
                builder.addHeader(headerName, additionalHeaders[headerName]!!)
            }
        }

        return builder
    }

    private fun <T : Resource> sendResourceRequest(request: Request, resourceType: ResourceType, callback: (ResourceResponse<T>) -> Unit, resourceClass: Class<T>? = null)
            = sendResourceRequest(request, resourceType, null, callback = callback, resourceClass = resourceClass)

    private fun <T : Resource> sendResourceRequest(request: Request, resourceType: ResourceType, resource: T?, callback: (ResourceResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        logIfVerbose("***", "Sending ${request.method()} request for Data to ${request.url()}", "\tContent : length = ${request.body()?.contentLength()}, type = ${request.body()?.contentType()}", "***")

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, e: IOException) {
                            logIfVerbose(e)
                            return callback(ResourceResponse(DataError(e), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processResponse(request, response, resourceType, resource, resourceClass))
                    })
        } catch (e: Exception) {
            logIfVerbose(e)
            callback(ResourceResponse(DataError(e), request))
        }
    }

    private fun sendRequest(request: Request, callback: (Response) -> Unit) {

        logIfVerbose("***", "Sending ${request.method()} request for Data to ${request.url()}", "\tContent : length = ${request.body()?.contentLength()}, type = ${request.body()?.contentType()}", "***")

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        override fun onFailure(call: Call, e: IOException) {
                            logIfVerbose(e)
                            return callback(Response(DataError(e), request))
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processDataResponse(request, response))
                    })
        } catch (e: Exception) {
            logIfVerbose(e)
            callback(Response(DataError(e), request))
        }
    }

    private fun <T : Resource> sendResourceListRequest(request: Request, resourceType: ResourceType, callback: (ResourceListResponse<T>) -> Unit, resourceClass: Class<T>? = null) {

        logIfVerbose("***", "Sending ${request.method()} request for Data to ${request.url()}", "\tContent : length = ${request.body()?.contentLength()}, type = ${request.body()?.contentType()}", "***")

        try {
            client.newCall(request)
                    .enqueue(object : Callback {

                        // only transpprt errors handled here
                        override fun onFailure(call: Call, e: IOException) =
                                callback(ResourceListResponse(DataError(e)))

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: okhttp3.Response) =
                                callback(processListResponse(request, response, resourceType, resourceClass))
                    })
        } catch (e: Exception) {
            logIfVerbose(e)
            callback(ResourceListResponse(DataError(e), request))
        }
    }

    private fun <T : Resource> processResponse(request: Request, response: okhttp3.Response, resourceType: ResourceType, resource: T?, resourceClass: Class<T>? = null): ResourceResponse<T> {

        try {
            val body = response.body() ?: return ResourceResponse(DataError("Empty response body received"))
            val json = body.string()

            logIfVerbose(json)

            //check http return code/success
            if (response.isSuccessful) {

                val type = resourceClass ?: resource?.javaClass ?: resourceType.type
                val returnedResource = gson.fromJson<T>(json, type) ?: return ResourceResponse(json.toError())

                return ResourceResponse(request, response, json, Result(returnedResource))
            } else if (response.code() == HttpStatusCode.NotModified.code) {
                //return the original resource
                return ResourceResponse(request, response, json, Result(resource))
            } else {
                return ResourceResponse(json.toError(), request, response, json)
            }
        } catch (e: Exception) {
            return ResourceResponse(DataError(e), request, response)
        }
    }

    private fun <T : Resource> processListResponse(request: Request, response: okhttp3.Response, resourceType: ResourceType, resourceClass: Class<T>? = null): ResourceListResponse<T> {

        try {
            val body = response.body() ?: return ResourceListResponse(DataError("Empty response body received"), request, response)
            val json = body.string()

            logIfVerbose(json)

            if (response.isSuccessful) {

                //TODO: see if there's any benefit to caching these type tokens performance wise (or for any other reason)
                val type = resourceClass ?: resourceType.type
                val listType = TypeToken.getParameterized(ResourceList::class.java, type).type
                val resourceList = gson.fromJson<ResourceList<T>>(json, listType) ?: return ResourceListResponse(json.toError(), request, response, json)

                return ResourceListResponse(request, response, json, Result(resourceList))
            } else {
                return ResourceListResponse(json.toError(), request, response, json)
            }
        } catch (e: Exception) {
            return ResourceListResponse(DataError(e), request, response)
        }
    }

    private fun processDataResponse(request: Request, response: okhttp3.Response): Response {

        try {
            val body = response.body() ?: return Response(DataError("Empty response body received"), request, response)
            val json = body.string()

            logIfVerbose(json)

            //check http return code
            return if (response.isSuccessful) {
                Response(request, response, json, Result(json))
            } else {
                Response(json.toError(), request, response, json)
            }
        } catch (e: Exception) {
            return Response(DataError(e), request, response)
        }
    }

    //endregion

    private fun logIfVerbose(thing: Any) {

        if (ContextProvider.verboseLogging) {
            println(thing)
        }
    }

    private fun logIfVerbose(vararg things: Any) {

        if (ContextProvider.verboseLogging) {
            things.forEach {
                println(it)
            }
        }
    }

    companion object {

        val client = OkHttpClient()

        val jsonMediaType = MediaType.parse(HttpMediaType.Json.value)
    }
}