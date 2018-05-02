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
import com.azure.data.model.Database;
import com.azure.mobile.azuredataandroidexample_java.Adapter.Callback;
import com.azure.mobile.azuredataandroidexample_java.Adapter.CardAdapter;
import com.azure.mobile.azuredataandroidexample_java.Adapter.DatabaseViewHolder;
import com.azure.mobile.azuredataandroidexample_java.Controllers.App;
import com.azure.mobile.azuredataandroidexample_java.R;

import static com.azure.data.util.FunctionalUtils.onCallback;

public class DatabaseActivity extends Activity {

    private static final String TAG = "DatabaseActivity";

    private CardAdapter<Database> _adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.databases_activity);

        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        _adapter = new CardAdapter<>(R.layout.database_view, new Callback<Object>() {
            @Override
            public Void call() {
                Database db = (Database) this._result;
                DatabaseViewHolder vHolder = (DatabaseViewHolder) this._viewHolder;

                vHolder.idTextView.setText(db.getId());
                vHolder.ridTextView.setText(db.getResourceId());
                vHolder.selfTextView.setText(db.getSelfLink());
                vHolder.eTagTextView.setText(db.getEtag());
                vHolder.collsTextView.setText(db.getCollectionsLink());
                vHolder.usersTextView.setText(db.getUsersLink());

                vHolder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getBaseContext(), CollectionsActivity.class);
                    intent.putExtra("db_id", db.getId());
                    startActivity(intent);
                });

                return null;
            }
        }, DatabaseViewHolder.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(_adapter);

        Button clearButton = findViewById(R.id.button_clear);
        Button fetchButton = findViewById(R.id.button_fetch);
        Button createButton = findViewById(R.id.button_create);

        clearButton.setOnClickListener(v -> _adapter.clear());

        createButton.setOnClickListener(v -> {

            View editTextView = getLayoutInflater().inflate(R.layout.edit_text, null);
            EditText editText = editTextView.findViewById(R.id.editText);
            TextView messageTextView = editTextView.findViewById(R.id.messageText);
            messageTextView.setText(R.string.database_dialogue);

            new AlertDialog.Builder(DatabaseActivity.this)
                    .setView(editTextView)
                    .setPositiveButton("Create", (dialog, whichButton) -> {
                        String databaseId = editText.getText().toString();
                        final ProgressDialog progressDialog = ProgressDialog.show(DatabaseActivity.this, "", "Creating. Please wait...", true);

                        AzureData.createDatabase(databaseId, onCallback(response -> {

                            Log.e(TAG, "Database create result: " + response.isSuccessful());

                            runOnUiThread(() -> {
                                dialog.cancel();
                                _adapter.addData(response.getResource());

                                progressDialog.cancel();
                            });
                        }));
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    }).show();
        });

        fetchButton.setOnClickListener(v -> {
            try
            {
                final ProgressDialog dialog = ProgressDialog.show(DatabaseActivity.this, "", "Loading. Please wait...", true);

                AzureData.getDatabases(onCallback(response -> {

                    Log.e(TAG, "Database list result: " + response.isSuccessful());

                    runOnUiThread(() -> {
                        _adapter.clear();

                        for (Database db: response.getResource().getItems()) {
                            _adapter.addData(db);
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

    @Override
    protected void onResume() {
        super.onResume();

        if (_adapter != null) {
            _adapter.clear();
        }

        App.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }
}