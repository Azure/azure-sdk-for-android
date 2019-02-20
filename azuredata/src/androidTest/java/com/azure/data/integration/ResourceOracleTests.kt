package com.azure.data.integration

import android.support.test.runner.AndroidJUnit4
import com.azure.data.model.DocumentCollection
import com.azure.data.model.Resource
import com.azure.data.model.ResourceType
import com.azure.core.util.ContextProvider
import com.azure.data.integration.common.CustomDocument
import com.azure.data.integration.common.ResourceTest
import com.azure.data.util.ResourceOracle
import junit.framework.Assert.assertEquals
import org.junit.AfterClass
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@RunWith(AndroidJUnit4::class)
class ResourceOracleTests : ResourceTest<DocumentCollection>(ResourceType.Collection, true, true, true) {

    @Test
    fun testResourceOracleForDatabase() {

        testResourceOracleForResource(database!!)
    }

    @Test
    fun testResourceOracleForCollection() {

        testResourceOracleForResource(collection!!)
    }

    @Test
    fun testResourceOracleForDocument() {

        testResourceOracleForResource(document!!)
    }

    @Test
    fun testResourceOracleResourceIdRetrieval() {

        testResourceOracleForResource(document!!)

        val newDoc = CustomDocument(document!!.id)

        // test with empty resource but passing selfLink
        var rid = ResourceOracle.shared.getResourceId(newDoc, document!!.selfLink)

        assertEquals(document!!.resourceId, rid)

        // test with resource with altLink
        newDoc.altLink = document!!.altLink

        rid = ResourceOracle.shared.getResourceId(newDoc)

        assertEquals(document!!.resourceId, rid)

        // test with resource with selfLink
        newDoc.altLink = null
        newDoc.selfLink = document!!.selfLink

        rid = ResourceOracle.shared.getResourceId(newDoc)

        assertEquals(document!!.resourceId, rid)
    }

    @Test
    fun testResourceOracleRestore() {

        testResourceOracleForResource(document!!)

        // remove links from memory cache
        ResourceOracle.shared.removeLinks(document!!, false)

        // then reload the link map(s) from disk/prefs
        ResourceOracle.shared.restore(ContextProvider.appContext)

        // NOW, go try and get links and see if they were persisted and restored correctly
        testResourceOracleForResource(document!!)
    }

    @Test
    fun testResourceOracleParentLinks() {

        testResourceOracleForResource(document!!)

        val altLink = ResourceOracle.shared.getParentAltLink(document!!)

        assertEquals(collection!!.altLink, altLink)

        val selfLink = ResourceOracle.shared.getParentSelfLink(document!!)

        assertEquals(collection!!.selfLink, selfLink)
    }

    private fun testResourceOracleForResource(resource: Resource) {

        // make sure the altLink and selfLink returned for the created resource are correct
        var altLink = ResourceOracle.shared.getAltLink(resource.selfLink!!)

        assertEquals(resource.altLink, altLink)

        var selfLink = ResourceOracle.shared.getSelfLink(resource.altLink!!)

        assertEquals(resource.selfLink, selfLink)

        // try with a new/not created resource and see if the oracle returns the correct link(s)
        val newResource = resource::class.java.newInstance()
        newResource.id = resource.id
        newResource.resourceId = resource.resourceId
        newResource.selfLink = resource.selfLink

        // getAltLink for the selfLink and check the result
        altLink = ResourceOracle.shared.getAltLink(newResource.selfLink!!)

        assertEquals(resource.altLink, altLink)

        // try with the resource directly and make sure it returns the same
        altLink = ResourceOracle.shared.getAltLink(newResource)

        assertEquals(resource.altLink, altLink)

        newResource.id = resource.id
        newResource.resourceId = resource.resourceId
        newResource.selfLink = null
        newResource.altLink = resource.altLink

        // getSelfLink for the altLink and check the result
        selfLink = ResourceOracle.shared.getSelfLink(newResource.altLink!!)

        assertEquals(resource.selfLink, selfLink)

        // try with the resource directly and make sure it returns the same
        selfLink = ResourceOracle.shared.getSelfLink(newResource)

        assertEquals(resource.selfLink, selfLink)
    }

    companion object {

        @JvmStatic
        @AfterClass
        fun afterOracleTests() {

            ResourceOracle.shared.purge()
        }
    }
}