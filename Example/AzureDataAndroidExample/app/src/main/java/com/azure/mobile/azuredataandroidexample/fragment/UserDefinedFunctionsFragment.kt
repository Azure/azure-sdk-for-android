package com.azure.mobile.azuredataandroidexample.fragment

import android.os.Bundle
import android.view.View
import com.azure.data.AzureData
import com.azure.data.model.UserDefinedFunction
import com.azure.data.service.DataResponse
import com.azure.data.service.ListResponse
import com.azure.mobile.azuredataandroidexample.model.ResourceAction
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class UserDefinedFunctionsFragment : ResourceListFragment<UserDefinedFunction>() {

    private lateinit var collectionId: String

    override val actionSupport: EnumSet<ResourceAction> = EnumSet.of(ResourceAction.Delete, ResourceAction.CreatePermission)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        activity?.intent?.extras?.let {
            databaseId = it.getString("db_id")!!
            collectionId = it.getString("coll_id")!!
        }
    }

    override fun fetchData(callback: (ListResponse<UserDefinedFunction>) -> Unit) {

        AzureData.getUserDefinedFunctions(collectionId, databaseId) { response ->
            callback(response)
        }
    }

//    override fun getItem(id: String, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {
//
//        AzureData.getStoredProcedure(id, collectionId, databaseId) { response ->
//            callback(response)
//        }
//    }

//    override fun createResource(dialogView: View, callback: (ResourceResponse<UserDefinedFunction>) -> Unit) {
//
//        val editText = dialogView.findViewById<EditText>(R.id.editText)
//        val resourceId = editText.text.toString()
//
//        val storedProcedure = """
//        function () {
//            var context = getContext();
//            var r = context.getResponse();
//
//            r.setBody(\"Hello World!\");
//        }
//        """
//
//        AzureData.createUserDefinedFunction(resourceId, storedProcedure, collectionId, databaseId) { response ->
//            callback(response)
//        }
//    }

    override fun deleteItem(resourceId: String, callback: (DataResponse) -> Unit) {

        AzureData.deleteUserDefinedFunction(resourceId, collectionId, databaseId) { result ->
            callback(result)
        }
    }

    override fun onItemClick(view: View, item: UserDefinedFunction, position: Int) {

        super.onItemClick(view, item, position)

//        val udf = typedAdapter.getItem(position)

//        val intent = Intent(activity.baseContext, CollectionActivity::class.java)
//        intent.putExtra("db_id", databaseId)
//        intent.putExtra("coll_id", coll.id)
//
//        startActivity(intent)
    }
}