package com.azure.data.azappconfigactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.data.azurecognitivecomputervision.ComputerVisionClient;
import com.azure.data.azurecognitivecomputervision.models.ComputerVisionErrorException;
import com.azure.data.azurecognitivecomputervision.models.Line;
import com.azure.data.azurecognitivecomputervision.models.RecognizeTextHeadersResponse;
import com.azure.data.azurecognitivecomputervision.models.TextOperationResult;
import com.azure.data.azurecognitivecomputervision.models.TextOperationStatusCodes;
import com.azure.data.azurecognitivecomputervision.models.Word;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class AzCsComputerVisionDemoFragment extends Fragment implements View.OnClickListener {
    private final CompositeDisposable disposables = new CompositeDisposable();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // FILE_PROVIDER_AUTHORITY must be value of 'android:authorities' attribute of 'provider'
    // node in AndroidManifest.xml.
    private static final String FILE_PROVIDER_AUTHORITY = "com.azure.data.azappconfigactivity.fileprovider";
    private ImageView capturedImgView;
    private Uri capturedImageUri;
    private ProgressBar recognizeProgressBar;
    private TextView recognizeProgressBarMessage;
    private TextView recognizeResponseTxt;

    public static AzCsComputerVisionDemoFragment newInstance() {
        return new AzCsComputerVisionDemoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.az_cs_computer_vision_demo_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        this.capturedImgView = rootView.findViewById(R.id.capturedImageView);
        this.recognizeProgressBar = rootView.findViewById(R.id.recognizeProgressBar);
        this.recognizeProgressBarMessage = rootView.findViewById(R.id.recognizeProgressBarMessage);
        this.recognizeResponseTxt = rootView.findViewById(R.id.recognizeResponseText);
        //
        Button takePicBtn = rootView.findViewById(R.id.takePictureButton);
        takePicBtn.setOnClickListener(this);
        //
        Button recognizeTxtInPicBtn = rootView.findViewById(R.id.recognizeTextInPictureButton);
        recognizeTxtInPicBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View buttonView) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String serviceEndpoint = preference.getString("az_cs_endpoint", "<unset>");
        String key = preference.getString("az_cs_key", "<unset>");
        //
        if (serviceEndpoint.isEmpty() || serviceEndpoint.equals("<unset>") || key.isEmpty() || key.equals("<unset>")) {
            TextView responseTextView = buttonView.getRootView().findViewById(R.id.recognizeResponseText);
            responseTextView.setText(R.string.endpoint_info_not_set);
        } else {
            ComputerVisionClient client;
            try {
                client = new ComputerVisionClient(serviceEndpoint, key);
            } catch (MalformedURLException mue) {
                throw new RuntimeException(mue);
            }
            switch (buttonView.getId()) {
                case R.id.takePictureButton:
                    onCaptureClick();
                    break;
                case R.id.recognizeTextInPictureButton:
                    onAnalyzeClick(client);
                    break;
            }
        }
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
        progressBarMsg(null);
        //
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ioe) {
                Log.e("AzSDKDemo - CompVision", "Unexpected error: " + ioe.getMessage());
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

    private void onAnalyzeClick(ComputerVisionClient client) {
        progressBarMsg("Processing captured image...");
        Bitmap bitmap = ((BitmapDrawable) capturedImgView.getDrawable()).getBitmap();
        //
        DisposableSingleObserver<Response<TextOperationResult>> disposable = bitmapToByteArray(bitmap)
            .flatMap((Function<byte[], Single<String>>) image -> recognizeTextInImage(client, image))
            .flatMap((Function<String, Single<Response<TextOperationResult>>>) operationId -> pollRecognizeTextOperationUntilDone(client, operationId))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<Response<TextOperationResult>>() {
                @Override
                public void onSuccess(Response<TextOperationResult> result) {
                    progressBarMsg(null);
                    if (result.statusCode() == 200) {
                        recognizeResponseTxt.setText(formatRecognizeResult(result.value()));
                    } else {
                        recognizeResponseTxt.setText(String.format("statusCode:%d", result.statusCode()));
                    }
                }

                @Override
                public void onError(Throwable e) {
                    progressBarMsg(null);
                    if (e instanceof ComputerVisionErrorException) {
                        ComputerVisionErrorException visionException = (ComputerVisionErrorException) e;
                        recognizeResponseTxt.setText(String.format("Operation failed: { code: %d error:%s }", visionException.value().code(), visionException.value().message()));
                    } else if (e instanceof HttpResponseException) {
                        HttpResponseException responseException = (HttpResponseException) e;
                        recognizeResponseTxt.setText(String.format("Operation failed: { code: %d error:%s }", responseException.response().statusCode(), responseException.value()));
                    } else {
                        recognizeResponseTxt.setText(String.format("Operation failed: { error:%s }", e.getMessage()));
                    }
                }
            });
        disposables.add(disposable);
    }


    private Single<byte[]> bitmapToByteArray(Bitmap bitmap) {
        return Single.fromCallable(() -> {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        });
    }

    private Single<String> recognizeTextInImage(ComputerVisionClient client, byte[] image) {
        return Observable.defer((Callable<ObservableSource<RecognizeTextHeadersResponse>>) () -> Observable.just(client.recognizeTextWithResponse(image)))
            .map(recognizeTextHeadersResponse -> {
                String pollingUrl = recognizeTextHeadersResponse.deserializedHeaders().operationLocation();
                if (pollingUrl != null) {
                    int i = pollingUrl.lastIndexOf("/");
                    if (i != -1) {
                        return pollingUrl.substring(i + 1);
                    }
                }
                throw new RuntimeException("operation-Location header is absent or invalid");
            })
            .firstElement()
            .toSingle();
    }

    private Single<Response<TextOperationResult>> pollRecognizeTextOperationUntilDone(ComputerVisionClient client, String operationId) {
        return Observable.defer(() -> Observable.just(client.getTextOperationResultWithResponse(operationId)))
            .repeatWhen(o -> o.delay(2, TimeUnit.SECONDS))
            .takeUntil(r -> r.value().status() == TextOperationStatusCodes.SUCCEEDED || r.value().status() == TextOperationStatusCodes.FAILED)
            .filter(r -> r.value().status() == TextOperationStatusCodes.SUCCEEDED || r.value().status() == TextOperationStatusCodes.FAILED)
            .firstElement()
            .toSingle();
    }

    private void progressBarMsg(String message) {
        if (message == null) {
            recognizeProgressBar.setVisibility(View.INVISIBLE);
            recognizeProgressBarMessage.setText(null);
        } else {
            recognizeProgressBar.setVisibility(View.VISIBLE);
            recognizeProgressBarMessage.setText(message);
        }
    }

    private String formatRecognizeResult(TextOperationResult result) {
        if (result == null || result.recognitionResult() == null) {
            return "no text recognized";
        }
        List<Line> lines = result.recognitionResult().lines();
        if (lines == null || lines.size() == 0) {
            return "no text recognized";
        }
        StringBuilder builder = new StringBuilder();
        for (Line line : lines) {
            List<Word> words = line.words();
            if (words == null || words.size() == 0) {
                continue;
            }
            for (Word word : line.words()) {
                builder.append(word.text());
                builder.append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
