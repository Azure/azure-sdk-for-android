package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.model.*
import com.azure.data.service.Response
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.awaitility.Awaitility.await
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class AttachmentTests : ResourceTest<Attachment>(ResourceType.Attachment, true, true, true) {

    private val urlString = "https://azuredatatests.blob.core.windows.net/attachment-tests/youre%20welcome.jpeg?st=2017-11-07T14%3A00%3A00Z&se=2020-11-08T14%3A00%3A00Z&sp=rl&sv=2017-04-17&sr=c&sig=RAHr6Mee%2Bt7RrDnGHyjgSX3HSqJgj8guhy0IrEMh3KQ%3D"
    private val url: URL = URL("https", "azuredatatests.blob.core.windows.net", "/attachment-tests/youre%20welcome.jpeg?st=2017-11-07T14%3A00%3A00Z&se=2020-11-08T14%3A00%3A00Z&sp=rl&sv=2017-04-17&sr=c&sig=RAHr6Mee%2Bt7RrDnGHyjgSX3HSqJgj8guhy0IrEMh3KQ%3D")

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
            document?.createAttachment(createdResourceId, "image/jpeg", imageData) {
                response = it
            }
        } else {
            AzureData.createAttachment(createdResourceId, "image/jpeg", imageData, documentId, collectionId, databaseId) {
                response = it
            }
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        Assert.assertEquals(createdResourceId, response?.resource?.id)

        return response!!.resource!!
    }

    private fun createNewAttachment(theUrl: Any = url, doc: Document? = null) : Attachment {

        var response: Response<Attachment>? = null

        when (theUrl) {

            is String -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId, "image/jpeg", theUrl) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId, "image/jpeg", theUrl, documentId, collectionId, databaseId) {
                        response = it
                    }
                }
            }
            is URL -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId, "image/jpeg", theUrl) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId, "image/jpeg", theUrl, documentId, collectionId, databaseId) {
                        response = it
                    }
                }
            }
            is HttpUrl -> {

                if (doc != null) {
                    document?.createAttachment(createdResourceId, "image/jpeg", theUrl) {
                        response = it
                    }
                } else {
                    AzureData.createAttachment(createdResourceId, "image/jpeg", theUrl, documentId, collectionId, databaseId) {
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
        Assert.assertEquals(createdResourceId, response?.resource?.id)

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

        AzureData.getAttachments(documentId, collectionId, databaseId) {
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
    fun replaceAttachment() {

        createNewAttachment()

        AzureData.replaceAttachment(createdResourceId, "image/jpeg", url, documentId, collectionId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        resetResponse()

        AzureData.replaceAttachment(createdResourceId, "image/jpeg", HttpUrl.get(url)!!, documentId, collectionId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)

        resetResponse()

        AzureData.replaceAttachment(createdResourceId, "image/jpeg", urlString, documentId, collectionId, databaseId) {
            response = it
        }

        await().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

//    @Test
//    fun replaceAttachmentForDocument() {
//
//        val attachment = createNewAttachment(url, document)
//
//        document?.replaceAttachment(createdResourceId, attachment.createdResourceId!!, "image/jpeg", url) {
//            response = it
//        }
//
//        await().until {
//            response != null
//        }
//
//        assertResourceResponseSuccess(response)
//        assertEquals(createdResourceId, response?.resource?.id)
//    }

    @Test
    fun replaceBlobAttachment() {

        //TODO: figure out why this test is failing!

        val bytes = getImageData()
        createNewBlobAttachment(bytes)

        AzureData.replaceAttachment(createdResourceId, "image/jpeg", bytes, documentId, collectionId, databaseId) {
            response = it
        }

        await().forever().until {
            response != null
        }

        assertResourceResponseSuccess(response)
        assertEquals(createdResourceId, response?.resource?.id)
    }

    //region Deletes

    @Test
    fun deleteAttachmentById() {

        createNewAttachment()

        AzureData.deleteAttachment(createdResourceId, documentId, collectionId, databaseId) {
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

        AzureData.deleteAttachment(attachment, documentId, collectionId, databaseId) {
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
    fun deleteAttachmentFromDocumentByRId() {

        val attachment = createNewAttachment(url, document)

        document?.deleteAttachment(attachment.resourceId!!) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertDataResponseSuccess(dataResponse)
    }

    //endregion
}