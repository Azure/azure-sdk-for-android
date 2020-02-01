package com.azure.android.storage.sample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class MainActivity extends AppCompatActivity {
    private Button listBlobsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);
        this.listBlobsButton = findViewById(R.id.listBlobs);
        this.listBlobsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContainerBlobsActivity.class);
            startActivity(intent);
        });
    }
}
