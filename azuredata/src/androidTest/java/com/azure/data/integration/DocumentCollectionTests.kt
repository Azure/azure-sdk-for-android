package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.*
import com.azure.data.model.*
import com.azure.data.model.indexing.*
import org.awaitility.Awaitility.await
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class DocumentCollectionTests : ResourceTest<DocumentCollection>(ResourceType.Collection, true, false) {

    @Test
    fun createCollection() {

        ensureCollection()
    }

    @Test
    fun createCollectionFromDatabase() {

        database?.createCollection(resourceId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
    }

    @Test
    fun listCollections() {

        ensureCollection()

        AzureData.getCollections(databaseId) {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun listCollectionsFromDatabase() {

        ensureCollection()

        database?.getCollections {
            resourceListResponse = it
        }

        await().until {
            resourceListResponse != null
        }

        assertResponseSuccess(resourceListResponse)
        assertTrue(resourceListResponse?.resource?.count!! > 0)
    }

    @Test
    fun getCollection() {

        ensureCollection()

        AzureData.getCollection(resourceId, databaseId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
    }

    @Test
    fun getCollectionFromDatabase() {

        ensureCollection()

        database?.getCollection(resourceId) {
            resourceResponse = it
        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)
    }

    @Test
    fun refreshCollection() {

        val coll = ensureCollection()

        coll.refresh {
            resourceResponse = it
        }

//        AzureData.refresh(coll) {
//            resourceResponse = it
//        }

        await().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(collectionId, resourceResponse?.resource?.id)
    }

    //region Deletes

    @Test
    fun deleteCollection() {

        val coll = ensureCollection()

        coll.delete {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteCollectionByIds() {

        ensureCollection()

        AzureData.deleteCollection(collectionId, databaseId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteCollectionFromDatabase() {

        val coll = ensureCollection()

        database?.deleteCollection(coll) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    @Test
    fun deleteCollectionFromDatabaseById() {

        ensureCollection()

        database?.deleteCollection(collectionId) {
            dataResponse = it
        }

        await().until {
            dataResponse != null
        }

        assertResponseSuccess(dataResponse)
    }

    //endregion

    @Test
    fun replaceCollectionById() {

        ensureCollection()

        val policy = IndexingPolicy.create {
            automatic = true
            mode = IndexingMode.Lazy
            includedPaths {
                includedPath {
                    path = "/*"
                    indexes {
                        index(Index.range(DataType.Number, -1))
                        index {
                            kind = IndexKind.Hash
                            dataType = DataType.String
                            precision = 3
                        }
                        index(Index.spatial(DataType.Point))
                    }
                }
            }
            excludedPaths {
                excludedPath {
                    path = "/test/*"
                }
            }
        }

        AzureData.replaceCollection(resourceId, databaseId, policy) {
            resourceResponse = it
        }

        await().forever().until {
            resourceResponse != null
        }

        assertResponseSuccess(resourceResponse)
        assertEquals(resourceId, resourceResponse?.resource?.id)

        val newPolicy = resourceResponse?.resource?.indexingPolicy!!

        assertEquals(policy.automatic, newPolicy.automatic)
        assertEquals(policy.indexingMode, newPolicy.indexingMode)

        //complicated logic to compare the returned policy follows....
        assertEquals(policy.includedPaths!!.size, newPolicy.includedPaths!!.size)

        for (path in policy.includedPaths!!) {

            for (newPath in newPolicy.includedPaths!!) {

                if (newPath.path != path.path) {

                    if (newPath == newPolicy.includedPaths!!.last()) {
                        throw Exception("Included path not found in indexingPolicy")
                    }
                }
                else {

                    assertEquals(path.indexes!!.size, newPath.indexes!!.size)

                    for (index in path.indexes as List<Index>) {

                        for (newIndex in newPath.indexes as List<Index>) {

                            if (newIndex.dataType == index.dataType ||
                                    newIndex.kind == index.kind ||
                                    newIndex.precision == index.precision) {
                                break
                            }
                            else if (newIndex == newPath.indexes!!.last()) {
                                throw Exception("Index not found in included path")
                            }
                        }
                    }

                    break
                }
            }
        }

        assertEquals(policy.excludedPaths!!.size, newPolicy.excludedPaths!!.size)

        for (path in policy.excludedPaths!!) {

            for (newPath in newPolicy.excludedPaths!!) {

                if (newPath.path != path.path) {

                    if (newPath == newPolicy.excludedPaths!!.last()) {
                        throw Exception("Excluded path not found in indexingPolicy")
                    }
                }
                else {
                    break
                }
            }
        }
    }
}