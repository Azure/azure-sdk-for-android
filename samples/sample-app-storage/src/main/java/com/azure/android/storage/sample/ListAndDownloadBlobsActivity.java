package com.azure.android.storage.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.azure.android.core.credential.TokenRequestObservable;
import com.azure.android.core.credential.TokenRequestObserver;
import com.azure.android.core.credential.TokenResponseCallback;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.core.util.paging.PageLoadState;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import javax.inject.Inject;

import static com.azure.android.core.util.CoreUtil.isNullOrEmpty;
import static com.azure.android.storage.sample.Constants.CONTAINER_NAME_EXTRA;

public class ListAndDownloadBlobsActivity extends AppCompatActivity {
    // Singleton StorageBlobClient that will be created by Dagger. The singleton object is shared across various
    // activities in the application.
    @Inject
    StorageBlobAsyncClient storageBlobAsyncClient;

    private static final String TAG = ListAndDownloadBlobsActivity.class.getSimpleName();
    private ContainerBlobsPaginationViewModel viewModel;

    private RecyclerView recyclerView;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_listblobs);

        // Request Dagger to get singleton StorageBlobClient and initialize this.storageBlobClient
        ((MainApplication) getApplication()).getAppComponent().inject(this);

        this.recyclerView = findViewById(R.id.blob_list);
        this.searchText = findViewById(R.id.input_container_name);

        this.viewModel = ViewModelProviders.of(this, new ContainerBlobsViewModelFactory(this.storageBlobAsyncClient))
            .get(ContainerBlobsPaginationViewModel.class);

        // Set up Login
        TokenRequestObservable tokenRequestObservable = this.viewModel.getTokenRequestObservable();
        LifecycleOwner lifecycleOwner = this;

        PublicClientApplication.createMultipleAccountPublicClientApplication(
            this.getApplicationContext(),
            R.raw.authorization_configuration,
            new PublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                @Override
                public void onCreated(IMultipleAccountPublicClientApplication application) {
                    tokenRequestObservable.observe(lifecycleOwner, new TokenRequestObserver() {
                        @Override
                        public void onTokenRequest(String[] scopes, TokenResponseCallback callback) {
                            MsalClient.signIn(application, getActivity(), scopes, callback);
                        }
                    });
                }

                @Override
                public void onError(MsalException exception) {
                    Log.e(TAG, "Exception found when trying to sign in.", exception);
                }
            });

        ContainerBlobsPaginationRepository repository = (ContainerBlobsPaginationRepository) this.viewModel.getRepository();
        StorageBlobAsyncClient storageBlobAsyncClient = repository.getStorageBlobClient();

        // Set up PagedList Adapter
        ContainerBlobsPagedListAdapter adapter =
            new ContainerBlobsPagedListAdapter(storageBlobAsyncClient, () -> this.viewModel.retry());
        this.recyclerView.setAdapter(adapter);
        this.viewModel.getPagedListObservable().observe(this, getPagedListObserver(adapter, this.recyclerView));
        this.viewModel.getLoadStateObservable().observe(this, getPageLoadStateObserver(adapter));

        // Set up refresh/reload
        this.viewModel.getRefreshStateObservable().observe(this, pageLoadState -> {
            SwipeRefreshLayout swipe = findViewById(R.id.swipe_refresh);
            swipe.setRefreshing(pageLoadState == PageLoadState.LOADING);
        });
        SwipeRefreshLayout swipe = findViewById(R.id.swipe_refresh);
        swipe.setOnRefreshListener(() -> this.viewModel.refresh());

        // Set up search box
        this.searchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updatedContainerBlobsFromInput();
                return true;
            } else {
                return false;
            }
        });

        this.searchText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updatedContainerBlobsFromInput();
                return true;
            } else {
                return false;
            }
        });

        String containerName = getIntent().getStringExtra(CONTAINER_NAME_EXTRA);

        this.searchText.setText(containerName);
        // List items in container
        this.viewModel.list(containerName);
    }

    private void updatedContainerBlobsFromInput() {
        String text = this.searchText.getText().toString();

        if (!isNullOrEmpty(text)) {
            if (this.viewModel.list(text)) {
                this.recyclerView.scrollToPosition(0);
                RecyclerView.Adapter adapter = this.recyclerView.getAdapter();

                if (adapter instanceof ContainerBlobsPagedListAdapter) {
                    ((ContainerBlobsPagedListAdapter) adapter).submitList(null);
                }
            }
        }
    }

    private Activity getActivity() {
        return this;
    }

    private static Observer<PagedList<BlobItem>> getPagedListObserver(ContainerBlobsPagedListAdapter adapter,
                                                                      RecyclerView recyclerView) {
        return blobItemsList -> adapter.submitList(blobItemsList, new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int position = layoutManager.findFirstCompletelyVisibleItemPosition();

                if (position != RecyclerView.NO_POSITION) {
                    recyclerView.scrollToPosition(position);
                }
            }
        });
    }

    private static Observer<PageLoadState> getPageLoadStateObserver(ContainerBlobsPagedListAdapter adapter) {
        return loadState -> adapter.setPageLoadState(loadState);
    }

    // Use a custom factory for ContainerBlobsPaginationViewModel, since the default Factory based ViewModelProviders
    // does not allow passing parameters to the ContainerBlobsPaginationViewModel constructor.
    public class ContainerBlobsViewModelFactory implements ViewModelProvider.Factory {
        private final StorageBlobAsyncClient storageBlobAsyncClient;

        public ContainerBlobsViewModelFactory(StorageBlobAsyncClient storageBlobAsyncClient) {
            this.storageBlobAsyncClient = storageBlobAsyncClient;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new ContainerBlobsPaginationViewModel(this.storageBlobAsyncClient);
        }
    }
}
