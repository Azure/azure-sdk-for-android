package com.azure.data.azurecognitivecomputervision;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.core.http.rest.Response;
import com.azure.data.azurecognitivecomputervision.models.Line;
import com.azure.data.azurecognitivecomputervision.models.RecognizeTextHeadersResponse;
import com.azure.data.azurecognitivecomputervision.models.TextOperationResult;
import com.azure.data.azurecognitivecomputervision.models.TextOperationStatusCodes;
import com.azure.data.azurecognitivecomputervision.models.TextRecognitionResult;
import com.azure.data.azurecognitivecomputervision.models.Word;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ComputerVisionClientTest {
    private static String AZ_COGNITIVE_SETTINGS_FILE = "azCognitiveSettings.txt";

    @Test
    public void canRecognizeText() throws IOException {
        InputStream imageStream = this.getClass().getClassLoader().getResourceAsStream("ms.jpg");
        ComputerVisionClient client = createConfigClient();
        RecognizeTextHeadersResponse recognizeTextHeadersResponse = client.recognizeTextWithResponse(toByteArray(imageStream));
        String pollingUrl = recognizeTextHeadersResponse.deserializedHeaders().operationLocation();
        if (pollingUrl != null) {
            int i = pollingUrl.lastIndexOf("/");
            if (i != -1) {
                String operationId = pollingUrl.substring(i + 1);
                Response<TextOperationResult> finalResponse = Observable.defer(() -> Observable.just(client.getTextOperationResultWithResponse(operationId)))
                    .repeatWhen(o -> o.delay(2, TimeUnit.SECONDS))
                    .takeUntil(r -> r.value().status() == TextOperationStatusCodes.SUCCEEDED
                        || r.value().status() == TextOperationStatusCodes.FAILED)
                    .filter(r -> r.value().status() == TextOperationStatusCodes.SUCCEEDED
                        || r.value().status() == TextOperationStatusCodes.FAILED)
                    .blockingFirst();

                if (finalResponse.value().status() == TextOperationStatusCodes.SUCCEEDED) {
                    TextRecognitionResult recognitionResult = finalResponse.value().recognitionResult();
                    assertNotNull(recognitionResult);
                    List<Line> lines = recognitionResult.lines();
                    assertNotNull(lines);
                    assertFalse(lines.isEmpty());
                    Line line = lines.get(0);
                    List<Word> words = line.words();
                    assertNotNull(words);
                    assertFalse(words.isEmpty());
                    Word word = words.get(0);
                    Assert.assertTrue("microsoft".equalsIgnoreCase(word.text()));
                } else {
                    assertTrue("Polling failed", false);
                }
            } else {
                assertTrue("Missing operation-id in operation-location response header", false);
            }
        } else {
            assertTrue("Missing operation-location response header that is needed for polling", false);
        }
    }

    private ComputerVisionClient createConfigClient() throws MalformedURLException {
        String[] settings = azCognitiveSettings();
        String serviceEndpoint = settings[0];
        String key = settings[1];

        return new ComputerVisionClient(serviceEndpoint, key);
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    private String[] azCognitiveSettings() {
        try {
            List<String> lines = readLinesFromResourceTxtFile(AZ_COGNITIVE_SETTINGS_FILE);
            if (lines.size() != 2) {
                throw new RuntimeException(AZ_COGNITIVE_SETTINGS_FILE + " should contain two entries - endpoint and key");
            }
            return new String [] {lines.get(0), lines.get(1)};
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private List<String> readLinesFromResourceTxtFile(String fileName) throws IOException {
        InputStream connectionStringUrl = this.getClass().getClassLoader().getResourceAsStream(fileName);
        if (connectionStringUrl == null) {
            throw  new IllegalArgumentException(fileName + " not found in the com.resources dir.");
        }
        List<String> lines = new ArrayList<String>();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(connectionStringUrl));
            for (String line; (line = r.readLine()) != null; ) {
                lines.add(line);
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return lines;
    }
}
