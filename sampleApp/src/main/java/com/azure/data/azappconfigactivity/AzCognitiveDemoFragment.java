package com.azure.data.azappconfigactivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AzCognitiveDemoFragment extends Fragment implements View.OnClickListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // FILE_PROVIDER_AUTHORITY must be value of 'android:authorities' attribute of 'provider'
    // node in AndroidManifest.xml.
    private static final String FILE_PROVIDER_AUTHORITY = "com.azure.data.azappconfigactivity.fileprovider";
    private ImageView capturedImgView;
    private Uri capturedImageUri;

    public static AzCognitiveDemoFragment newInstance() {
        return new AzCognitiveDemoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.az_cognitive_demo_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        Button setBtn = rootView.findViewById(R.id.takePicBtn);
        setBtn.setOnClickListener(this);
        capturedImgView = rootView.findViewById(R.id.imageview);
    }

    @Override
    public void onClick(View view) {
        onCaptureClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                capturedImgView.setImageURI(capturedImageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onCaptureClick() {
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                capturedImageUri = FileProvider.getUriForFile(this.getActivity().getBaseContext(),
                    FILE_PROVIDER_AUTHORITY,
                    photoFile);
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                startActivityForResult(captureImageIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);
        return image;
    }
}
