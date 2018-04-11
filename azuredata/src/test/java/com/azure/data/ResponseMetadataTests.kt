package com.azure.data

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

import com.azure.core.http.HttpHeader
import com.azure.core.util.dateFromRfc1123
import com.azure.data.constants.MSHttpHeader
import com.azure.data.service.ResponseMetadata
import okhttp3.Protocol
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class ResponseMetadataTests {

    @Before fun before() {}
    @After fun after() {}

    val ACTIVITY_ID = "856acd38-320d-47df-ab6f-9761bb987668"
    val CONTENT_TYPE = "application/json"
    val ETAG = "00003200-0000-0000-0000-56f9e84d0000"
    val DATE = "Tue, 29 Mar 2016 02:28:30 GMT"
    val CONTINUATION = "-RID:K0JYAKIH9QADAAAAAAAAAA==#RT:1#TRC:2"
    val ITEM_COUNT = 10
    val QUOTA = "collections=5000;functions=25;storedProcedures=100;triggers=25;documentsCount=-1;documentSize=10240;documentsSize=10485760;collectionSize=10485760;"
    val USAGE = "collections=13;functions=5;storedProcedures=10;triggers=2;documentsCount=0;documentSize=0;documentsSize=1;collectionSize=1;"
    val SCHEMA_VERSION = "1.1"
    val ALT_CONTENT_PATH = "dbs/testdb/colls/testcoll"
    val REQUEST_CHARGE = "12.38"
    val SERVICE_VERSION = "version=1.6.52.5"
    val SESSION_TOKEN = "0:603"
    val RETRY_AFTER = 5000

    val QCOLLECTIONS = 5000
    val UCOLLECTIONS = 13
    val QFUNCTIONS = 25
    val UFUNCTIONS = 5
    val QSTORED_PROCEDURES = 100
    val USTORED_PROCEDURES = 10
    val QTRIGGERS = 25
    val UTRIGGERS = 2
    val QDOCUMENTS = -1
    val UDOCUMENTS = 0
    val QDOC_SIZE = 10240
    val UDOC_SIZE = 0
    val QDOCS_SIZE = 10485760
    val UDOCS_SIZE = 1
    val QCOLL_SIZE = 10485760
    val UCOLL_SIZE = 1

    @Test @Throws(Exception::class) fun response_metadata() {
        val response = okhttp3.Response.Builder()
                .request(Request.Builder()
                        .url("https://ms.portal.azure.com")
                        .build()
                )
                .code(200)
                .protocol(Protocol.HTTP_1_1)
                .message("A Message")
                .addHeader(HttpHeader.ContentType.name,CONTENT_TYPE)
                .addHeader(HttpHeader.ETag.name,ETAG)
                .addHeader(HttpHeader.Date.name,DATE)
                .addHeader(MSHttpHeader.MSContinuation.name,CONTINUATION)
                .addHeader(MSHttpHeader.MSItemCount.name,ITEM_COUNT.toString())
                .addHeader(MSHttpHeader.MSResourceQuota.name,QUOTA)
                .addHeader(MSHttpHeader.MSResourceUsage.name,USAGE)
                .addHeader(MSHttpHeader.MSSchemaVersion.name,SCHEMA_VERSION)
                .addHeader(MSHttpHeader.MSAltContentPath.name,ALT_CONTENT_PATH)
                .addHeader(MSHttpHeader.MSRequestCharge.name,REQUEST_CHARGE)
                .addHeader(MSHttpHeader.MSServiceVersion.name,SERVICE_VERSION)
                .addHeader(MSHttpHeader.MSActivityId.name,ACTIVITY_ID)
                .addHeader(MSHttpHeader.MSSessionToken.name,SESSION_TOKEN)
                .addHeader(MSHttpHeader.MSRetryAfterMs.name,RETRY_AFTER.toString())
                .build()

        val meta = ResponseMetadata(response)

        assertEquals(ACTIVITY_ID, meta.activityId)
        assertEquals(CONTENT_TYPE, meta.contentType)
        assertEquals(CONTINUATION,meta.continuation)
        assertNotNull(meta.date)
        assertEquals(dateFromRfc1123(DATE),meta.date)
        assertEquals(ETAG,meta.etag)
        assertNotNull(meta.requestCharge)
        meta.requestCharge?.let { assertEquals(REQUEST_CHARGE.toDouble(), it, 0.000000001) }
        assertEquals(SCHEMA_VERSION,meta.schemaVersion)
        assertEquals(SERVICE_VERSION.split("=")[1],meta.serviceVersion)
        assertEquals(SESSION_TOKEN,meta.sessionToken)
        assertNotNull(meta.retryAfter)
        meta.retryAfter?.let { assertEquals(RETRY_AFTER.toLong(), it) }

        assertNotNull(meta.resourceQuota)

        var resources = meta.resourceQuota
        assertEquals(QCOLLECTIONS,resources?.collections)
        assertEquals(QFUNCTIONS,resources?.functions)
        assertEquals(QSTORED_PROCEDURES,resources?.storedProcedures)
        assertEquals(QTRIGGERS,resources?.triggers)
        assertEquals(QDOCUMENTS,resources?.documents)
        assertEquals(QDOC_SIZE,resources?.documentSize)
        assertEquals(QDOCS_SIZE,resources?.documentsSize)
        assertEquals(QCOLL_SIZE,resources?.collectionSize)

        resources = meta.resourceUsage
        assertEquals(UCOLLECTIONS,resources?.collections)
        assertEquals(UFUNCTIONS,resources?.functions)
        assertEquals(USTORED_PROCEDURES,resources?.storedProcedures)
        assertEquals(UTRIGGERS,resources?.triggers)
        assertEquals(UDOCUMENTS,resources?.documents)
        assertEquals(UDOC_SIZE,resources?.documentSize)
        assertEquals(UDOCS_SIZE,resources?.documentsSize)
        assertEquals(UCOLL_SIZE,resources?.collectionSize)
    }
}
