package com.azure.data

import com.azure.data.model.*
import com.azure.data.services.ResourceListResponse
import com.azure.data.services.ResourceResponse
import com.azure.data.services.Response
import okhttp3.HttpUrl
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

//region Database

//region Database -> Collections

// create
fun Database.createCollection (collectionId: String, callback: (ResourceResponse<DocumentCollection>) -> Unit) {
    return AzureData.createCollection(collectionId, this.id, callback)
}

// get
fun Database.getCollection (collectionId: String, callback: (ResourceResponse<DocumentCollection>) -> Unit) {
    return AzureData.getCollection(collectionId, this.id, callback)
}

// list
fun Database.getCollections (callback: (ResourceListResponse<DocumentCollection>) -> Unit) {
    return AzureData.getCollections(this.id, callback)
}

// delete
fun Database.deleteCollection (collection: DocumentCollection, callback: (Response) -> Unit) {
    return AzureData.deleteCollection(collection.id, this.id, callback)
}

// delete
fun Database.deleteCollection (collectionId: String, callback: (Response) -> Unit) {
    return AzureData.deleteCollection(collectionId, this.id, callback)
}

//endregion

//region Database -> User

// create
fun Database.createUser (userId: String, callback: (ResourceResponse<User>) -> Unit) {
    return AzureData.createUser(userId, this.id, callback)
}

// list
fun Database.getUsers (callback: (ResourceListResponse<User>) -> Unit) {
    return AzureData.getUsers(this.id, callback)
}

// get
fun Database.getUser (userId: String, callback: (ResourceResponse<User>) -> Unit) {
    return AzureData.getUser(userId, this.id, callback)
}

// delete
fun Database.deleteUser (userId: String, callback: (Response) -> Unit) {
    return AzureData.deleteUser(userId, this.id, callback)
}

// delete
fun Database.deleteUser (user: User, callback: (Response) -> Unit) {
    return AzureData.deleteUser(user, this, callback)
}

// replace
fun Database.replaceUser (userId: String, newUserId: String, callback: (ResourceResponse<User>) -> Unit) {
    return AzureData.replaceUser(userId, newUserId, this, callback)
}

//endregion

//endregion


//region DocumentCollection

//region DocumentCollection -> Documents

// list
fun <T : Document> DocumentCollection.getDocuments (documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) {
    return AzureData.getDocuments(this, documentClass, callback)
}

// create
fun <T : Document> DocumentCollection.createDocument (document: T, callback: (ResourceResponse<T>) -> Unit) {
    return AzureData.createDocument(document, this, callback)
}

// get
fun <T : Document> DocumentCollection.getDocument (documentResourceId: String, documentClass: Class<T>, callback: (ResourceResponse<T>) -> Unit) {
    return AzureData.getDocument(documentResourceId, this, documentClass, callback)
}

// delete
fun DocumentCollection.deleteDocument (document: Document, callback: (Response) -> Unit) {
    return AzureData.deleteDocument(document.resourceId!!, this, callback)
}

// delete
fun DocumentCollection.deleteDocument (documentResourceId: String, callback: (Response) -> Unit) {
    return AzureData.deleteDocument(documentResourceId, this, callback)
}

// replace
fun <T : Document> DocumentCollection.replaceDocument (document: T, callback: (ResourceResponse<T>) -> Unit) {
    return AzureData.replaceDocument(document, this, callback)
}

// query
fun <T : Document> DocumentCollection.queryDocuments (query: Query, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) {
    return AzureData.queryDocuments(this, query, documentClass, callback)
}

//endregion

//region DocumentCollection -> Stored Procedures

// create
fun DocumentCollection.createStoredProcedure (storedProcedureId: String, procedure: String, callback: (ResourceResponse<StoredProcedure>) -> Unit) {
    return AzureData.createStoredProcedure(storedProcedureId, procedure, this, callback)
}

// list
fun DocumentCollection.getStoredProcedures (callback: (ResourceListResponse<StoredProcedure>) -> Unit) {
    return AzureData.getStoredProcedures(this, callback)
}

// delete
fun DocumentCollection.deleteStoredProcedure (storedProcedureResourceId: String, callback: (Response) -> Unit) {
    return AzureData.deleteStoredProcedure(storedProcedureResourceId, this, callback)
}

// delete
fun DocumentCollection.deleteStoredProcedure (storedProcedure: StoredProcedure, callback: (Response) -> Unit) {
    return AzureData.deleteStoredProcedure(storedProcedure, this, callback)
}

