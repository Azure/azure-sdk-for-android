package com.azure.data.util.json

import com.azure.data.model.*
import com.google.gson.JsonParser
import java.lang.reflect.Type

internal class ResourceListJsonDeserializer<T: Resource> {

    fun deserialize(json: String, resourceType: Type): ResourceList<T> {
        val resourceList = ResourceList<T>()
        val jsonObject = JsonParser().parse(json).asJsonObject

        jsonObject.keySet().forEach {
            when (it) {
                ResourceList.Companion.Keys.countKey -> resourceList.count = jsonObject.get(it).asInt
                ResourceBase.resourceIdKey -> resourceList.resourceId = jsonObject.get(it).asString

                else -> {
                    resourceList.items = jsonObject.get(it).asJsonArray.map {
                        gson.fromJson<T>(it, resourceType)
                    }
                }
            }
        }

        return resourceList
    }
}