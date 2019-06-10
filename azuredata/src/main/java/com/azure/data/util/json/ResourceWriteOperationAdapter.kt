package com.azure.data.util.json

import com.azure.data.model.Resource
import com.azure.data.model.service.RequestDetails
import com.azure.data.model.service.ResourceWriteOperation
import com.azure.data.model.service.ResourceWriteOperationType
import com.google.gson.*
import java.lang.reflect.Type

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class ResourceWriteOperationAdapter: JsonSerializer<ResourceWriteOperation>, JsonDeserializer<ResourceWriteOperation> {

    override fun serialize(src: ResourceWriteOperation?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {

        src?.let {
            val json = JsonObject()
            json.addProperty("type", it.type.toString())
            json.add("resource", serialize(it.resource))
            json.add("details", serialize(it.requestDetails))
            json.addProperty("path", it.resourceLocalContentPath)

            return json
        }

        return JsonNull.INSTANCE
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ResourceWriteOperation? {

        json?.asJsonObject?.let {
            val type = it.getAsJsonPrimitive("type").asString
            val resource = it.get("resource")?.let { deserializeResource(it) }
            val details = deserializeDetails(it.getAsJsonObject("details"))
            val path = it.getAsJsonPrimitive("path").asString

            return ResourceWriteOperation(
                    type = ResourceWriteOperationType.valueOf(type),
                    resource = resource,
                    requestDetails = details,
                    resourceLocalContentPath = path
            )
        }

        return null
    }

    private fun serialize(resource: Resource?): JsonElement {

        resource?.let {

            val json = JsonObject()
            json.addProperty("_class", it::class.java.name)
            json.addProperty("resource", gson.toJson(it.javaClass.cast(it)))

            return json
        }

        return JsonNull.INSTANCE
    }

    private fun deserializeResource(json: JsonElement): Resource? {

        return when (json) {

            is JsonNull -> null
            is JsonObject -> {

                val resourceClass = Class.forName(json.getAsJsonPrimitive("_class").asString)
                return gson.fromJson(json.getAsJsonPrimitive("resource").asString, resourceClass) as Resource
            }
            else -> {
                return null
            }
        }
    }

    private fun serialize(details: RequestDetails): JsonElement {

        val json = JsonObject()
        json.addProperty("_class", details::class.java.name)
        json.addProperty("details", gson.toJson(details::class.java.cast(details)))

        return json
    }

    private fun deserializeDetails(json: JsonObject): RequestDetails {

        val detailsClass = Class.forName(json.getAsJsonPrimitive("_class").asString)
        return gson.fromJson(json.getAsJsonPrimitive("details").asString, detailsClass) as RequestDetails
    }

    private fun serialize(headers: MutableMap<String, String>): JsonElement {

        val json = JsonObject()

        headers.keys.forEach { header: String ->
            json.addProperty(header, headers[header])
        }

        return json
    }
}