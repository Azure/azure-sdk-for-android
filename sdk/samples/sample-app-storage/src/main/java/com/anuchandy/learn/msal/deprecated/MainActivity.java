//package com.anuchandy.learn.msal.deprecated;
//
//import android.os.Bundle;
//
//import com.anuchandy.learn.msal.R;
//import com.azure.android.core.http.interceptors.AddDateInterceptor;
//import com.azure.android.core.http.rest.RestCallBack;
//import com.azure.android.storage.blob.StorageClient;
//import com.azure.android.storage.blob.models.BlobItem;
//import com.jakewharton.threetenabp.AndroidThreeTen;
//import com.microsoft.identity.client.PublicClientApplication;
//
//import androidx.appcompat.app.AppCompatActivity;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Button;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import okhttp3.Interceptor;
//
//public class MainActivity extends AppCompatActivity {
//
//    final static  List<String> SCOPES = Arrays.asList("https://anustorageandroid.blob.core.windows.net/.default");
//    final static String STORAGE_URL = "https://anustorageandroid.blob.core.windows.net/";
//    private static final String TAG = MainActivity.class.getSimpleName();
//    Button callStorageButton;
//    private StorageClient storageClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        AndroidThreeTen.init(this);
//        setContentView(R.layout.activity_main);
//
//        callStorageButton = findViewById(R.id.callStorage);
//        callStorageButton.setOnClickListener(v -> onCallStorageClicked());
//        //
//        MsalMobileTokenCredentials credentials = new MsalMobileTokenCredentials(this,
//                new PublicClientApplication(this.getApplicationContext(), R.raw.auth_config));
//        List<Interceptor> interceptors = new ArrayList<>();
//        interceptors.add(new BearerTokenAuthenticationInterceptor(credentials, SCOPES));
//        interceptors.add(new AddDateInterceptor());
//        this.storageClient = new StorageClient(STORAGE_URL, interceptors);
//    }
//
//    private void onCallStorageClicked() {
//        callStorageAPI();
//    }
//
//    private void callStorageAPI() {
//        final String containerName = "firstcontainer";
//        this.storageClient.getBlobsInPage(null, containerName, null, new RestCallBack<List<BlobItem>>() {
//            @Override
//            public void onResponse(List<BlobItem> blobs) {
//                for (BlobItem blob : blobs) {
//                    Log.d(TAG, blob.getName() + ":" + blob.getProperties().getLastModified());
//                }
//            }
//            @Override
//            public void onFailure(Throwable t) {
//                Log.e(TAG, t.getMessage());
//            }
//        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//}
