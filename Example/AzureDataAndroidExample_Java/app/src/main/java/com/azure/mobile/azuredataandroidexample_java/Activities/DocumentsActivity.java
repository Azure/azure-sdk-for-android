package com.azure.mobile.azuredataandroidexample_java.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.azure.data.AzureData;
import com.azure.data.model.Document;
import com.azure.mobile.azuredataandroidexample_java.Adapter.Callback;
import com.azure.mobile.azuredataandroidexample_java.Adapter.CardAdapter;
import com.azure.mobile.azuredataandroidexample_java.Adapter.DocumentViewHolder;
import com.azure.mobile.azuredataandroidexample_java.R;
import com.azure.mobile.azuredataandroidexample_java.model.MyDocument;

import static com.azure.data.util.FunctionalUtils.onCallback;

public class DocumentsActivity extends Activity {

    private static final String TAG = "CollectionsActivity";

    private CardAdapter<Document> _adapter;

    private String _databaseId;

    private String _collectionId;

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.documents_activity);

        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        _adapter = new CardAdapter<>(R.layout.document_view, new Callback<Object>() {
            @Override
            public Void call() {
                Document coll = (Document)this._result;
                DocumentViewHolder vHolder = (DocumentViewHolder)this._viewHolder;

                vHolder.idTextView.setText(coll.getId());
                vHolder.ridTextView.setText(coll.getResourceId());
                vHolder.selfTextView.setText(coll.getSelfLink());
                vHolder.eTagTextView.setText(coll.getEtag());

                vHolder.itemView.setOnClickListener(v -> {
                });

                return null;
            }
        }, DocumentViewHolder.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(_adapter);

        TextView collectionIdTextView = findViewById(R.id.collectionId);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _databaseId = extras.getString("db_id");
            _collectionId = extras.getString("coll_id");

            collectionIdTextView.setText(_collectionId);
        }

        Button clearButton = findViewById(R.id.button_clear);
        Button fetchButton = findViewById(R.id.button_fetch);
        Button deleteButton = findViewById(R.id.button_delete);

        clearButton.setOnClickListener(v -> _adapter.clear());

        deleteButton.setOnClickListener(v -> {
            final ProgressDialog dialog = ProgressDialog.show(DocumentsActivity.this, "", "Deleting. Please wait...", true);

            AzureData.deleteCollection(_collectionId, _databaseId, onCallback(response -> {

                Log.e(TAG, "Collection delete result: " + response.isSuccessful());

                runOnUiThread(() -> {
                    _adapter.clear();
                    dialog.cancel();
                    finish();
                });
            }));
        });

        fetchButton.setOnClickListener(v -> {
            try
            {
                final ProgressDialog dialog = ProgressDialog.show(DocumentsActivity.this, "", "Loading. Please wait...", true);

                AzureData.getDocuments(_collectionId, _databaseId, MyDocument.class, 100, onCallback(response -> {

                    Log.e(TAG, "Document list result: " + response.isSuccessful());

                    runOnUiThread(() -> {
                        // clear current list
                        _adapter.clear();

                        for (Document doc: response.getResource().getItems()) {

                            _adapter.addData(doc);
                        }

                        dialog.cancel();
                    });
                }));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}