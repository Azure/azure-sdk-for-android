package com.azure.data.azurecognitivespeech;

import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.NoMatchDetails;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SpeechClient {
    private String region, key;

    public SpeechClient(String serviceEndpoint, String key) {
        this.region = getServiceRegion(serviceEndpoint);
        this.key = key;
    }

    public String recognizeSpeech() throws ExecutionException, InterruptedException {
        String resultString;
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(key, region);

        try (SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig)) {
            Future<SpeechRecognitionResult> future = speechRecognizer.recognizeOnceAsync();
            assert (future != null);

            // Note: this will block the UI thread, so eventually, you want to register for the event (see full samples)
            SpeechRecognitionResult result = future.get();
            assert (result != null);

            if (result.getReason() == ResultReason.RecognizedSpeech)
                resultString = result.getText();
            else {
                String errorDetails = "No details";

                if (result.getReason() == ResultReason.Canceled)
                    errorDetails = CancellationDetails.fromResult(result).getReason().toString();
                else if (result.getReason() == ResultReason.NoMatch)
                    errorDetails = NoMatchDetails.fromResult(result).getReason().toString();

                resultString = "Error recognizing" + System.lineSeparator() +
                    "Reason: " + result.getReason() + System.lineSeparator() +
                    "Details: " + errorDetails;
            }
        }

        return resultString;
    }

    private String getServiceRegion(String serviceEndpoint) {
        int regionStart = serviceEndpoint.lastIndexOf("://") + 3;
        int regionEnd = serviceEndpoint.indexOf('.');

        return serviceEndpoint.substring(regionStart, regionEnd);
    }
}
