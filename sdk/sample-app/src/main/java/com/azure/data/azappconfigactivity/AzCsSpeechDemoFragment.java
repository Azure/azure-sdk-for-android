package com.azure.data.azappconfigactivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.azure.data.azurecognitivespeech.SpeechClient;

import java.util.concurrent.ExecutionException;

public class AzCsSpeechDemoFragment extends Fragment implements View.OnClickListener {
    private TextView speechTextView;
    private SpeechClient speechClient;

    public static Fragment newInstance() {
        return new AzCsSpeechDemoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.az_cs_speech_demo_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        this.speechTextView = rootView.findViewById(R.id.recognizeSpeechTextView);

        Button recordAudioButton = rootView.findViewById(R.id.recordAudioButton);
        recordAudioButton.setOnClickListener(this);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String serviceEndpoint = preference.getString("az_cs_endpoint", "<unset>");
        String key = preference.getString("az_cs_key", "<unset>");

        if (serviceEndpoint.isEmpty() || serviceEndpoint.equals("<unset>") || key.isEmpty() || key.equals("<unset>"))
            this.speechTextView.setText(R.string.endpoint_info_not_set);
        else {
            this.speechClient = new SpeechClient(serviceEndpoint, key);
        }
    }

    @Override
    public void onClick(View buttonView) {
        if (speechClient != null) {
            try {
                String result = this.speechClient.recognizeSpeech();

                this.speechTextView.setText(result);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("AzSDKDemo - Speech", "Unexpected error: " + e.getMessage());

                this.speechTextView.setText(R.string.unexpected_error);
            }
        }
    }
}
