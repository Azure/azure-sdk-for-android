package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.core.http.HttpMediaType
import com.azure.core.http.HttpScheme
import com.azure.data.*
import com.azure.data.integration.common.ResourceTest
import com.azure.data.model.*
import com.azure.data.model.service.ListResponse
import com.azure.data.service.PartitionKeyPropertyCache
import com.azure.data.model.service.Response
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.awaitility.Awaitility.await
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
open class AttachmentTests : ResourceTest<Attachment>("AttachmentTests", true, true, true) {

    data class ImageDefinition(val scheme: HttpScheme, val host: String, val path: String, val mimeType: HttpMediaType) {

        val urlString: String = "$scheme://$host$path"
        val url: URL = URL(scheme.toString(), host, path)
        val httpUrl: HttpUrl = HttpUrl.get(urlString)
    }

    enum class UrlMode {

        URL,
        HttpUrl,
        String
    }

    private val imageOne = ImageDefinition(HttpScheme.Https, "lcsc.academyofmine.com", "/wp-content/uploads/2017/06/Test-Logo.svg.png", HttpMediaType.Png)
    private val imageTwo = ImageDefinition(HttpScheme.Https,"assets.pernod-ricard.com", "/nz/media_images/test.jpg", HttpMediaType.Jpeg)

    init {
        partitionKeyPath = "/testKey"
    }

    private fun getPartitionKeyValue() : String {

        return PartitionKeyPropertyCache.getPartitionKeyValues(document!!).single()
    }

    private fun getImageData(image: ImageDefinition) : ByteArray {

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(image.url)
                .get()
                .build()

        val response = client.newCall(request).execute()

        return response.body()!!.bytes()
    }

    private fun createNewBlobAttachment(image: ImageDefinition, doc: Document? = null) : Attachment {

        val bytes = getImageData(image)

        return createNewBlobAttachment(bytes, image.mimeType, doc)
    }

    private fun createNewBlobAttachment(imageBytes: ByteArray, mimeType: HttpMediaType, doc: Document? = null) : Attachment {

        var response: Response<Attachment>? = null

        if (doc != null) {
            document?.createAttachment(createdResourceId, mimeType.value, imageBytes) {
                response = it
            }
        } else {
            AzureData.createAttachment(createdResourceId, mimeType.value, imageBytes, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                response = it
            }
        }

        await().until { response != null }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        return response!!.resource!!
    }

