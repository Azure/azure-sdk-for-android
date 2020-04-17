package com.azure.android.storage.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.core.util.paging.PageLoadState;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservable;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObserver;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenResponseCallback;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import javax.inject.Inject;

public class ContainerBlobsActivity extends AppCompatActivity {
    // to hold Singleton StorageBlobClient that will be created by Dagger.
    // The singleton object is shared across various activities in the application.
    @Inject
    StorageBlobClient storageBlobClient;

    private final static String DEFAULT_CONTAINER_NAME = "{thecontainername}";
    private static final String TAG = ContainerBlobsActivity.class.getSimpleName();
    private ContainerBlobsPaginationViewModel viewModel;
    //
    private RecyclerView recyclerView;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_containerblobs);

        // -------
        // Request Dagger to get singleton StorageBlobClient and initialize this.storageBlobClient
        //
        ((MainApplication) getApplication()).getAppComponent().inject(this);
        // -------

        this.recyclerView = findViewById(R.id.list);
        this.searchText = findViewById(R.id.input);
        //
        this.viewModel = ViewModelProviders.of(this, new ContainerBlobsViewModelFactory(this.storageBlobClient))
                .get(ContainerBlobsPaginationViewModel.class);
        //
        // Set up Login
        //
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
        //
        // Set up PagedList Adapter
        // ---------------------
        ContainerBlobsPagedListAdapter adapter
                = new ContainerBlobsPagedListAdapter(() -> this.viewModel.retry());
        this.recyclerView.setAdapter(adapter);
        this.viewModel.getPagedListObservable().observe(this, getPagedListObserver(adapter, this.recyclerView));
        this.viewModel.getLoadStateObservable().observe(this, getPageLoadStateObserver(adapter));

        //
        // Set up refresh/reload
        // ---------------------
        this.viewModel.getRefreshStateObservable().observe(this, pageLoadState -> {
            SwipeRefreshLayout swipe = findViewById(R.id.swipe_refresh);
            swipe.setRefreshing(pageLoadState == PageLoadState.LOADING);
        });
        SwipeRefreshLayout swipe = findViewById(R.id.swipe_refresh);
        swipe.setOnRefreshListener(() -> viewModel.refresh());

        //
        //
        // Set up search box
        // -----------------
        this.searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    updatedContainerBlobsFromInput();
                    return true;
                } else {
                    return false;
                }
            }
        });

        this.searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    updatedContainerBlobsFromInput();
                    return true;
                } else {
                    return false;
                }
            }
        });

        //------------------
        //
        this.viewModel.list(DEFAULT_CONTAINER_NAME); // List items in container
    }

    private void updatedContainerBlobsFromInput() {
        String text = this.searchText.getText().toString();
        if (text != null && text != "") {
            if (this.viewModel.list(text)) {
                this.recyclerView.scrollToPosition(0);
                RecyclerView.Adapter adapter = this.recyclerView.getAdapter();
                if (adapter != null && adapter instanceof ContainerBlobsPagedListAdapter) {
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
        return new Observer<PagedList<BlobItem>>() {
            @Override
            public void onChanged(PagedList<BlobItem> blobItemsList) {
                adapter.submitList(blobItemsList, new Runnable() {
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
        };
    }

    private static Observer<PageLoadState> getPageLoadStateObserver(ContainerBlobsPagedListAdapter adapter) {
        return new Observer<PageLoadState>() {
            @Override
            public void onChanged(PageLoadState loadState) {
                adapter.setPageLoadState(loadState);
            }
        };
    }

    // Use a custom factory for ContainerBlobsPaginationViewModel, since the default Factory based
    // ViewModelProviders does not allow passing parameters to ContainerBlobsPaginationViewModel ctr.
    public class ContainerBlobsViewModelFactory implements ViewModelProvider.Factory {
        private final StorageBlobClient storageBlobClient;

        public ContainerBlobsViewModelFactory(StorageBlobClient storageBlobClient) {
            this.storageBlobClient = storageBlobClient;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new ContainerBlobsPaginationViewModel(this.storageBlobClient);
        }
    }
}
