package com.azure.data

import android.content.Context
import com.azure.data.constants.TokenType
import com.azure.data.model.*
import com.azure.data.model.indexing.IndexingPolicy
import com.azure.data.service.DocumentClient
import com.azure.data.service.ResourceListResponse
import com.azure.data.service.ResourceResponse
import com.azure.data.service.Response
import com.azure.data.util.ContextProvider
import okhttp3.HttpUrl
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class AzureData {

    companion object {

        lateinit var baseUri: ResourceUri
        lateinit var documentClient: DocumentClient

        @JvmStatic
        fun configure(context: Context, name: String, key: String, keyType: TokenType = TokenType.MASTER) {

            ContextProvider.init(context.applicationContext)

            baseUri = ResourceUri(name)
            documentClient = DocumentClient(baseUri, key, keyType)

            isConfigured = true
        }

        @JvmStatic
        var isConfigured: Boolean = false
            private set

        //region Databases

        // create
        @JvmStatic
        fun createDatabase(databaseId: String, callback: (ResourceResponse<Database>) -> Unit) =
                documentClient.createDatabase(databaseId, callback)

        // list
        @JvmStatic
        fun getDatabases(callback: (ResourceListResponse<Database>) -> Unit) =
                documentClient.databases(callback)

        // get
        @JvmStatic
        fun getDatabase(databaseId: String, callback: (ResourceResponse<Database>) -> Unit) =
                documentClient.getDatabase(databaseId, callback)

        // delete
        @JvmStatic
        fun deleteDatabase(database: Database, callback: (Response) -> Unit) =
                documentClient.deleteDatabase(database.id, callback)

        // delete
        @JvmStatic
        fun deleteDatabase(databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteDatabase(databaseId, callback)

        //endregion

        //region Collections

        // create
        @JvmStatic
        fun createCollection(collectionId: String, databaseId: String, callback: (ResourceResponse<DocumentCollection>) -> Unit) =
                documentClient.createCollection(collectionId, databaseId, callback)

        // list
        @JvmStatic
        fun getCollections(databaseId: String, callback: (ResourceListResponse<DocumentCollection>) -> Unit) =
                documentClient.getCollectionsIn(databaseId, callback)

        // get
        @JvmStatic
        fun getCollection(collectionId: String, databaseId: String, callback: (ResourceResponse<DocumentCollection>) -> Unit) =
                documentClient.getCollection(collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteCollection(collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteCollection(collectionId, databaseId, callback)

        // replace
        fun replaceCollection(collectionId: String, databaseId: String, indexingPolicy: IndexingPolicy, callback: (ResourceResponse<DocumentCollection>) -> Unit) =
                documentClient.replaceCollection(collectionId, databaseId, indexingPolicy, callback)

        //endregion

        //region Documents

        // create
        @JvmStatic
        fun <T : Document> createDocument(document: T, collectionId: String, databaseId: String, callback: (ResourceResponse<T>) -> Unit) =
                documentClient.createDocument(document, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun <T : Document> createDocument(document: T, collection: DocumentCollection, callback: (ResourceResponse<T>) -> Unit) =
                documentClient.createDocument(document, collection, callback)

        // list
        @JvmStatic
        fun <T : Document> getDocuments(collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) =
                documentClient.getDocumentsAs(collectionId, databaseId, documentClass, callback)

        // list
        @JvmStatic
        fun <T : Document> getDocuments(collection: DocumentCollection, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) =
                documentClient.getDocumentsAs(collection, documentClass, callback)

        // get
        @JvmStatic
        fun <T : Document> getDocument(documentId: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ResourceResponse<T>) -> Unit) =
                documentClient.getDocument(documentId, collectionId, databaseId, documentClass, callback)

        // get
        @JvmStatic
        fun <T : Document> getDocument(documentResourceId: String, collection: DocumentCollection, documentClass: Class<T>, callback: (ResourceResponse<T>) -> Unit) =
                documentClient.getDocument(documentResourceId, collection, documentClass, callback)

        // delete
        @JvmStatic
        fun deleteDocument(documentId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteDocument(documentId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteDocument(document: Document, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteDocument(document.id, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteDocument(document: Document, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteDocument(document.resourceId!!, collection, callback)

        // delete
        @JvmStatic
        fun deleteDocument(documentResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteDocument(documentResourceId, collection, callback)

        // replace
        @JvmStatic
        fun <T : Document> replaceDocument(document: T, collectionId: String, databaseId: String, callback: (ResourceResponse<T>) -> Unit) =
                documentClient.replaceDocument(document, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun <T : Document> replaceDocument(document: T, collection: DocumentCollection, callback: (ResourceResponse<T>) -> Unit) =
                documentClient.replaceDocument(document, collection, callback)

        // query
        @JvmStatic
        fun <T : Document> queryDocuments(collectionId: String, databaseId: String, query: Query, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) =
                documentClient.queryDocuments(collectionId, databaseId, query, documentClass, callback)

        // query
        @JvmStatic
        fun <T : Document> queryDocuments(collection: DocumentCollection, query: Query, documentClass: Class<T>, callback: (ResourceListResponse<T>) -> Unit) =
                documentClient.queryDocuments(collection, query, documentClass, callback)

        //endregion

        //region Attachments

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, mediaUrl, documentId, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: String, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.parse(mediaUrl)!!, documentId, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: URL, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.get(mediaUrl)!!, documentId, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, media, documentId, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, mediaUrl, document, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: String, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.parse(mediaUrl)!!, document, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: URL, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.get(mediaUrl)!!, document, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, media, document, callback)

        // list
        @JvmStatic
        fun getAttachments(documentId: String, collectionId: String, databaseId: String, callback: (ResourceListResponse<Attachment>) -> Unit) =
                documentClient.getAttachments(documentId, collectionId, databaseId, callback)

        // list
        @JvmStatic
        fun getAttachments(document: Document, callback: (ResourceListResponse<Attachment>) -> Unit) =
                documentClient.getAttachments(document, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachment: Attachment, documentId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteAttachment(attachment.id, documentId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteAttachment(attachmentId, documentId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachment: Attachment, document: Document, callback: (Response) -> Unit) =
                documentClient.deleteAttachment(attachment.resourceId!!, document, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachmentResourceId: String, document: Document, callback: (Response) -> Unit) =
                documentClient.deleteAttachment(attachmentResourceId, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, mediaUrl, documentId, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: String, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, HttpUrl.parse(mediaUrl)!!, documentId, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: URL, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, HttpUrl.get(mediaUrl)!!, documentId, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, media, documentId, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, attachmentResourceId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, attachmentResourceId, contentType, mediaUrl, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, attachmentResourceId: String, contentType: String, mediaUrl: String, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, attachmentResourceId, contentType, HttpUrl.parse(mediaUrl)!!, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, attachmentResourceId: String, contentType: String, mediaUrl: URL, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, attachmentResourceId, contentType, HttpUrl.get(mediaUrl)!!, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, attachmentResourceId: String, contentType: String, media: ByteArray, document: Document, callback: (ResourceResponse<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, attachmentResourceId, contentType, media, document, callback)

        //endregion

        //region Stored Procedures

        // create
        @JvmStatic
        fun createStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (ResourceResponse<StoredProcedure>) -> Unit) =
                documentClient.createStoredProcedure(storedProcedureId, procedure, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (ResourceResponse<StoredProcedure>) -> Unit) =
                documentClient.createStoredProcedure(storedProcedureId, procedure, collection, callback)

        // list
        @JvmStatic
        fun getStoredProcedures(collectionId: String, databaseId: String, callback: (ResourceListResponse<StoredProcedure>) -> Unit) =
                documentClient.getStoredProcedures(collectionId, databaseId, callback)

        // list
        @JvmStatic
        fun getStoredProcedures(collection: DocumentCollection, callback: (ResourceListResponse<StoredProcedure>) -> Unit) =
                documentClient.getStoredProcedures(collection, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedure: StoredProcedure, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedure.id, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedure: StoredProcedure, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedure.resourceId!!, collection, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedureResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedureResourceId, collection, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedureId, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (ResourceResponse<StoredProcedure>) -> Unit) =
                documentClient.replaceStoredProcedure(storedProcedureId, procedure, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceStoredProcedure(storedProcedureId: String, storedProcedureResourceId: String, procedure: String, collection: DocumentCollection, callback: (ResourceResponse<StoredProcedure>) -> Unit) =
                documentClient.replaceStoredProcedure(storedProcedureId, storedProcedureResourceId, procedure, collection, callback)

        // replace
        @JvmStatic
        fun replaceStoredProcedure(storedProcedure: StoredProcedure, collection: DocumentCollection, callback: (ResourceResponse<StoredProcedure>) -> Unit) =
            documentClient.replaceStoredProcedure(storedProcedure.id, storedProcedure.resourceId!!, storedProcedure.body!!, collection, callback)

        // execute
        @JvmStatic
        fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.executeStoredProcedure(storedProcedureId, parameters, collectionId, databaseId, callback)

        // execute
        @JvmStatic
        fun executeStoredProcedure(storedProcedureResourceId: String, parameters: List<String>?, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.executeStoredProcedure(storedProcedureResourceId, parameters, collection, callback)

        //endregion

        //region User Defined Functions

        // create
        @JvmStatic
        fun createUserDefinedFunction(functionId: String, functionBody: String, collectionId: String, databaseId: String, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) =
                documentClient.createUserDefinedFunction(functionId, functionBody, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createUserDefinedFunction(functionId: String, functionBody: String, collection: DocumentCollection, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) =
                documentClient.createUserDefinedFunction(functionId, functionBody, collection, callback)

        // list
        @JvmStatic
        fun getUserDefinedFunctions(collectionId: String, databaseId: String, callback: (ResourceListResponse<UserDefinedFunction>) -> Unit) =
                documentClient.getUserDefinedFunctions(collectionId, databaseId, callback)

        // list
        @JvmStatic
        fun getUserDefinedFunctions(collection: DocumentCollection, callback: (ResourceListResponse<UserDefinedFunction>) -> Unit) =
                documentClient.getUserDefinedFunctions(collection, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunctionId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunctionId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunction: UserDefinedFunction, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunction.id, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunction: UserDefinedFunction, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunction.resourceId!!, collection, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunctionResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunctionResourceId, collection, callback)

        // replace
        @JvmStatic
        fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collectionId: String, databaseId: String, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) =
                documentClient.replaceUserDefinedFunction(userDefinedFunctionId, function, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceUserDefinedFunction(userDefinedFunctionId: String, userDefinedFunctionResourceId: String, function: String, collection: DocumentCollection, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) =
                documentClient.replaceUserDefinedFunction(userDefinedFunctionId, userDefinedFunctionResourceId, function, collection, callback)

        // replace
        @JvmStatic
        fun replaceUserDefinedFunction(userDefinedFunction: UserDefinedFunction, collection: DocumentCollection, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) =
                documentClient.replaceUserDefinedFunction(userDefinedFunction.id, userDefinedFunction.resourceId!!, userDefinedFunction.body!!, collection, callback)

        //endregion

        //region Triggers

        // create
        @JvmStatic
        fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Trigger>) -> Unit) =
                documentClient.createTrigger(triggerId, operation, triggerType, triggerBody, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (ResourceResponse<Trigger>) -> Unit) =
                documentClient.createTrigger(triggerId, operation, triggerType, triggerBody, collection, callback)

        // list
        @JvmStatic
        fun getTriggers(collectionId: String, databaseId: String, callback: (ResourceListResponse<Trigger>) -> Unit) =
                documentClient.getTriggers(collectionId, databaseId, callback)

        // list
        @JvmStatic
        fun getTriggers(collection: DocumentCollection, callback: (ResourceListResponse<Trigger>) -> Unit) =
                documentClient.getTriggers(collection, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(triggerId: String, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteTrigger(triggerId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(trigger: Trigger, collectionId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteTrigger(trigger.id, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(trigger: Trigger, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteTrigger(trigger.resourceId!!, collection, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(triggerResourceId: String, collection: DocumentCollection, callback: (Response) -> Unit) =
                documentClient.deleteTrigger(triggerResourceId, collection, callback)

        // replace
        @JvmStatic
        fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (ResourceResponse<Trigger>) -> Unit) =
                documentClient.replaceTrigger(triggerId, operation, triggerType, triggerBody, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceTrigger(triggerId: String, triggerResourceId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (ResourceResponse<Trigger>) -> Unit) =
                documentClient.replaceTrigger(triggerId, triggerResourceId, operation, triggerType, triggerBody, collection, callback)

        // replace
        @JvmStatic
        fun replaceTrigger(trigger: Trigger, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, collection: DocumentCollection, callback: (ResourceResponse<Trigger>) -> Unit) =
                documentClient.replaceTrigger(trigger.id, trigger.resourceId!!, operation, triggerType, trigger.body!!, collection, callback)

        //endregion

        //region Users

        // create
        @JvmStatic
        fun createUser(userId: String, databaseId: String, callback: (ResourceResponse<User>) -> Unit) =
                documentClient.createUser(userId, databaseId, callback)

        // list
        @JvmStatic
        fun getUsers(databaseId: String, callback: (ResourceListResponse<User>) -> Unit) =
                documentClient.getUsers(databaseId, callback)

        // get
        @JvmStatic
        fun getUser(userId: String, databaseId: String, callback: (ResourceResponse<User>) -> Unit) =
                documentClient.getUser(userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUser(userId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteUser(userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUser(user: User, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deleteUser(user.id, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUser(user: User, database: Database, callback: (Response) -> Unit) =
                documentClient.deleteUser(user.id, database.id, callback)

        // replace
        @JvmStatic
        fun replaceUser(userId: String, newUserId: String, databaseId: String, callback: (ResourceResponse<User>) -> Unit) =
                documentClient.replaceUser(userId, newUserId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceUser(userId: String, newUserId: String, database: Database, callback: (ResourceResponse<User>) -> Unit) =
                documentClient.replaceUser(userId, newUserId, database.id, callback)

        //endregion

        //region Permissions

        // create
        @JvmStatic
        fun createPermission(permissionId: String, permissionMode: Permission.PermissionMode, resource: Resource, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.createPermission(permissionId, permissionMode, resource, userId, databaseId, callback)

        // create
        @JvmStatic
        fun createPermission(permissionId: String, permissionMode: Permission.PermissionMode, resource: Resource, user: User, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.createPermission(permissionId, permissionMode, resource, user, callback)

        // list
        @JvmStatic
        fun getPermissions(userId: String, databaseId: String, callback: (ResourceListResponse<Permission>) -> Unit) =
                documentClient.getPermissions(userId, databaseId, callback)

        // list
        @JvmStatic
        fun getPermissions(user: User, callback: (ResourceListResponse<Permission>) -> Unit) =
                documentClient.getPermissions(user, callback)

        // get
        @JvmStatic
        fun getPermission(permissionId: String, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.getPermission(permissionId, userId, databaseId, callback)

        // get
        @JvmStatic
        fun getPermission(permissionResourceId: String, user: User, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.getPermission(permissionResourceId, user, callback)

        // delete
        @JvmStatic
        fun deletePermission(permissionId: String, userId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deletePermission(permissionId, userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deletePermission(permission: Permission, userId: String, databaseId: String, callback: (Response) -> Unit) =
                documentClient.deletePermission(permission.id, userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deletePermission(permission: Permission, user: User, callback: (Response) -> Unit) =
                documentClient.deletePermission(permission.resourceId!!, user, callback)

        // delete
        @JvmStatic
        fun deletePermission(permissionResourceId: String, user: User, callback: (Response) -> Unit) =
                documentClient.deletePermission(permissionResourceId, user, callback)

        // replace
        @JvmStatic
        fun replacePermission(permissionId: String, permissionMode: Permission.PermissionMode, resourceSelfLink: String, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionMode, resourceSelfLink, userId, databaseId, callback)

        // replace
        @JvmStatic
        fun <TResource : Resource> replacePermission(permissionId: String, permissionMode: Permission.PermissionMode, resource: TResource, userId: String, databaseId: String, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionMode, resource.selfLink!!, userId, databaseId, callback)

        // replace
        @JvmStatic
        fun replacePermission(permissionId: String, permissionResourceId: String, permissionMode: Permission.PermissionMode, resourceSelfLink: String, user: User, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionResourceId, permissionMode, resourceSelfLink, user, callback)

        // replace
        @JvmStatic
        fun <TResource : Resource> replacePermission(permissionId: String, permissionResourceId: String, permissionMode: Permission.PermissionMode, resource: TResource, user: User, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionResourceId, permissionMode, resource.selfLink!!, user, callback)

        // replace
        @JvmStatic
        fun replacePermission(permission: Permission, user: User, callback: (ResourceResponse<Permission>) -> Unit) =
                documentClient.replacePermission(permission.id, permission.resourceId!!, permission.permissionMode!!, permission.resourceLink!!, user, callback)

        //endregion

        //region Offers

        // list
        @JvmStatic
        fun getOffers(callback: (ResourceListResponse<Offer>) -> Unit) =
                documentClient.getOffers(callback)

        // get
        @JvmStatic
        fun getOffer(offerId: String, callback: (ResourceResponse<Offer>) -> Unit) =
                documentClient.getOffer(offerId, callback)

        //endregion

        //region Resources

        // delete
        @JvmStatic
        fun <T : Resource> delete(resource: T, callback: (Response) -> Unit) =
                documentClient.delete(resource, callback)

        // refresh
        @JvmStatic
        fun <T : Resource> refresh(resource: T, callback: (ResourceResponse<T>) -> Unit) =
                documentClient.refresh(resource, callback)

        //endregion
    }
}