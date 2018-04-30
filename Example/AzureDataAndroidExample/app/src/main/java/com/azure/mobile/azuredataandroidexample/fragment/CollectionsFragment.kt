package com.azure.mobile.azuredataandroidexample.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.azure.data.AzureData
import com.azure.data.model.DocumentCollection
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.activity.CollectionActivity
import com.azure.mobile.azuredataandroidexample.model.ResourceAction
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class CollectionsFragment : ResourceListFragment<DocumentCollection>() {

    override val actionSupport: EnumSet<ResourceAction> = EnumSet.of(ResourceAction.Get, ResourceAction.Create, ResourceAction.Delete, ResourceAction.CreatePermission)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        databaseId = activity?.intent?.extras?.getString("db_id") ?: throw Exception("Database Id not found")
    }

    override fun fetchData(callback: (ListResponse<DocumentCollection>) -> Unit) {

        AzureData.getCollections(databaseId) { response ->
            callback(response)
        }
    }

    override fun getItem(id: String, callback: (Response<DocumentCollection>) -> Unit) {

        AzureData.getCollection(id, databaseId) { response ->
            callback(response)
        }
    }

    override fun createResource(dialogView: View, callback: (Response<DocumentCollection>) -> Unit) {

        val editText = dialogView.findViewById<EditText>(R.id.editText)
        val resourceId = editText.text.toString()

        AzureData.createCollection(resourceId, databaseId) { response ->
            callback(response)
        }
    }

    override fun deleteItem(resourceId: String, callback: (DataResponse) -> Unit) {

        AzureData.deleteCollection(resourceId, databaseId) { result ->
            callback(result)
        }
    }

    override fun onItemClick(view: View, item: DocumentCollection, position: Int) {

        super.onItemClick(view, item, position)

        val coll = typedAdapter.getItem(position)

        val intent = Intent(activity?.baseContext, CollectionActivity::class.java)
        intent.putExtra("db_id", databaseId)
        intent.putExtra("coll_id", coll.id)

        startActivity(intent)
    }
}