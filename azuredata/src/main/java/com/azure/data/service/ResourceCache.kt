package com.azure.data.service

import com.azure.data.model.Resource
import com.azure.data.model.ResourceList
import com.azure.data.model.ResourceLocation

class ResourceCache {

    // region Properties

    companion object {
        var isEnabled = true

        //region cache

        fun <T: Resource> cache(resource: T, replacing: Boolean = false) {
            // TODO
        }

        fun <T: Resource> cache(resources: ResourceList<T>) {
            // TODO
        }

        //endregion


        //region replace

        fun <T: Resource> replace(resource: T, resourceLocation: ResourceLocation) {
            // TODO
        }

        //endregion

        //region get

        fun <T: Resource> getResourceAt(location: ResourceLocation) {
            // TODO
        }

        fun <T: Resource> getResourcesAt(location: ResourceLocation): ResourceList<T> {
            // TODO
        }

        //endregion

        //region remove

        fun removeResourceAt(location: ResourceLocation) {
            // TODO
        }

        //endregion

        //region purge

        fun purge() {
            // TODO
        }

        //endregion
    }

}