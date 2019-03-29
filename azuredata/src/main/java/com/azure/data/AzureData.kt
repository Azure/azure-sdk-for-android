package com.azure.data

import android.content.Context
import com.azure.core.util.ContextProvider
import com.azure.data.model.*
import com.azure.data.model.indexing.IndexingPolicy
import com.azure.data.model.partition.PartitionKeyRange
import com.azure.data.service.*
import com.azure.data.util.json.gsonBuilder
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class AzureData {

    companion object {

        internal var documentClient = DocumentClient.shared

        //region Configuration

        private var configured = false

        @JvmStatic
        @JvmOverloads
        fun configure(context: Context, accountName: String, masterKey: String, permissionMode: PermissionMode, configureGsonBuilder: (GsonBuilder) -> Unit = {}) {

            ContextProvider.init(context.applicationContext)

            documentClient.configure(accountName, masterKey, permissionMode)

            configured = true

            configureGsonBuilder(gsonBuilder)
        }

        @JvmStatic
        @JvmOverloads
        fun configure(context: Context, accountUrl: URL, masterKey: String, permissionMode: PermissionMode, configureGsonBuilder: (GsonBuilder) -> Unit = {}) {

            ContextProvider.init(context.applicationContext)

            documentClient.configure(accountUrl, masterKey, permissionMode)

            configured = true

            configureGsonBuilder(gsonBuilder)
        }

        @JvmStatic
        @JvmOverloads
        fun configure(context: Context, accountUrl: HttpUrl, masterKey: String, permissionMode: PermissionMode, configureGsonBuilder: (GsonBuilder) -> Unit = {}) {

            ContextProvider.init(context.applicationContext)

            documentClient.configure(accountUrl, masterKey, permissionMode)

            configured = true

            configureGsonBuilder(gsonBuilder)
        }

        @JvmStatic
        @JvmOverloads
        fun configure(context: Context, accountName: String, permissionProvider: PermissionProvider, configureGsonBuilder: (GsonBuilder) -> Unit = {}) {

            ContextProvider.init(context.applicationContext)

            documentClient.configure(accountName, permissionProvider)

            configured = true

            configureGsonBuilder(gsonBuilder)
        }

        @JvmStatic
        @JvmOverloads
        fun configure(context: Context, accountUrl: URL, permissionProvider: PermissionProvider, configureGsonBuilder: (GsonBuilder) -> Unit = {}) {

            ContextProvider.init(context.applicationContext)

            documentClient.configure(accountUrl, permissionProvider)

            configured = true

            configureGsonBuilder(gsonBuilder)
        }

        @JvmStatic
        @JvmOverloads
        fun configure(context: Context, accountUrl: HttpUrl, permissionProvider: PermissionProvider, configureGsonBuilder: (GsonBuilder) -> Unit = {}) {

            ContextProvider.init(context.applicationContext)

            documentClient.configure(accountUrl, permissionProvider)

            configured = true

            configureGsonBuilder(gsonBuilder)
        }

        //endregion

        @JvmStatic
        val isConfigured: Boolean
            get() = configured && documentClient.isConfigured

        @JvmStatic
        var isOfflineDataEnabled: Boolean
            get() = ResourceCache.shared.isEnabled
            set(value) = { ResourceCache.shared.isEnabled = value }()

        @JvmStatic
        var resourceEncryptor: ResourceEncryptor?
            get() = ResourceCache.shared.resourceEncryptor
            set(value) = { ResourceCache.shared.resourceEncryptor = value }()
        
        //region Databases

        // create
        @JvmStatic
        fun createDatabase(databaseId: String, callback: (Response<Database>) -> Unit) =
                documentClient.createDatabase(databaseId, null, callback)

        // create
        @JvmStatic
        fun createDatabase(databaseId: String, throughput: Int, callback: (Response<Database>) -> Unit) =
                documentClient.createDatabase(databaseId, throughput, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getDatabases(maxPerPage: Int? = null, callback: (ListResponse<Database>) -> Unit) =
                documentClient.getDatabases(maxPerPage, callback)

        // get
        @JvmStatic
        fun getDatabase(databaseId: String, callback: (Response<Database>) -> Unit) =
                documentClient.getDatabase(databaseId, callback)

        // delete
        @JvmStatic
        fun deleteDatabase(database: Database, callback: (DataResponse) -> Unit) =
                documentClient.deleteDatabase(database.id, callback)

        // delete
        @JvmStatic
        fun deleteDatabase(databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteDatabase(databaseId, callback)

        //endregion

        //region Collections

        // create
        @JvmStatic
        @Deprecated("Creating a collection without a partition key is deprecated and will be removed in a future version of AzureData")
        fun createCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.createCollection(collectionId, databaseId, callback)

        // create
        @JvmStatic
        @Deprecated("Creating a collection without a partition key is deprecated and will be removed in a future version of AzureData")
        fun createCollection(collectionId: String, database: Database, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.createCollection(collectionId, database.id, callback)

        // create
        @JvmStatic
        fun createCollection(collectionId: String, partitionKey: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.createCollection(collectionId, null, partitionKey, databaseId, callback)

        // create
        @JvmStatic
        fun createCollection(collectionId: String, partitionKey: String, database: Database, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.createCollection(collectionId, null, partitionKey, database.id, callback)

        // create
        @JvmStatic
        fun createCollection(collectionId: String, throughput: Int, partitionKey: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.createCollection(collectionId, throughput, partitionKey, databaseId, callback)

        // create
        @JvmStatic
        fun createCollection(collectionId: String, throughput: Int, partitionKey: String, database: Database, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.createCollection(collectionId, throughput, partitionKey, database.id, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getCollections(databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<DocumentCollection>) -> Unit) =
                documentClient.getCollectionsIn(databaseId, maxPerPage, callback)

        // get
        @JvmStatic
        fun getCollection(collectionId: String, databaseId: String, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.getCollection(collectionId, databaseId, callback)

        // get
        @JvmStatic
        fun getCollection(collectionId: String, database: Database, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.getCollection(collectionId, database.id, callback)

        // delete
        @JvmStatic
        fun deleteCollection(collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteCollection(collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteCollection(collectionId: String, database: Database, callback: (DataResponse) -> Unit) =
                documentClient.deleteCollection(collectionId, database.id, callback)

        // replace
        @JvmStatic
        fun replaceCollection(collection: DocumentCollection, databaseId: String, indexingPolicy: IndexingPolicy, callback: (Response<DocumentCollection>) -> Unit) =
                documentClient.replaceCollection(collection, databaseId, indexingPolicy, callback)

        // list partition key ranges
        @JvmStatic
        fun getCollectionPartitionKeyRanges(collectionId: String, databaseId: String, callback: (ListResponse<PartitionKeyRange>) -> Unit) =
                documentClient.getCollectionPartitionKeyRanges(collectionId, databaseId, callback)

        //endregion

        //region Documents

        // create
        @JvmStatic
        fun <T : Document> createDocument(document: T, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) =
                documentClient.createDocument(document, null, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun <T : Document> createDocument(document: T, partitionKey: String, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) =
                documentClient.createDocument(document, partitionKey, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun <T : Document> createDocument(document: T, collection: DocumentCollection, callback: (Response<T>) -> Unit) =
                documentClient.createDocument(document, null, collection, callback)

        // create
        @JvmStatic
        fun <T : Document> createDocument(document: T, partitionKey: String, collection: DocumentCollection, callback: (Response<T>) -> Unit) =
                documentClient.createDocument(document, partitionKey, collection, callback)

        // createOrReplace
        @JvmStatic
        fun <T : Document> createOrUpdateDocument(document: T, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) =
                documentClient.createOrUpdateDocument(document, null, collectionId, databaseId, callback)

        // createOrReplace
        @JvmStatic
        fun <T : Document> createOrUpdateDocument(document: T, partitionKey: String, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) =
                documentClient.createOrUpdateDocument(document, partitionKey, collectionId, databaseId, callback)

        // createOrReplace
        @JvmStatic
        fun <T : Document> createOrUpdateDocument(document: T, collection: DocumentCollection, callback: (Response<T>) -> Unit) =
                documentClient.createOrUpdateDocument(document, null, collection, callback)

        // createOrReplace
        @JvmStatic
        fun <T : Document> createOrUpdateDocument(document: T, partitionKey: String, collection: DocumentCollection, callback: (Response<T>) -> Unit) =
                documentClient.createOrUpdateDocument(document, partitionKey, collection, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun <T : Document> getDocuments(collectionId: String, databaseId: String, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) =
                documentClient.getDocumentsAs(collectionId, databaseId, documentClass, maxPerPage, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun <T : Document> getDocuments(collection: DocumentCollection, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) =
                documentClient.getDocumentsAs(collection, documentClass, maxPerPage, callback)

        // get
        @JvmStatic
        @Deprecated("Getting a document without a partition key is deprecated and will be removed in a future version of AzureData")
        fun <T : Document> getDocument(documentId: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (Response<T>) -> Unit) =
                documentClient.getDocument(documentId, null, collectionId, databaseId, documentClass, callback)

        // get
        @JvmStatic
        @Deprecated("Getting a document without a partition key is deprecated and will be removed in a future version of AzureData")
        fun <T : Document> getDocument(documentId: String, collection: DocumentCollection, documentClass: Class<T>, callback: (Response<T>) -> Unit) =
                documentClient.getDocument(documentId, null, collection, documentClass, callback)

        // get
        @JvmStatic
        fun <T : Document> getDocument(documentId: String, partitionKey: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (Response<T>) -> Unit) =
                documentClient.getDocument(documentId, partitionKey, collectionId, databaseId, documentClass, callback)

        // get
        @JvmStatic
        fun <T : Document> getDocument(documentId: String, partitionKey: String, collection: DocumentCollection, documentClass: Class<T>, callback: (Response<T>) -> Unit) =
                documentClient.getDocument(documentId, partitionKey, collection, documentClass, callback)

        // delete
        @JvmStatic
        @Deprecated("Deleting a document without a partition key is deprecated and will be removed in a future version of AzureData")
        fun deleteDocument(documentId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteDocument(documentId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        @Deprecated("Deleting a document without a partition key is deprecated and will be removed in a future version of AzureData")
        fun deleteDocument(documentId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteDocument(documentId, collection, callback)

        // delete
        @JvmStatic
        fun deleteDocument(document: Document, callback: (DataResponse) -> Unit) =
                documentClient.delete(document, callback)

        // delete
        @JvmStatic
        fun deleteDocument(documentId: String, partitionKey: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteDocument(documentId, partitionKey, collectionId, databaseId, callback)

        // delete
        fun deleteDocument(documentId: String, partitionKey: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteDocument(documentId, partitionKey, collection, callback)

        // replace
        @JvmStatic
        fun <T : Document> replaceDocument(document: T, collectionId: String, databaseId: String, callback: (Response<T>) -> Unit) =
                documentClient.replaceDocument(document, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun <T : Document> replaceDocument(document: T, collection: DocumentCollection, callback: (Response<T>) -> Unit) =
                documentClient.replaceDocument(document, collection, callback)

        // query
        @JvmStatic
        @JvmOverloads
        fun <T : Document> queryDocuments(collectionId: String, databaseId: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) =
                documentClient.queryDocuments(collectionId, databaseId, query, documentClass, maxPerPage, callback)

        // query
        @JvmStatic
        @JvmOverloads
        fun <T : Document> queryDocuments(collectionId: String, partitionKey: String, databaseId: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) =
                documentClient.queryDocuments(collectionId, partitionKey, databaseId, query, documentClass, maxPerPage, callback)

        // query
        @JvmStatic
        @JvmOverloads
        fun <T : Document> queryDocuments(collection: DocumentCollection, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) =
                documentClient.queryDocuments(collection, query, documentClass, maxPerPage, callback)

        // query
        @JvmStatic
        @JvmOverloads
        fun <T : Document> queryDocuments(collection: DocumentCollection, partitionKey: String, query: Query, documentClass: Class<T>, maxPerPage: Int? = null, callback: (ListResponse<T>) -> Unit) =
                documentClient.queryDocuments(collection, partitionKey, query, documentClass, maxPerPage, callback)

        // find
        @JvmStatic
        fun <T : Document> findDocument(documentId: String, collectionId: String, databaseId: String, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) =
                documentClient.findDocument(documentId, collectionId, databaseId, documentClass, callback)

        // find
        @JvmStatic
        fun <T : Document> findDocument(documentId: String, collection: DocumentCollection, documentClass: Class<T>, callback: (ListResponse<T>) -> Unit) =
                documentClient.findDocument(documentId, collection, documentClass, callback)

        //endregion

        //region Attachments

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, mediaUrl, documentId, collectionId, databaseId, partitionKey, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: String, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.parse(mediaUrl)!!, documentId, collectionId, databaseId, partitionKey, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: URL, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.get(mediaUrl)!!, documentId, collectionId, databaseId, partitionKey, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, media, documentId, collectionId, databaseId, partitionKey, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, mediaUrl, document, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: String, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.parse(mediaUrl)!!, document, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, mediaUrl: URL, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, HttpUrl.get(mediaUrl)!!, document, callback)

        // create
        @JvmStatic
        fun createAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.createAttachment(attachmentId, contentType, media, document, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getAttachments(documentId: String, collectionId: String, databaseId: String, partitionKey: String, maxPerPage: Int? = null, callback: (ListResponse<Attachment>) -> Unit) =
                documentClient.getAttachments(documentId, collectionId, databaseId, partitionKey, maxPerPage, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getAttachments(document: Document, maxPerPage: Int? = null, callback: (ListResponse<Attachment>) -> Unit) =
                documentClient.getAttachments(document, maxPerPage, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachment: Attachment, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteAttachment(attachment.id, documentId, collectionId, databaseId, partitionKey, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachmentId: String, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteAttachment(attachmentId, documentId, collectionId, databaseId, partitionKey, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachment: Attachment, document: Document, callback: (DataResponse) -> Unit) =
                documentClient.deleteAttachment(attachment.id, document, callback)

        // delete
        @JvmStatic
        fun deleteAttachment(attachmentId: String, document: Document, callback: (DataResponse) -> Unit) =
                documentClient.deleteAttachment(attachmentId, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, mediaUrl, documentId, collectionId, databaseId, partitionKey, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: String, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, HttpUrl.parse(mediaUrl)!!, documentId, collectionId, databaseId, partitionKey, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: URL, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, HttpUrl.get(mediaUrl)!!, documentId, collectionId, databaseId, partitionKey, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, documentId: String, collectionId: String, databaseId: String, partitionKey: String, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, media, documentId, collectionId, databaseId, partitionKey, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: HttpUrl, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, mediaUrl, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: String, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, HttpUrl.parse(mediaUrl)!!, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, mediaUrl: URL, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, HttpUrl.get(mediaUrl)!!, document, callback)

        // replace
        @JvmStatic
        fun replaceAttachment(attachmentId: String, contentType: String, media: ByteArray, document: Document, callback: (Response<Attachment>) -> Unit) =
                documentClient.replaceAttachment(attachmentId, contentType, media, document, callback)

        //endregion

        //region Stored Procedures

        // create
        @JvmStatic
        fun createStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) =
                documentClient.createStoredProcedure(storedProcedureId, procedure, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) =
                documentClient.createStoredProcedure(storedProcedureId, procedure, collection, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getStoredProcedures(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<StoredProcedure>) -> Unit) =
                documentClient.getStoredProcedures(collectionId, databaseId, maxPerPage, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getStoredProcedures(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<StoredProcedure>) -> Unit) =
                documentClient.getStoredProcedures(collection, maxPerPage, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedure: StoredProcedure, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedure.id, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedure: StoredProcedure, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedure.id, collection, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedureId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedureId, collection, callback)

        // delete
        @JvmStatic
        fun deleteStoredProcedure(storedProcedureId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteStoredProcedure(storedProcedureId, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collectionId: String, databaseId: String, callback: (Response<StoredProcedure>) -> Unit) =
                documentClient.replaceStoredProcedure(storedProcedureId, procedure, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceStoredProcedure(storedProcedureId: String, procedure: String, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) =
                documentClient.replaceStoredProcedure(storedProcedureId, procedure, collection, callback)

        // replace
        @JvmStatic
        fun replaceStoredProcedure(storedProcedure: StoredProcedure, collection: DocumentCollection, callback: (Response<StoredProcedure>) -> Unit) =
            documentClient.replaceStoredProcedure(storedProcedure.id, storedProcedure.body!!, collection, callback)

        // execute
        @JvmStatic
        fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.executeStoredProcedure(storedProcedureId, parameters, null, collectionId, databaseId, callback)

        // execute
        @JvmStatic
        fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, partitionKey: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.executeStoredProcedure(storedProcedureId, parameters, partitionKey, collectionId, databaseId, callback)

        // execute
        @JvmStatic
        fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.executeStoredProcedure(storedProcedureId, parameters, null, collection, callback)

        // execute
        @JvmStatic
        fun executeStoredProcedure(storedProcedureId: String, parameters: List<String>?, partitionKey: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.executeStoredProcedure(storedProcedureId, parameters, partitionKey, collection, callback)

        //endregion

        //region User Defined Functions

        // create
        @JvmStatic
        fun createUserDefinedFunction(functionId: String, functionBody: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) =
                documentClient.createUserDefinedFunction(functionId, functionBody, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createUserDefinedFunction(functionId: String, functionBody: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) =
                documentClient.createUserDefinedFunction(functionId, functionBody, collection, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getUserDefinedFunctions(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<UserDefinedFunction>) -> Unit) =
                documentClient.getUserDefinedFunctions(collectionId, databaseId, maxPerPage, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getUserDefinedFunctions(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<UserDefinedFunction>) -> Unit) =
                documentClient.getUserDefinedFunctions(collection, maxPerPage, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunctionId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunctionId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunction: UserDefinedFunction, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunction.id, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunction: UserDefinedFunction, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunction.id, collection, callback)

        // delete
        @JvmStatic
        fun deleteUserDefinedFunction(userDefinedFunctionId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteUserDefinedFunction(userDefinedFunctionId, collection, callback)

        // replace
        @JvmStatic
        fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collectionId: String, databaseId: String, callback: (Response<UserDefinedFunction>) -> Unit) =
                documentClient.replaceUserDefinedFunction(userDefinedFunctionId, function, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceUserDefinedFunction(userDefinedFunctionId: String, function: String, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) =
                documentClient.replaceUserDefinedFunction(userDefinedFunctionId, function, collection, callback)

        // replace
        @JvmStatic
        fun replaceUserDefinedFunction(userDefinedFunction: UserDefinedFunction, collection: DocumentCollection, callback: (Response<UserDefinedFunction>) -> Unit) =
                documentClient.replaceUserDefinedFunction(userDefinedFunction.id, userDefinedFunction.body!!, collection, callback)

        //endregion

        //region Triggers

        // create
        @JvmStatic
        fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (Response<Trigger>) -> Unit) =
                documentClient.createTrigger(triggerId, operation, triggerType, triggerBody, collectionId, databaseId, callback)

        // create
        @JvmStatic
        fun createTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (Response<Trigger>) -> Unit) =
                documentClient.createTrigger(triggerId, operation, triggerType, triggerBody, collection, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getTriggers(collectionId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<Trigger>) -> Unit) =
                documentClient.getTriggers(collectionId, databaseId, maxPerPage, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getTriggers(collection: DocumentCollection, maxPerPage: Int? = null, callback: (ListResponse<Trigger>) -> Unit) =
                documentClient.getTriggers(collection, maxPerPage, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(triggerId: String, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteTrigger(triggerId, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(trigger: Trigger, collectionId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteTrigger(trigger.id, collectionId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(trigger: Trigger, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteTrigger(trigger.id, collection, callback)

        // delete
        @JvmStatic
        fun deleteTrigger(triggerId: String, collection: DocumentCollection, callback: (DataResponse) -> Unit) =
                documentClient.deleteTrigger(triggerId, collection, callback)

        // replace
        @JvmStatic
        fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collectionId: String, databaseId: String, callback: (Response<Trigger>) -> Unit) =
                documentClient.replaceTrigger(triggerId, operation, triggerType, triggerBody, collectionId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceTrigger(triggerId: String, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, triggerBody: String, collection: DocumentCollection, callback: (Response<Trigger>) -> Unit) =
                documentClient.replaceTrigger(triggerId, operation, triggerType, triggerBody, collection, callback)

        // replace
        @JvmStatic
        fun replaceTrigger(trigger: Trigger, operation: Trigger.TriggerOperation, triggerType: Trigger.TriggerType, collection: DocumentCollection, callback: (Response<Trigger>) -> Unit) =
                documentClient.replaceTrigger(trigger.id, operation, triggerType, trigger.body!!, collection, callback)

        //endregion

        //region Users

        // create
        @JvmStatic
        fun createUser(userId: String, databaseId: String, callback: (Response<User>) -> Unit) =
                documentClient.createUser(userId, databaseId, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getUsers(databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<User>) -> Unit) =
                documentClient.getUsers(databaseId, maxPerPage, callback)

        // get
        @JvmStatic
        fun getUser(userId: String, databaseId: String, callback: (Response<User>) -> Unit) =
                documentClient.getUser(userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUser(userId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteUser(userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUser(user: User, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deleteUser(user.id, databaseId, callback)

        // delete
        @JvmStatic
        fun deleteUser(user: User, database: Database, callback: (DataResponse) -> Unit) =
                documentClient.deleteUser(user.id, database.id, callback)

        // replace
        @JvmStatic
        fun replaceUser(userId: String, newUserId: String, databaseId: String, callback: (Response<User>) -> Unit) =
                documentClient.replaceUser(userId, newUserId, databaseId, callback)

        // replace
        @JvmStatic
        fun replaceUser(userId: String, newUserId: String, database: Database, callback: (Response<User>) -> Unit) =
                documentClient.replaceUser(userId, newUserId, database.id, callback)

        //endregion

        //region Permissions

        // create
        @JvmStatic
        fun createPermission(permissionId: String, permissionMode: PermissionMode, resource: Resource, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) =
                documentClient.createPermission(permissionId, permissionMode, resource, userId, databaseId, callback)

        // create
        @JvmStatic
        fun createPermission(permissionId: String, permissionMode: PermissionMode, resource: Resource, user: User, callback: (Response<Permission>) -> Unit) =
                documentClient.createPermission(permissionId, permissionMode, resource, user, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getPermissions(userId: String, databaseId: String, maxPerPage: Int? = null, callback: (ListResponse<Permission>) -> Unit) =
                documentClient.getPermissions(userId, databaseId, maxPerPage, callback)

        // list
        @JvmStatic
        @JvmOverloads
        fun getPermissions(user: User, maxPerPage: Int? = null, callback: (ListResponse<Permission>) -> Unit) =
                documentClient.getPermissions(user, maxPerPage, callback)

        // get
        @JvmStatic
        fun getPermission(permissionId: String, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) =
                documentClient.getPermission(permissionId, userId, databaseId, callback)

        // get
        @JvmStatic
        fun getPermission(permissionId: String, user: User, callback: (Response<Permission>) -> Unit) =
                documentClient.getPermission(permissionId, user, callback)

        // delete
        @JvmStatic
        fun deletePermission(permissionId: String, userId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deletePermission(permissionId, userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deletePermission(permission: Permission, userId: String, databaseId: String, callback: (DataResponse) -> Unit) =
                documentClient.deletePermission(permission.id, userId, databaseId, callback)

        // delete
        @JvmStatic
        fun deletePermission(permission: Permission, user: User, callback: (DataResponse) -> Unit) =
                documentClient.deletePermission(permission.id, user, callback)

        // delete
        @JvmStatic
        fun deletePermission(permissionId: String, user: User, callback: (DataResponse) -> Unit) =
                documentClient.deletePermission(permissionId, user, callback)

        // replace
        @JvmStatic
        fun replacePermission(permissionId: String, permissionMode: PermissionMode, resourceSelfLink: String, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionMode, resourceSelfLink, userId, databaseId, callback)

        // replace
        @JvmStatic
        fun <TResource : Resource> replacePermission(permissionId: String, permissionMode: PermissionMode, resource: TResource, userId: String, databaseId: String, callback: (Response<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionMode, resource.selfLink!!, userId, databaseId, callback)

        // replace
        @JvmStatic
        fun replacePermission(permissionId: String, permissionMode: PermissionMode, resourceSelfLink: String, user: User, callback: (Response<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionMode, resourceSelfLink, user, callback)

        // replace
        @JvmStatic
        fun <TResource : Resource> replacePermission(permissionId: String, permissionMode: PermissionMode, resource: TResource, user: User, callback: (Response<Permission>) -> Unit) =
                documentClient.replacePermission(permissionId, permissionMode, resource.selfLink!!, user, callback)

        // replace
        @JvmStatic
        fun replacePermission(permission: Permission, user: User, callback: (Response<Permission>) -> Unit) =
                documentClient.replacePermission(permission.id, permission.permissionMode!!, permission.resourceLink!!, user, callback)

        //endregion

        //region Offers

        // list
        @JvmStatic
        @JvmOverloads
        fun getOffers(maxPerPage: Int? = null, callback: (ListResponse<Offer>) -> Unit) =
                documentClient.getOffers(maxPerPage, callback)

        // get
        @JvmStatic
        fun getOffer(offerId: String, callback: (Response<Offer>) -> Unit) =
                documentClient.getOffer(offerId, callback)

        //endregion

        //region Resources

        // delete
        @JvmStatic
        fun <T : Resource> delete(resource: T, callback: (DataResponse) -> Unit) =
                documentClient.delete(resource, callback)

        // refresh
        @JvmStatic
        fun <T : Resource> refresh(resource: T, callback: (Response<T>) -> Unit) =
                documentClient.refresh(resource, callback)

        //endregion
    }
}