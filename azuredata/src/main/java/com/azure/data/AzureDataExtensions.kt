package com.azure.data

import com.azure.data.model.*
import com.azure.data.model.service.DataResponse
import com.azure.data.model.service.ListResponse
import com.azure.data.model.service.Response
import okhttp3.HttpUrl
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

//region Database

//region Database -> Collections

// create
@Deprecated("Creating a collection without a partition key is deprecated and will be removed in a future version of AzureData")
fun Database.createCollection (collectionId: String, callback: (Response<DocumentCollection>) -> Unit) {
    return AzureData.createCollection(collectionId, this.id, callback)
}

// create
fun Database.createCollection (collectionId: String, partitionKey: String, callback: (Response<DocumentCollection>) -> Unit) {
    return AzureData.createCollection(collectionId, partitionKey, this.id, callback)
}

// create
fun Database.createCollection (collectionId: String, throughput: Int, partitionKey: String, callback: (Response<DocumentCollection>) -> Unit) {
    return AzureData.createCollection(collectionId, throughput, partitionKey, this.id, callback)
}

// get
fun Database.getCollection (collectionId: String, callback: (Response<DocumentCollection>) -> Unit) {
    return AzureData.getCollection(collectionId, this.id, callback)
}

// list
@JvmOverloads
fun Database.getCollections (maxPerPage: Int? = null, callback: (ListResponse<DocumentCollection>) -> Unit) {
    return AzureData.getCollections(this.id, maxPerPage, callback)
}

// delete
fun Database.deleteCollection (collection: DocumentCollection, callback: (DataResponse) -> Unit) {
    return AzureData.deleteCollection(collection.id, this.id, callback)
}

// delete
fun Database.deleteCollection (collectionId: String, callback: (DataResponse) -> Unit) {
    return AzureData.deleteCollection(collectionId, this.id, callback)
}

//endregion

//region Database -> User

// create
fun Database.createUser (userId: String, callback: (Response<User>) -> Unit) {
    return AzureData.createUser(userId, this.id, callback)
}

// list
@JvmOverloads
fun Database.getUsers (maxPerPage: Int? = null, callback: (ListResponse<User>) -> Unit) {
    return AzureData.getUsers(this.id, maxPerPage, callback)
}

// get
fun Database.getUser (userId: String, callback: (Response<User>) -> Unit) {
    return AzureData.getUser(userId, this.id, callback)
}

// delete
fun Database.deleteUser (userId: String, callback: (DataResponse) -> Unit) {
    return AzureData.deleteUser(userId, this.id, callback)
}

// delete
fun Database.deleteUser (user: User, callback: (DataResponse) -> Unit) {
    return AzureData.deleteUser(user, this, callback)
}

// replace
fun Database.replaceUser (userId: String, newUserId: String, callback: (Response<User>) -> Unit) {
    return AzureData.replaceUser(userId, newUserId, this, callback)
}

//endregion

//endregion


//region DocumentCollection

//region DocumentCollection -> Documents

