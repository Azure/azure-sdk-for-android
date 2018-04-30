package com.azure.mobile.azuredataandroidexample.fragment

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.azure.data.AzureData
import com.azure.data.model.Permission
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.data.service.Response
import com.azure.mobile.azuredataandroidexample.R
import com.azure.mobile.azuredataandroidexample.model.ResourceAction
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class PermissionsFragment : ResourceListFragment<Permission>() {

    private lateinit var userId: String

    override val actionSupport: EnumSet<ResourceAction> = EnumSet.of(ResourceAction.Get, ResourceAction.Delete)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        activity?.intent?.extras?.let {
            databaseId = it.getString("db_id")
            userId = it.getString("user_id")
        }
    }

    override fun fetchData(callback: (ListResponse<Permission>) -> Unit) {

        AzureData.getPermissions(userId, databaseId) { response ->
            callback(response)
        }
    }

    override fun getItem(id: String, callback: (Response<Permission>) -> Unit) {

        AzureData.getPermission(id, userId, databaseId) { response ->
            callback(response)
        }
    }

//    override fun getResourceCreationDialog(): View {
//
//        val dialog = layoutInflater.inflate(R.layout.dialog_create_permission, null)
//        val messageTextView = dialog.findViewById<TextView>(R.id.messageText)
//        messageTextView.setText(R.string.resource_dialog)
//
//        return dialog
//    }

//    override fun createResource(dialogView: View, callback: (ResourceResponse<Permission>) -> Unit) {
//
//        val resourceId = dialogView.findViewById<EditText>(R.id.editTextId).text.toString()
//        val modeText = dialogView.findViewById<EditText>(R.id.editTextMode).text.toString()
//        val mode = Permission.PermissionMode.valueOf(modeText)
//
//        AzureData.createPermission(resourceId, mode, res) { response ->
//            callback(response)
//        }
//    }

    override fun deleteItem(resourceId: String, callback: (DataResponse) -> Unit) {

        AzureData.deletePermission(resourceId, userId, databaseId) { result ->
            callback(result)
        }
    }
}