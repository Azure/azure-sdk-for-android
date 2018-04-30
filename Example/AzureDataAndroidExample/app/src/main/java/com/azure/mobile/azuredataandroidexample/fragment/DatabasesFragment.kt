package com.azure.mobile.azuredataandroidexample.fragment

import android.content.Intent
import android.view.View
import android.widget.EditText
import com.azure.data.AzureData
import com.azure.data.model.Database
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.activity.DatabaseActivity
import com.azure.mobile.azuredataandroidexample.model.ResourceAction
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DatabasesFragment : ResourceListFragment<Database>() {

    override val actionSupport: EnumSet<ResourceAction> = EnumSet.of(ResourceAction.Get, ResourceAction.Create, ResourceAction.Delete)

    override fun fetchData(callback: (ListResponse<Database>) -> Unit) {

        AzureData.getDatabases { response ->
            callback(response)
        }
    }

    override fun getItem(id: String, callback: (Response<Database>) -> Unit) {

        AzureData.getDatabase(id) { response ->
            callback(response)
        }
    }

    override fun createResource(dialogView: View, callback: (Response<Database>) -> Unit) {

        val editText = dialogView.findViewById<EditText>(R.id.editText)
        val resourceId = editText.text.toString()

        AzureData.createDatabase(resourceId) { response ->
            callback(response)
        }
    }

    override fun deleteItem(resourceId: String, callback: (DataResponse) -> Unit) {

        AzureData.deleteDatabase(resourceId) { result ->
            callback(result)
        }
    }

    override fun onItemClick(view: View, item: Database, position: Int) {

        super.onItemClick(view, item, position)

        val db = typedAdapter.getItem(position)

        val intent = Intent(activity?.baseContext, DatabaseActivity::class.java)
        intent.putExtra("db_id", db.id)
        startActivity(intent)
    }
}