package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.integration.common.ResourceTest
import com.azure.data.model.*
import com.azure.data.service.ListResponse
import com.azure.data.service.PartitionKeyPropertyCache
import com.azure.data.service.Response
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.awaitility.Awaitility.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
open class AttachmentTests : ResourceTest<Attachment>("AttachmentTests", true, true, true) {

    private val urlString = "https://lcsc.academyofmine.com/wp-content/uploads/2017/06/Test-Logo.svg.png"
    private val url: URL = URL("https", "lcsc.academyofmine.com", "/wp-content/uploads/2017/06/Test-Logo.svg.png")
    private val mimeType = "image/png"

    init {
        partitionKeyPath = "/testKey"
    }

    private fun getPartitionKeyValue() : String {

        return PartitionKeyPropertyCache.getPartitionKeyValues(document!!).single()
    }

    private fun getImageData() : ByteArray {

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        val response = client.newCall(request).execute()

        return response.body()!!.bytes()
    }

    private fun createNewBlobAttachment(imageData: ByteArray, doc: Document? = null) : Attachment {

        var response: Response<Attachment>? = null

        if (doc != null) {
            document?.createAttachment(createdResourceId, mimeType, imageData) {
                response = it
            }
        } else {
            AzureData.createAttachment(createdResourceId, mimeType, imageData, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        return response!!.resource!!
    }

    private fun createNewAttachment(theUrl: Any = url, doc: Document? = null, id: Int? = null) : Attachment {

        var response: Response<Attachment>? = null

        when (theUrl) {

            is String -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId(id), mimeType, theUrl) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId(id), mimeType, theUrl, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                        response = it
                    }
                }
            }
            is URL -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId(id), mimeType, theUrl) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId(id), mimeType, theUrl, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                        response = it
                    }
                }
            }
            is HttpUrl -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId(id), mimeType, theUrl) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId(id), mimeType, theUrl, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                        response = it
                    }
                }
            }
            else -> throw Exception("Unhandled URL case")
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId(id), response?.resource?.id)

        return response!!.resource!!
    }

    @Test
    fun createAttachment() {

        createNewAttachment()
    }

    @Test
    fun createAttachmentFromStringUrl() {

        createNewAttachment(urlString)
    }

    @Test
    fun createAttachmentFromHttpUrl() {

        createNewAttachment(HttpUrl.get(url)!!)
    }

    @Test
    fun createAttachmentForDocument() {

        createNewAttachment(url, document)
    }

    @Test
    fun createBlobAttachment() {

        val bytes = getImageData()

        createNewBlobAttachment(bytes)
    }

    @Test
    fun createBlobAttachmentForDocument() {

        val bytes = getImageData()

        createNewBlobAttachment(bytes, document)
    }

    @Test
    fun testListAttachments() {

        //ensure at least 1 attachment
        createNewAttachment()

        AzureData.getAttachments(documentId, collectionId, databaseId, getPartitionKeyValue()) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listAttachmentsForDocument() {

        //ensure at least 1 attachment
        createNewAttachment(url, document)

        document?.getAttachments {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listAttachmentsPaging() {

        val idsFound = mutableListOf<String>()
        var waitForResponse : ListResponse<Attachment>? = null

        createNewAttachment("http://www.bing.com", id = 1)
        createNewAttachment("http://www.google.com", id = 2)
        createNewAttachment("http://www.yahoo.com", id = 3)

        // Get the first one
        AzureData.getAttachments(documentId, collectionId, databaseId, getPartitionKeyValue(), 1) { waitForResponse = it }

        await().until { waitForResponse != null }

        assertPageN(idsFound, waitForResponse)

        // Get the second one
        waitForResponse.let { response ->

            waitForResponse = null

            response!!.next {

                assertPageN(idsFound, it)
                waitForResponse = it
            }
        }

        await().until { waitForResponse != null }

        // Get the third one
        waitForResponse.let { response ->

            waitForResponse = null

            response!!.next {

                assertPageLast(idsFound, it)
                waitForResponse = it
            }
        }

        await().until { waitForResponse != null }

        // Try to get one more
        waitForResponse!!.next {
            assertPageOnePastLast(it)
        }
    }

    @Test
    fun replaceAttachment() {

        createNewAttachment()

        AzureData.replaceAttachment(createdResourceId, mimeType, url, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        resetResponse()

        AzureData.replaceAttachment(createdResourceId, mimeType, HttpUrl.get(url)!!, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        resetResponse()

        AzureData.replaceAttachment(createdResourceId, mimeType, urlString, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    @Test
    fun replaceAttachmentForDocument() {

        val attachment = createNewAttachment(url, document)

        document?.replaceAttachment(attachment.id, mimeType, url) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    @Test
    fun replaceBlobAttachment() {

        //TODO: figure out why this test is failing!

        val bytes = getImageData()
        createNewBlobAttachment(bytes)

        AzureData.replaceAttachment(createdResourceId, mimeType, bytes, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    //region Deletes

    @Test
    fun deleteAttachmentById() {

        createNewAttachment()

        AzureData.deleteAttachment(createdResourceId, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteAttachment() {

        val attachment = createNewAttachment()

        AzureData.deleteAttachment(attachment, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteAttachmentFromDocument() {

        val attachment = createNewAttachment(url, document)

        document?.deleteAttachment(attachment) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteAttachmentFromDocumentById() {

        val attachment = createNewAttachment(url, document)

        document?.deleteAttachment(attachment.id) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    //endregion
}