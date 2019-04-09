package com.azure.data.util.json

import com.azure.data.model.Resource
import com.azure.data.model.service.ResourceLocation
import com.azure.data.model.service.ResourceWriteOperation
import com.azure.data.service.ResourceWriteOperationType
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
            json.add("location", serialize(it.resourceLocation))
            json.addProperty("path", it.resourceLocalContentPath)
            json.add("headers", serialize(it.httpHeaders))

            return json
        }

        return JsonNull.INSTANCE
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ResourceWriteOperation? {

        json?.asJsonObject?.let {
            val type = it.getAsJsonPrimitive("type").asString
            val resource = it.get("resource")?.let { deserializeResource(it) }
            val location = deserializeLocation(it.getAsJsonObject("location"))
            val path = it.getAsJsonPrimitive("path").asString
            val headers = deserializeHeaders(it.getAsJsonObject("headers"))


            return ResourceWriteOperation(
                    type = ResourceWriteOperationType.valueOf(type),
                    resource = resource,
                    resourceLocation = location,
                    resourceLocalContentPath = path,
                    httpHeaders = headers
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

    private fun serialize(location: ResourceLocation): JsonElement {

        val json = JsonObject()
        json.addProperty("_class", location::class.java.name)
        json.addProperty("location", gson.toJson(location::class.java.cast(location)))

        return json
    }

    private fun deserializeLocation(json: JsonObject): ResourceLocation {

        val locationClass = Class.forName(json.getAsJsonPrimitive("_class").asString)
        return gson.fromJson(json.getAsJsonPrimitive("location").asString, locationClass) as ResourceLocation
    }

    private fun serialize(headers: MutableMap<String, String>): JsonElement {

        val json = JsonObject()

        headers.keys.forEach { header: String ->
            json.addProperty(header, headers[header])
        }

        return json
    }

    private fun deserializeHeaders(json: JsonObject): MutableMap<String, String> {

        val headers = mutableMapOf<String, String>()

        json.keySet().forEach {
            headers[it] = json.getAsJsonPrimitive(it).asString
        }

        return headers
    }
}