// list
@JvmOverloads
fun <T : Document> DocumentCollection.getDocuments (documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {
    return AzureData.getDocuments(this, documentClass, maxPerPage, callback)
}

// create
fun <T : Document> DocumentCollection.createDocument (document: T, preTrigger: String? = null, postTrigger: String? = null, callback: (Response<T>) -> Unit) {
    return AzureData.createDocument(document, this, preTrigger, postTrigger, callback)
}

// create
fun <T : Document> DocumentCollection.createDocument (document: T, partitionKey: String, preTrigger: String? = null, postTrigger: String? = null, callback: (Response<T>) -> Unit) {
    return AzureData.createDocument(document, partitionKey, this, preTrigger, postTrigger, callback)
}

// get
@Deprecated("Getting a document without a partition key is deprecated and will be removed in a future version of AzureData")
fun <T : Document> DocumentCollection.getDocument (documentId: String, documentClass: Class<T>, callback: (Response<T>) -> Unit) {
    return AzureData.getDocument(documentId, this, documentClass, callback)
}

// get
fun <T : Document> DocumentCollection.getDocument (documentId: String, partitionKey: String, documentClass: Class<T>, callback: (Response<T>) -> Unit) {
    return AzureData.getDocument(documentId, partitionKey, this, documentClass, callback)
}

// delete
fun DocumentCollection.deleteDocument (document: Document, preTrigger: String? = null, postTrigger: String? = null, callback: (DataResponse) -> Unit) {
    return AzureData.deleteDocument(document, preTrigger, postTrigger, callback)
}

// replace
fun <T : Document> DocumentCollection.replaceDocument (document: T, preTrigger: String? = null, postTrigger: String? = null, callback: (Response<T>) -> Unit) {
    return AzureData.replaceDocument(document, this, preTrigger, postTrigger, callback)
}

// replace
fun <T : Document> DocumentCollection.replaceDocument (document: T, partitionKey: String, preTrigger: String? = null, postTrigger: String? = null, callback: (Response<T>) -> Unit) {
    return AzureData.replaceDocument(document, partitionKey, this, preTrigger, postTrigger, callback)
}

// query
@JvmOverloads
fun <T : Document> DocumentCollection.queryDocuments (query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {
    return AzureData.queryDocuments(this, query, documentClass, maxPerPage, callback)
}

// query
@JvmOverloads
fun <T : Document> DocumentCollection.queryDocuments (query: Query, partitionKey: String, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) {
    return AzureData.queryDocuments(this, partitionKey, query, documentClass, maxPerPage, callback)
}

// find
fun <T : Document> DocumentCollection.findDocument (documentId: String, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) {
    return AzureData.findDocument(documentId, this, documentClass, callback)
}

//endregion

//region DocumentCollection -> Stored Procedures

// create
fun DocumentCollection.createStoredProcedure (storedProcedureId: String, procedure: String, callback: (Response<StoredProcedure>) -> Unit) {
    return AzureData.createStoredProcedure(storedProcedureId, procedure, this, callback)
}

// list
@JvmOverloads
fun DocumentCollection.getStoredProcedures (maxPerPage: Int? = null, callback: (ListResponse<StoredProcedure>) -> Unit) {
    return AzureData.getStoredProcedures(this, maxPerPage, callback)
}

// delete
fun DocumentCollection.deleteStoredProcedure (storedProcedureId: String, callback: (DataResponse) -> Unit) {
    return AzureData.deleteStoredProcedure(storedProcedureId, this, callback)
}

// delete
fun DocumentCollection.deleteStoredProcedure (storedProcedure: StoredProcedure, callback: (DataResponse) -> Unit) {
    return AzureData.deleteStoredProcedure(storedProcedure, this, callback)
}

// replace
fun DocumentCollection.replaceStoredProcedure (storedProcedureId: String, procedure: String, callback: (Response<StoredProcedure>) -> Unit) {
    return AzureData.replaceStoredProcedure(storedProcedureId, procedure, this, callback)
}

// replace
fun DocumentCollection.replaceStoredProcedure (storedProcedure: StoredProcedure, callback: (Response<StoredProcedure>) -> Unit) {
    return AzureData.replaceStoredProcedure(storedProcedure, this, callback)
}

// execute
fun DocumentCollection.executeStoredProcedure (storedProcedureId: String, parameters: List<String>?, callback: (DataResponse) -> Unit) {
    return AzureData.executeStoredProcedure(storedProcedureId, parameters, this, callback)
}

// execute
fun DocumentCollection.executeStoredProcedure (storedProcedureId: String, parameters: List<String>?, partitionKey: String, callback: (DataResponse) -> Unit) {
    return AzureData.executeStoredProcedure(storedProcedureId, parameters, partitionKey, this, callback)
}

//endregion

//region DocumentCollection -> UDF

// create
fun DocumentCollection.createUserDefinedFunction (userDefinedFunctionId: String, functionBody: String, callback: (Response<UserDefinedFunction>) -> Unit) {
    return AzureData.createUserDefinedFunction(userDefinedFunctionId, functionBody, this, callback)
}

// list
@JvmOverloads
fun DocumentCollection.getUserDefinedFunctions (maxPerPage: Int? = null, callback: (ListResponse<UserDefinedFunction>) -> Unit) {
    return AzureData.getUserDefinedFunctions(this, maxPerPage, callback)
}

// delete
fun DocumentCollection.deleteUserDefinedFunction (userDefinedFunction: UserDefinedFunction, callback: (DataResponse) -> Unit) {
    return AzureData.deleteUserDefinedFunction(userDefinedFunction, this, callback)
}

// delete
fun DocumentCollection.deleteUserDefinedFunction (userDefinedFunctionId: String, callback: (DataResponse) -> Unit) {
    return AzureData.deleteUserDefinedFunction(userDefinedFunctionId, this, callback)
}

// replace
fun DocumentCollection.replaceUserDefinedFunction (userDefinedFunctionId: String, procedure: String, callback: (Response<UserDefinedFunction>) -> Unit) {
    return AzureData.replaceUserDefinedFunction(userDefinedFunctionId, procedure, this, callback)
}

// replace
fun DocumentCollection.replaceUserDefinedFunction (userDefinedFunction: UserDefinedFunction, callback: (Response<UserDefinedFunction>) -> Unit) {
    return AzureData.replaceUserDefinedFunction(userDefinedFunction, this, callback)
}

//endregion

//region DocumentCollection -> Trigger

// create
fun DocumentCollection.createTrigger (triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, callback: (Response<Trigger>) -> Unit) {
    return AzureData.createTrigger(triggerId, operation, triggerType, triggerBody, this, callback)
}

// list
@JvmOverloads
fun DocumentCollection.getTriggers (maxPerPage: Int? = null, callback: (ListResponse<Trigger>) -> Unit) {
    return AzureData.getTriggers(this, maxPerPage, callback)
}

// delete
fun DocumentCollection.deleteTrigger (trigger: Trigger, callback: (DataResponse) -> Unit) {
    return AzureData.deleteTrigger(trigger, this, callback)
}

// delete
fun DocumentCollection.deleteTrigger (triggerId: String, callback: (DataResponse) -> Unit) {
    return AzureData.deleteTrigger(triggerId, this, callback)
}

// replace
fun DocumentCollection.replaceTrigger (triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, callback: (Response<Trigger>) -> Unit) {
    return AzureData.replaceTrigger(triggerId, operation, triggerType, triggerBody, this, callback)
}

// replace
fun DocumentCollection.replaceTrigger (trigger: Trigger, callback: (Response<Trigger>) -> Unit) {
    return AzureData.replaceTrigger(trigger, trigger.triggerOperation!!, trigger.triggerType!!, this, callback)
}

//endregion

//endregion


//region Document -> Attachment

// create
fun Document.createAttachment (attachmentId: String, contentType: String, mediaUrl: URL, callback: (Response<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// create
fun Document.createAttachment (attachmentId: String, contentType: String, mediaUrl: HttpUrl, callback: (Response<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// create
fun Document.createAttachment (attachmentId: String, contentType: String, mediaUrl: String, callback: (Response<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// create
fun Document.createAttachment (attachmentId: String, contentType: String, data: ByteArray, callback: (Response<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, data, this, callback)
}

// list
@JvmOverloads
fun Document.getAttachments (maxPerPage: Int? = null, callback: (ListResponse<Attachment>) -> Unit) {
    return AzureData.getAttachments(this, maxPerPage, callback)
}

// delete
fun Document.deleteAttachment (attachment: Attachment, callback: (DataResponse) -> Unit) {
    return AzureData.deleteAttachment(attachment, this, callback)
}

// delete
fun Document.deleteAttachment (attachmentRid: String, callback: (DataResponse) -> Unit) {
    return AzureData.deleteAttachment(attachmentRid, this, callback)
}

// replace
fun Document.replaceAttachment (attachmentId: String, contentType: String, mediaUrl: URL, callback: (Response<Attachment>) -> Unit) {
    return AzureData.replaceAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// replace
fun Document.replaceAttachment (attachmentId: String, contentType: String, mediaUrl: HttpUrl, callback: (Response<Attachment>) -> Unit) {
    return AzureData.replaceAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// replace
fun Document.replaceAttachment (attachmentId: String, contentType: String, mediaUrl: String, callback: (Response<Attachment>) -> Unit) {
    return AzureData.replaceAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

//endregion


//region User

//region User -> Permission

// create
fun <TResource : Resource> User.createPermission (permissionId: String, permissionMode: PermissionMode, resource: TResource, callback: (Response<Permission>) -> Unit) {
    return AzureData.createPermission(permissionId, permissionMode, resource, this, callback)
}

// list
@JvmOverloads
fun User.getPermissions (maxPerPage: Int? = null, callback: (ListResponse<Permission>) -> Unit) {
    return AzureData.getPermissions(this, maxPerPage, callback)
}

// get
fun User.getPermission (permissionId: String, callback: (Response<Permission>) -> Unit) {
    return AzureData.getPermission(permissionId, this, callback)
}

// delete
fun User.deletePermission (permissionId: String, databaseId: String, callback: (DataResponse) -> Unit) {
    return AzureData.deletePermission(permissionId, this.id, databaseId, callback)
}

// delete
fun User.deletePermission (permission: Permission, callback: (DataResponse) -> Unit) {
    return AzureData.deletePermission(permission, this, callback)
}

// delete
fun User.deletePermission (permissionId: String, callback: (DataResponse) -> Unit) {
    return AzureData.deletePermission(permissionId, this, callback)
}

// replace
fun <TResource : Resource> User.replacePermission (permissionId: String, permissionMode: PermissionMode, resource: TResource, callback: (Response<Permission>) -> Unit) {
    return AzureData.replacePermission(permissionId, permissionMode, resource, this, callback)
}

// replace
fun User.replacePermission (permissionId: String, permissionMode: PermissionMode, resourceSelfLink: String, callback: (Response<Permission>) -> Unit) {
    return AzureData.replacePermission(permissionId, permissionMode, resourceSelfLink, this, callback)
}

// replace
fun User.replacePermission (permission: Permission, callback: (Response<Permission>) -> Unit) {
    return AzureData.replacePermission(permission, this, callback)
}

//endregion

//endregion


// Resource

fun <TResource : Resource> TResource.delete (callback: (DataResponse) -> Unit) =
        AzureData.delete(this, callback)

fun <TResource : Resource> TResource.refresh (callback: (Response<TResource>) -> Unit) =
        AzureData.refresh(this, callback)

fun <TResource : Resource> TResource.createPermission (permissionId: String, permissionMode: PermissionMode, user: User, callback: (Response<Permission>) -> Unit) =
        AzureData.createPermission(permissionId, permissionMode,this, user, callback)

fun <TResource : Resource> TResource.replacePermission (permissionId: String, permissionMode: PermissionMode, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) =
        AzureData.replacePermission(permissionId, permissionMode, this.selfLink!!, userId, databaseId, callback)

fun <TResource : Resource> TResource.replacePermission (permission: Permission, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) =
        AzureData.replacePermission(permission.id, permission.permissionMode!!, this, userId, databaseId, callback)

fun <TResource : Resource> TResource.replacePermission (permissionId: String, permissionMode: PermissionMode, user: User, callback: (Response<Permission>) -> Unit) =
        AzureData.replacePermission(permissionId, permissionMode, this, user, callback)