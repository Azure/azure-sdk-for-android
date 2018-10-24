package com.azure.mobile.azuredataandroidexample_java.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.azure.data.AzureData;
import com.azure.data.model.DocumentCollection;
import com.azure.mobile.azuredataandroidexample_java.Adapter.Callback;
import com.azure.mobile.azuredataandroidexample_java.Adapter.CardAdapter;
import com.azure.mobile.azuredataandroidexample_java.Adapter.DocumentCollectionViewHolder;
import com.azure.mobile.azuredataandroidexample_java.Controllers.App;
import com.azure.mobile.azuredataandroidexample_java.R;

import static com.azure.data.util.FunctionalUtils.onCallback;

public class CollectionsActivity extends Activity {

    private static final String TAG = "CollectionsActivity";

    private CardAdapter<DocumentCollection> _adapter;

    private String _databaseId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collections_activity);

        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        _adapter = new CardAdapter<>(R.layout.collection_view, new Callback<Object>() {
            @Override
            public Void call() {
                DocumentCollection coll = (DocumentCollection)this._result;
                DocumentCollectionViewHolder vHolder = (DocumentCollectionViewHolder)this._viewHolder;

                vHolder.idTextView.setText(coll.getId());
                vHolder.ridTextView.setText(coll.getResourceId());
                vHolder.selfTextView.setText(coll.getSelfLink());
                vHolder.eTagTextView.setText(coll.getEtag());

                vHolder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getBaseContext(), DocumentsActivity.class);
                    intent.putExtra("db_id", _databaseId);
                    intent.putExtra("coll_id", coll.getId());
                    startActivity(intent);
                });

                return null;
            }
        }, DocumentCollectionViewHolder.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(_adapter);

        TextView databaseIdTextView = findViewById(R.id.databaseId);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _databaseId = extras.getString("db_id");
            databaseIdTextView.setText(_databaseId);
        }

        Button clearButton = findViewById(R.id.button_clear);
        Button fetchButton = findViewById(R.id.button_fetch);
        Button deleteButton = findViewById(R.id.button_delete);
        Button createButton = findViewById(R.id.button_create);

        clearButton.setOnClickListener(v -> _adapter.clear());

        deleteButton.setOnClickListener(v -> {
            final ProgressDialog dialog = ProgressDialog.show(CollectionsActivity.this, "", "Deleting. Please wait...", true);

            AzureData.deleteDatabase(_databaseId, onCallback(response -> {

                Log.e(TAG, "Document delete result: " + response.isSuccessful());

                runOnUiThread(() -> {
                    _adapter.clear();
                    dialog.cancel();
                    // revert back to database page
                    finish();
                });
            }));
        });

        fetchButton.setOnClickListener(v -> {
            try
            {
                final ProgressDialog dialog = ProgressDialog.show(CollectionsActivity.this, "", "Loading. Please wait...", true);

                AzureData.getCollections(_databaseId, null, onCallback(response -> {

                    Log.e(TAG, "Collection list result: " + response.isSuccessful());

                    runOnUiThread(() -> {
                        // clear current list
                        _adapter.clear();

                        for (DocumentCollection collection: response.getResource().getItems()) {

                            _adapter.addData(collection);
                        }

                        dialog.cancel();
                    });
                }));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        createButton.setOnClickListener(v -> {

            View editTextView = getLayoutInflater().inflate(R.layout.edit_text, null);
            EditText editText = editTextView.findViewById(R.id.editText);
            TextView messageTextView = editTextView.findViewById(R.id.messageText);
            messageTextView.setText(R.string.document_collection_dialogue);

            new AlertDialog.Builder(CollectionsActivity.this)
                    .setView(editTextView)
                    .setPositiveButton("Create", (dialog, whichButton) -> {
                        String collectionId = editText.getText().toString();
                        final ProgressDialog progressDialog = ProgressDialog.show(CollectionsActivity.this, "", "Creating. Please wait...", true);

                        AzureData.createCollection(collectionId, _databaseId, onCallback(response -> {

                            Log.e(TAG, "Collection create result: " + response.isSuccessful());

                            runOnUiThread(() -> {
                                _adapter.addData(response.getResource());
                                dialog.cancel();
                                progressDialog.cancel();
                            });
                        }));
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    }).show();
        });
    }
}