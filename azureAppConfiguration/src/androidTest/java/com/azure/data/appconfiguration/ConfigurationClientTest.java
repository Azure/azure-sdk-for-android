package com.azure.data.appconfiguration;

import androidx.test.runner.AndroidJUnit4;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ConfigurationClientTest {
    private static String AZCONFIG_SETTINGS_FILE = "azConfigSettings.txt";
    //
    @Test
    public void getSetting() throws Exception {
        //
        String[] settings = azConfigSettings();
        String connectionString = settings[0];
        String serviceEndpoint = settings[1];
        //
        ConfigurationClient client = new ConfigurationClient(new URL(serviceEndpoint), connectionString);
        ConfigurationSetting setting = new ConfigurationSetting();
        setting.key("hello");
        ConfigurationSetting result = client.getSetting(setting);
        assertNotNull(result);
//        assertEquals(200, response.code());
//        assertNotNull(response.body());
        assertEquals("hello", result.key());
        assertEquals("world", result.value());
    }

    private String[] azConfigSettings() {
        try {
            List<String> lines = readLinesFromResourceTxtFile(AZCONFIG_SETTINGS_FILE);
            if (lines.size() != 2) {
                throw new RuntimeException(AZCONFIG_SETTINGS_FILE + " should contain two entries - connection string and endpoint");
            }
            return new String [] {lines.get(0), lines.get(1)};
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private List<String> readLinesFromResourceTxtFile(String fileName) throws IOException {
        InputStream connectionStringUrl = this.getClass().getClassLoader().getResourceAsStream(fileName);
        if (connectionStringUrl == null) {
            throw  new IllegalArgumentException(fileName + " not found in the resources dir.");
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