    private fun createNewAttachment(image: ImageDefinition = imageOne, urlMode: UrlMode = UrlMode.String, doc: Document? = null, id: Int? = null) : Attachment {

        var response: Response<Attachment>? = null

        when (urlMode) {

            UrlMode.String -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId(id), image.mimeType.value, image.urlString) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId(id), image.mimeType.value, image.urlString, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                        response = it
                    }
                }
            }
            UrlMode.URL -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId(id), image.mimeType.value, image.url) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId(id), image.mimeType.value, image.url, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                        response = it
                    }
                }
            }
            UrlMode.HttpUrl -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId(id), image.mimeType.value, image.httpUrl) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId(id), image.mimeType.value, image.httpUrl, documentId, collectionId, databaseId, getPartitionKeyValue()) {
                        response = it
                    }
                }
            }
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

        createNewAttachment(urlMode = UrlMode.String)
    }

    @Test
    fun createAttachmentFromHttpUrl() {

        createNewAttachment(urlMode = UrlMode.HttpUrl)
    }

    @Test
    fun createAttachmentForDocument() {

        createNewAttachment(urlMode = UrlMode.URL, doc = document)
    }

    @Test
    fun createBlobAttachment() {

        createNewBlobAttachment(imageOne)
    }

    @Test
    fun createBlobAttachmentForDocument() {

        createNewBlobAttachment(imageOne, document)
    }

    @Test
    fun testListAttachments() {

        //ensure at least 1 attachment
        createNewAttachment()

        AzureData.getAttachments(documentId, collectionId, databaseId, getPartitionKeyValue()) {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listAttachmentsForDocument() {

        //ensure at least 1 attachment
        createNewAttachment(urlMode = UrlMode.URL, doc = document)

        document?.getAttachments {
            resourceListResponse = it
        }

        await().until { resourceListResponse != null }

        assertListResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listAttachmentsPaging() {

        val idsFound = mutableListOf<String>()
        var waitForResponse : ListResponse<Attachment>? = null

        createNewAttachment(imageOne, UrlMode.String, id = 1)
        createNewAttachment(imageTwo, UrlMode.String, id = 2)
        createNewAttachment(imageOne, UrlMode.String, id = 3)

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
    fun getBlobAttachmentBytesForDocument() {

        val bytes = getImageData(imageOne)
        val attachment = createNewBlobAttachment(bytes, imageOne.mimeType, document)

        var byteResponse: Response<ByteArray>? = null

        AzureData.getAttachmentMedia(attachment, document!!) {
            byteResponse = it
        }

        await().until { byteResponse != null }

        assertResourceResponseSuccess(byteResponse)
        assertNotNull(byteResponse!!.resource)
        assertArrayEquals(bytes, byteResponse!!.resource)
    }

    @Test
    fun getBlobAttachmentBytesForDocumentById() {

        val bytes = getImageData(imageOne)
        val attachment = createNewBlobAttachment(bytes, imageOne.mimeType, document)

        var byteResponse: Response<ByteArray>? = null

        AzureData.getAttachmentMedia(attachment.id, document!!) {
            byteResponse = it
        }

        await().until { byteResponse != null }

        assertResourceResponseSuccess(byteResponse)
        assertNotNull(byteResponse!!.resource)
        assertArrayEquals(bytes, byteResponse!!.resource)
    }

    @Test
    fun replaceAttachment() {

        createNewAttachment()

        AzureData.replaceAttachment(createdResourceId, imageOne.mimeType.value, imageOne.url, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            response = it
        }

        await().until { response != null }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        resetResponse()

        AzureData.replaceAttachment(createdResourceId, imageOne.mimeType.value, imageOne.httpUrl, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            response = it
        }

        await().until { response != null }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        resetResponse()

        AzureData.replaceAttachment(createdResourceId, imageOne.mimeType.value, imageOne.urlString, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            response = it
        }

        await().until { response != null }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    @Test
    fun replaceAttachmentForDocument() {

        val attachment = createNewAttachment(imageOne, UrlMode.URL, document)

        document?.replaceAttachment(attachment.id, imageOne.mimeType.value, imageOne.url) {
            response = it
        }

        await().until { response != null }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    @Test
    fun replaceBlobAttachment() {

        createNewBlobAttachment(imageOne)

        // replace with imageTwo
        val bytes = getImageData(imageTwo)

        AzureData.replaceAttachmentMedia(createdResourceId, imageTwo.mimeType.value, bytes, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            dataResponse = it
        }

        await().until { dataResponse != null }

        assertDataResponseSuccess(dataResponse)

        var byteResponse: Response<ByteArray>? = null

        // get the media bytes and compare to what we replaced with
        AzureData.getAttachmentMedia(createdResourceId, document!!) {
            byteResponse = it
        }

        await().until { byteResponse != null }

        assertArrayEquals(bytes, byteResponse!!.resource)
    }

    //region Deletes

    @Test
    fun deleteAttachmentById() {

        createNewAttachment()

        AzureData.deleteAttachment(createdResourceId, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            dataResponse = it
        }

        await().until { dataResponse != null }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteAttachment() {

        val attachment = createNewAttachment()

        AzureData.deleteAttachment(attachment, documentId, collectionId, databaseId, getPartitionKeyValue()) {
            dataResponse = it
        }

        await().until { dataResponse != null }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteAttachmentFromDocument() {

        val attachment = createNewAttachment(urlMode = UrlMode.URL, doc = document)

        document?.deleteAttachment(attachment) {
            dataResponse = it
        }

        await().until { dataResponse != null }

        assertDataResponseSuccess(dataResponse)
    }

    @Test
    fun deleteAttachmentFromDocumentById() {

        val attachment = createNewAttachment(urlMode = UrlMode.URL, doc = document)

        document?.deleteAttachment(attachment.id) {
            dataResponse = it
        }

        await().until { dataResponse != null }

        assertDataResponseSuccess(dataResponse)
    }

    //endregion
}