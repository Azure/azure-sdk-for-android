package com.azure.data.appconfiguration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ConfigurationClientTest {
    private static String AZCONFIG_SETTINGS_FILE = "azConfigSettings.txt";
    //
    @Test
    public void setGetSetting() throws Exception {
        String SettingName = UUID.randomUUID().toString();
        String settingValue = "world1";
        //
        ConfigurationClient client = createConfigClient();
        // -- Add setting
        ConfigurationSetting result = client.addSetting(SettingName, settingValue);
        assertNotNull(result);
        assertEquals(SettingName, result.key());
        assertEquals(settingValue, result.value());
        // == Get setting
        ConfigurationSetting setting = new ConfigurationSetting();
        setting.key(SettingName);
        result = client.getSetting(setting);
        assertNotNull(result);
        assertEquals(SettingName, result.key());
        assertEquals(settingValue, result.value());
    }

    @Test
    public void getNotFoundSetting() throws Exception {
        String SettingName = UUID.randomUUID().toString();
        //
        ConfigurationClient client = createConfigClient();
        // == Get setting
        ConfigurationSetting setting = new ConfigurationSetting();
        setting.key(SettingName);
        boolean thrownException = false;
        try {
            ConfigurationSetting result = client.getSetting(setting);
        } catch (ResourceNotFoundException rnfe) {
            thrownException = true;
        }
        assertTrue("Expected ResourceNotFoundException is not thrown", thrownException);
    }


    private ConfigurationClient createConfigClient() throws MalformedURLException {
        String[] settings = azConfigSettings();
        String connectionString = settings[0];
        String serviceEndpoint = settings[1];
        //
        return new ConfigurationClient(new URL(serviceEndpoint), connectionString);
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