// replace
fun DocumentCollection.replaceStoredProcedure (storedProcedureId: String, storedProcedureResourceId: String, procedure: String, callback: (ResourceResponse<StoredProcedure>) -> Unit) {
    return AzureData.replaceStoredProcedure(storedProcedureId, storedProcedureResourceId, procedure, this, callback)
}

// replace
fun DocumentCollection.replaceStoredProcedure (storedProcedure: StoredProcedure, callback: (ResourceResponse<StoredProcedure>) -> Unit) {
    return AzureData.replaceStoredProcedure(storedProcedure, this, callback)
}

// execute
fun DocumentCollection.executeStoredProcedure (storedProcedureResourceId: String, parameters: List<String>?, callback: (Response) -> Unit) {
    return AzureData.executeStoredProcedure(storedProcedureResourceId, parameters, this, callback)
}

//endregion

//region DocumentCollection -> UDF

// create
fun DocumentCollection.createUserDefinedFunction (userDefinedFunctionId: String, functionBody: String, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {
    return AzureData.createUserDefinedFunction(userDefinedFunctionId, functionBody, this, callback)
}

// list
fun DocumentCollection.getUserDefinedFunctions (callback: (ResourceListResponse<UserDefinedFunction>) -> Unit) {
    return AzureData.getUserDefinedFunctions(this, callback)
}

// delete
fun DocumentCollection.deleteUserDefinedFunction (userDefinedFunction: UserDefinedFunction, callback: (Response) -> Unit) {
    return AzureData.deleteUserDefinedFunction(userDefinedFunction, this, callback)
}

// delete
fun DocumentCollection.deleteUserDefinedFunction (userDefinedFunctionResourceId: String, callback: (Response) -> Unit) {
    return AzureData.deleteUserDefinedFunction(userDefinedFunctionResourceId, this, callback)
}

// replace
fun DocumentCollection.replaceUserDefinedFunction (userDefinedFunctionId: String, userDefinedFunctionResourceId: String, procedure: String, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {
    return AzureData.replaceUserDefinedFunction(userDefinedFunctionId, userDefinedFunctionResourceId, procedure, this, callback)
}

// replace
fun DocumentCollection.replaceUserDefinedFunction (userDefinedFunction: UserDefinedFunction, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {
    return AzureData.replaceUserDefinedFunction(userDefinedFunction, this, callback)
}

//endregion

//region DocumentCollection -> Trigger

// create
fun DocumentCollection.createTrigger (triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, callback: (ResourceResponse<Trigger>) -> Unit) {
    return AzureData.createTrigger(triggerId, operation, triggerType, triggerBody, this, callback)
}

// list
fun DocumentCollection.getTriggers (callback: (ResourceListResponse<Trigger>) -> Unit) {
    return AzureData.getTriggers(this, callback)
}

// delete
fun DocumentCollection.deleteTrigger (trigger: Trigger, callback: (Response) -> Unit) {
    return AzureData.deleteTrigger(trigger, this, callback)
}

// delete
fun DocumentCollection.deleteTrigger (triggerResourceId: String, callback: (Response) -> Unit) {
    return AzureData.deleteTrigger(triggerResourceId, this, callback)
}

// replace
fun DocumentCollection.replaceTrigger (triggerId: String, triggerResourceId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, callback: (ResourceResponse<Trigger>) -> Unit) {
    return AzureData.replaceTrigger(triggerId, triggerResourceId, operation, triggerType, triggerBody, this, callback)
}

// replace
fun DocumentCollection.replaceTrigger (trigger: Trigger, callback: (ResourceResponse<Trigger>) -> Unit) {
    return AzureData.replaceTrigger(trigger, trigger.triggerOperation!!, trigger.triggerType!!, this, callback)
}

//endregion

//endregion


//region Document -> Attachment

// create
fun Document.createAttachment (attachmentId: String, contentType: String, mediaUrl: URL, callback: (ResourceResponse<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// create
fun Document.createAttachment (attachmentId: String, contentType: String, mediaUrl: HttpUrl, callback: (ResourceResponse<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// create
fun Document.createAttachment (attachmentId: String, contentType: String, mediaUrl: String, callback: (ResourceResponse<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, mediaUrl, this, callback)
}

// create
fun Document.createAttachment (attachmentId: String, contentType: String, data: ByteArray, callback: (ResourceResponse<Attachment>) -> Unit) {
    return AzureData.createAttachment(attachmentId, contentType, data, this, callback)
}

// list
fun Document.getAttachments (callback: (ResourceListResponse<Attachment>) -> Unit) {
    return AzureData.getAttachments(this, callback)
}

// delete
fun Document.deleteAttachment (attachment: Attachment, callback: (Response) -> Unit) {
    return AzureData.deleteAttachment(attachment, this, callback)
}

// delete
fun Document.deleteAttachment (attachmentRid: String, callback: (Response) -> Unit) {
    return AzureData.deleteAttachment(attachmentRid, this, callback)
}

// replace
fun Document.replaceAttachment (attachmentId: String, attachmentRId: String, contentType: String, mediaUrl: URL, callback: (ResourceResponse<Attachment>) -> Unit) {
    return AzureData.replaceAttachment(attachmentId, attachmentRId, contentType, mediaUrl, this, callback)
}

// replace
fun Document.replaceAttachment (attachmentId: String, attachmentRId: String, contentType: String, mediaUrl: HttpUrl, callback: (ResourceResponse<Attachment>) -> Unit) {
    return AzureData.replaceAttachment(attachmentId, attachmentRId, contentType, mediaUrl, this, callback)
}

// replace
fun Document.replaceAttachment (attachmentId: String, attachmentRId: String, contentType: String, mediaUrl: String, callback: (ResourceResponse<Attachment>) -> Unit) {
    return AzureData.replaceAttachment(attachmentId, attachmentRId, contentType, mediaUrl, this, callback)
}

//endregion


//region User

//region User -> Permission

// create
fun <TResource : Resource> User.createPermission (permissionId: String, permissionMode: Permission.PermissionMode, resource: TResource, callback: (ResourceResponse<Permission>) -> Unit) {
    return AzureData.createPermission(permissionId, permissionMode, resource, this, callback)
}

// list
fun User.getPermissions (callback: (ResourceListResponse<Permission>) -> Unit) {
    return AzureData.getPermissions(this, callback)
}

// get
fun User.getPermission (permissionResourceId: String, callback: (ResourceResponse<Permission>) -> Unit) {
    return AzureData.getPermission(permissionResourceId, this, callback)
}

// delete
fun User.deletePermission (permissionId: String, databaseId: String, callback: (Response) -> Unit) {
    return AzureData.deletePermission(permissionId, this.id, databaseId, callback)
}

// delete
fun User.deletePermission (permission: Permission, callback: (Response) -> Unit) {
    return AzureData.deletePermission(permission, this, callback)
}

// delete
fun User.deletePermission (permissionResourceId: String, callback: (Response) -> Unit) {
    return AzureData.deletePermission(permissionResourceId, this, callback)
}

// replace
fun <TResource : Resource> User.replacePermission (permissionId: String, permissionResourceId: String, permissionMode: Permission.PermissionMode, resource: TResource, callback: (ResourceResponse<Permission>) -> Unit) {
    return AzureData.replacePermission(permissionId, permissionResourceId, permissionMode, resource, this, callback)
}

// replace
fun User.replacePermission (permissionId: String, permissionResourceId: String, permissionMode: Permission.PermissionMode, resourceSelfLink: String, callback: (ResourceResponse<Permission>) -> Unit) {
    return AzureData.replacePermission(permissionId, permissionResourceId, permissionMode, resourceSelfLink, this, callback)
}

// replace
fun User.replacePermission (permission: Permission, callback: (ResourceResponse<Permission>) -> Unit) {
    return AzureData.replacePermission(permission, this, callback)
}

//endregion

//endregion


// Resource

fun <TResource : Resource> TResource.delete (callback: (Response) -> Unit) =
        AzureData.delete(this, callback)

fun <TResource : Resource> TResource.refresh (callback: (ResourceResponse<TResource>) -> Unit) =
        AzureData.refresh(this, callback)

fun <TResource : Resource> TResource.createPermission (permissionId: String, permissionMode: Permission.PermissionMode, user: User, callback: (ResourceResponse<Permission>) -> Unit) =
        AzureData.createPermission(permissionId, permissionMode,this, user, callback)

fun <TResource : Resource> TResource.replacePermission (permissionId: String, permissionMode: Permission.PermissionMode, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) =
        AzureData.replacePermission(permissionId, permissionMode, this.selfLink!!, userId, databaseId, callback)

fun <TResource : Resource> TResource.replacePermission (permission: Permission, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) =
        AzureData.replacePermission(permission.id, permission.permissionMode!!, this, userId, databaseId, callback)

fun <TResource : Resource> TResource.replacePermission (permissionId: String, permissionResourceId: String, permissionMode: Permission.PermissionMode, user: User, callback: (ResourceResponse<Permission>) -> Unit) =
        AzureData.replacePermission(permissionId, permissionResourceId, permissionMode, this, user, callback)