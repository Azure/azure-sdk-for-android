package com.azure.mobile.azuredataandroidexample.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.azure.data.AzureData
import com.azure.data.model.User
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.activity.UserActivity
import com.azure.mobile.azuredataandroidexample.model.ResourceAction
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class UsersFragment : ResourceListFragment<User>() {

    override val actionSupport: EnumSet<ResourceAction> = EnumSet.of(ResourceAction.Get, ResourceAction.Create, ResourceAction.Delete)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        activity?.intent?.extras?.let {
            databaseId = it.getString("db_id")
        }
    }

    override fun fetchData(callback: (ListResponse<User>) -> Unit) {

        AzureData.getUsers(databaseId) { response ->
            callback(response)
        }
    }

    override fun getItem(id: String, callback: (Response<User>) -> Unit) {

        AzureData.getUser(id, databaseId) { response ->
            callback(response)
        }
    }

    override fun createResource(dialogView: View, callback: (Response<User>) -> Unit) {

        val editText = dialogView.findViewById<EditText>(R.id.editText)
        val resourceId = editText.text.toString()

        AzureData.createUser(resourceId, databaseId) { response ->
            callback(response)
        }
    }

    override fun deleteItem(resourceId: String, callback: (DataResponse) -> Unit) {

        AzureData.deleteUser(resourceId, databaseId) { result ->
            callback(result)
        }
    }

    override fun onItemClick(view: View, item: User, position: Int) {

        super.onItemClick(view, item, position)

        val user = typedAdapter.getItem(position)

        val intent = Intent(activity?.baseContext, UserActivity::class.java)
        intent.putExtra("db_id", databaseId)
        intent.putExtra("user_id", user.id)
        startActivity(intent)
    }
}