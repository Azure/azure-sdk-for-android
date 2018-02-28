package com.azure.data.model

import android.net.Uri
import okhttp3.HttpUrl

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

// https://docs.microsoft.com/en-us/rest/api/documentdb/documentdb-resource-uri-syntax-for-rest
class ResourceUri(databaseName: String) {

    private val host: String = "$databaseName.documents.azure.com"

    fun forDatabase(databaseId: String? = null) : UrlLink {

        val baseLink = ""
        val itemLink = getItemLink(ResourceType.Database, baseLink, databaseId)

        return getUrlLink(baseLink, itemLink, databaseId)
    }

    fun forUser(databaseId: String, userId: String? = null) : UrlLink {

        val baseLink = "dbs/$databaseId"
        val itemLink = getItemLink(ResourceType.User, baseLink, userId)

        return getUrlLink(baseLink, itemLink, userId)
    }

    fun forPermission(databaseId: String, userId: String, permissionId: String?): UrlLink {

        val baseLink = "dbs/$databaseId/users/$userId"
        val itemLink = getItemLink(ResourceType.Permission, baseLink, permissionId)

        return getUrlLink(baseLink, itemLink, permissionId)
    }

    fun forPermission(baseLink: String, permissionResourceId: String?): UrlLink {

        val itemLink = getItemLink(ResourceType.Permission, baseLink, permissionResourceId)

        return getUrlLinkForSelf(baseLink, itemLink, permissionResourceId)
    }

    fun forCollection(databaseId: String, collectionId: String? = null) : UrlLink {

        val baseLink = "dbs/$databaseId"
        val itemLink = getItemLink(ResourceType.Collection, baseLink, collectionId)

        return getUrlLink(baseLink, itemLink, collectionId)
    }

    fun forDocument(databaseId: String, collectionId: String, documentId: String? = null) : UrlLink {

        val baseLink = "dbs/$databaseId/colls/$collectionId"
        val itemLink = getItemLink(ResourceType.Document, baseLink, documentId)

        return getUrlLink(baseLink, itemLink, documentId)
    }

    fun forDocument(baseLink: String, documentResourceId: String? = null) : UrlLink {

        val itemLink = getItemLink(ResourceType.Document, baseLink, documentResourceId)

        return getUrlLinkForSelf(baseLink, itemLink, documentResourceId)
    }

    fun forAttachment(databaseId: String, collectionId: String, documentId: String, attachmentId: String? = null) : UrlLink {

        val baseLink = "dbs/$databaseId/colls/$collectionId/docs/$documentId"
        val itemLink = getItemLink(ResourceType.Attachment, baseLink, attachmentId)

        return getUrlLink(baseLink, itemLink, attachmentId)
    }

    fun forAttachment(baseLink: String, resourceId: String? = null) : UrlLink {

        val itemLink = getItemLink(ResourceType.Attachment, baseLink, resourceId)

        return getUrlLinkForSelf(baseLink, itemLink, resourceId)
    }

    fun forStoredProcedure(databaseId: String, collectionId: String, storedProcedureId: String? = null) : UrlLink {

        val baseLink = "dbs/$databaseId/colls/$collectionId"
        val itemLink = getItemLink(ResourceType.StoredProcedure, baseLink, storedProcedureId)

        return getUrlLink(baseLink, itemLink, storedProcedureId)
    }

    fun forStoredProcedure(baseLink: String, storedProcedureResourceId: String? = null) : UrlLink {

        val itemLink = getItemLink(ResourceType.StoredProcedure, baseLink, storedProcedureResourceId)

        return getUrlLinkForSelf(baseLink, itemLink, storedProcedureResourceId)
    }

    fun forTrigger(databaseId: String, collectionId: String, triggerId: String? = null): UrlLink {

        val baseLink = "dbs/$databaseId/colls/$collectionId"
        val itemLink = getItemLink(ResourceType.Trigger, baseLink, triggerId)

        return getUrlLink(baseLink, itemLink, triggerId)
    }

    fun forTrigger(baseLink: String, triggerResourceId: String? = null): UrlLink {

        val itemLink = getItemLink(ResourceType.Trigger, baseLink, triggerResourceId)

        return getUrlLinkForSelf(baseLink, itemLink, triggerResourceId)
    }

    fun forUdf(databaseId: String, collectionId: String, udfId: String? = null): UrlLink {

        val baseLink = "dbs/$databaseId/colls/$collectionId"
        val itemLink = getItemLink(ResourceType.Udf, baseLink, udfId)

        return getUrlLink(baseLink, itemLink, udfId)
    }

    fun forUdf(baseLink: String, udfResourceId: String? = null): UrlLink {

        val itemLink = getItemLink(ResourceType.Udf, baseLink, udfResourceId)

        return getUrlLinkForSelf(baseLink, itemLink, udfResourceId)
    }

    fun forOffer() : UrlLink {

        val baseLink = ""
        val itemLink = getItemLink(ResourceType.Offer, baseLink)

        return getUrlLink(baseLink, itemLink)
    }

    fun forOffer(resourceId: String? = null) : UrlLink {

        val baseLink = ""
        val itemLink = getItemLink(ResourceType.Offer, baseLink, resourceId)

        return getUrlLinkForSelf(baseLink, itemLink, resourceId)
    }

    fun forResource (resource: Resource) : UrlLink {

        if (resource.selfLink.isNullOrEmpty() || resource.resourceId?.isEmpty() != false) {
            throw Exception(ErrorType.IncompleteIds.message)
        }

        val url = HttpUrl.Builder()
                .scheme("https")
                .host(host)
                .addPathSegment(resource.selfLink!!.trimStart('/'))
                .build()

        return UrlLink(url, resource.resourceId!!.toLowerCase())
    }

    private fun getItemLink(resourceType: ResourceType, baseLink: String, resourceId: String? = null) : String {

        val fragment = resourceId ?: ""

        val builder = Uri.Builder()
                .appendEncodedPath(baseLink.trim('/'))
                .appendEncodedPath(resourceType.path)
                .appendEncodedPath(fragment)

        return builder.build().path.trim('/')
    }

    private fun getUrlLink(baseLink: String, itemLink: String, resourceId: String? = null) : UrlLink {
        val url = HttpUrl.Builder()
                .scheme("https")
                .host(host)
                .addPathSegment(itemLink.trimStart('/'))
                .build()

        return UrlLink(url, if (resourceId != null) itemLink else baseLink)
    }

    private fun getUrlLinkForSelf(baseLink: String, itemLink: String, resourceId: String? = null) : UrlLink {
        val url = HttpUrl.Builder()
                .scheme("https")
                .host(host)
                .addPathSegment(itemLink.trimStart('/'))
                .build()

        return UrlLink(url, resourceId?.toLowerCase() ?: baseLink.trimEnd('/').split("/").last().toLowerCase())
    }
}