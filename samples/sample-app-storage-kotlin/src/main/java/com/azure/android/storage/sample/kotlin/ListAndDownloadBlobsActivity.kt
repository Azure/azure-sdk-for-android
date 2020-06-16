package com.azure.android.storage.sample.kotlin

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.NetworkType
import com.azure.android.core.util.CoreUtil
import com.azure.android.storage.blob.StorageBlobClient
import com.azure.android.storage.blob.models.BlobItem
import com.azure.android.storage.blob.transfer.TransferClient
import com.azure.android.storage.sample.kotlin.config.StorageConfiguration
import com.azure.android.storage.sample.kotlin.core.util.tokenrequest.TokenRequestObservable
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException

import com.azure.android.storage.sample.kotlin.core.util.paging.PageLoadState
import com.azure.android.storage.sample.kotlin.core.util.tokenrequest.TokenRequestObserver
import com.azure.android.storage.sample.kotlin.core.util.tokenrequest.TokenResponseCallback

class ListAndDownloadBlobsActivity : AppCompatActivity() {
    private lateinit var viewModel: ContainerBlobsPaginationViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listblobs)

        recyclerView = findViewById(R.id.blob_list)
        searchText = findViewById(R.id.input_container_name)

        val storageConfiguration = StorageConfiguration.create(applicationContext)
        val storageBlobClient : StorageBlobClient = StorageBlobClient
            .Builder()
            .setBlobServiceUrl(storageConfiguration.blobServiceUrl)
            .build()

        viewModel = ViewModelProvider(this, ContainerBlobsViewModelFactory(storageBlobClient))
            .get(ContainerBlobsPaginationViewModel::class.java)

        // Set up Login
        val tokenRequestObservable: TokenRequestObservable = viewModel.getTokenRequestObservable()
        val lifecycleOwner: LifecycleOwner = this
        PublicClientApplication.createMultipleAccountPublicClientApplication(
            this.applicationContext,
            R.raw.authorization_configuration,
            object : IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                    tokenRequestObservable.observe(lifecycleOwner, object : TokenRequestObserver() {
                        override fun onTokenRequest(scopes: Array<String>, callback: TokenResponseCallback) {
                            MsalClient.signIn(application, activity, scopes, callback)
                        }
                    })
                }

                override fun onError(exception: MsalException) {
                    Log.e(TAG, "Exception found when trying to sign in.", exception)
                }
            })
        val repository: ContainerBlobsPaginationRepository = viewModel.repository as ContainerBlobsPaginationRepository
        val transferClient = TransferClient.Builder(applicationContext)
            .addStorageBlobClient(Constants.STORAGE_BLOB_CLIENT_ID, repository.storageBlobClient)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Set up PagedList Adapter
        val adapter = ContainerBlobsPagedListAdapter(transferClient, Runnable { viewModel.retry() })
        recyclerView.adapter = adapter
        viewModel.getPagedListObservable().observe(this, getPagedListObserver(adapter, recyclerView))
        viewModel.getLoadStateObservable().observe(this, getPageLoadStateObserver(adapter))

        // Set up refresh/reload
        viewModel.getRefreshStateObservable().observe(this, Observer { pageLoadState: PageLoadState ->
            val swipe = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
            swipe.isRefreshing = pageLoadState == PageLoadState.LOADING
        })
        val swipe = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        swipe.setOnRefreshListener { viewModel.refresh() }

        // Set up search box
        searchText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updatedContainerBlobsFromInput()
                true
            } else {
                false
            }
        }

        searchText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updatedContainerBlobsFromInput()
                true
            } else {
                false
            }
        }

        val containerName = intent.getStringExtra(Constants.CONTAINER_NAME_EXTRA)
        searchText.setText(containerName)
        // List items in container
        viewModel.list(containerName)
    }

    private fun updatedContainerBlobsFromInput() {
        val text = searchText.text.toString()
        if (!CoreUtil.isNullOrEmpty(text)) {
            if (viewModel.list(text)) {
                recyclerView.scrollToPosition(0)
                val adapter = recyclerView.adapter
                if (adapter is ContainerBlobsPagedListAdapter) {
                    adapter.submitList(null)
                }
            }
        }
    }

    private val activity: Activity
        get() = this

    // Use a custom factory for ContainerBlobsPaginationViewModel, since the default Factory based ViewModelProviders
    // does not allow passing parameters to the ContainerBlobsPaginationViewModel constructor.
    @Suppress("UNCHECKED_CAST")
    inner class ContainerBlobsViewModelFactory(private val storageBlobClient: StorageBlobClient?) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return this.storageBlobClient?.let { ContainerBlobsPaginationViewModel(it) } as T
        }

    }

    companion object {
        private val TAG = ListAndDownloadBlobsActivity::class.java.simpleName

        private fun getPagedListObserver(adapter: ContainerBlobsPagedListAdapter,
                                         recyclerView: RecyclerView?): Observer<PagedList<BlobItem>> {
            return Observer { blobItemsList: PagedList<BlobItem> ->
                adapter.submitList(blobItemsList, Runnable {
                    val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager?
                    val position = layoutManager!!.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        recyclerView.scrollToPosition(position)
                    }
                })
            }
        }

        private fun getPageLoadStateObserver(adapter: ContainerBlobsPagedListAdapter): Observer<PageLoadState> {
            return Observer<PageLoadState> { loadState: PageLoadState -> adapter.setPageLoadState(loadState) }
        }
    }
}
