// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.util;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the configuration API.
 */
public class ConfigurationTests {
    private static final String MY_CONFIGURATION = "my-configuration";
    private static final String EXPECTED_VALUE = "expected-value";
    private static final String DEFAULT_VALUE = "default-value";
    private Configuration configuration;

    @Before
    public void setUp() {
        configuration = new Configuration();
    }

    /**
     * Verifies that a parameter is able to be retrieved.
     */
    @Test
    public void configurationFound() {
        configuration.put(MY_CONFIGURATION, EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        assertNull(configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        configuration.put(MY_CONFIGURATION, EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION, DEFAULT_VALUE));
    }

    /**
     * Verifies that when a configuration value isn't found the default will be returned.
     */
    @Test
    public void fallbackToDefaultConfiguration() {
        assertEquals(DEFAULT_VALUE, configuration.get(MY_CONFIGURATION, DEFAULT_VALUE));
    }

    /**
     * Verifies that a found configuration value is able to be mapped.
     */
    @Test
    public void foundConfigurationIsConverted() {
        configuration.put(MY_CONFIGURATION, EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE.toUpperCase(), configuration.get(MY_CONFIGURATION, String::toUpperCase));
    }

    /**
     * Verifies that when a configuration isn't found the converter returns null.
     */
    @Test
    public void notFoundConfigurationIsConvertedToNull() {
        assertNull(configuration.get(MY_CONFIGURATION, String::toUpperCase));
    }

    /**
     * Verifies that a configuration is loaded from a .properties file.
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    public void foundConfigurationFromPropertiesFile() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testConfig.properties");
        configuration = new Configuration(inputStream);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    @Test
    public void cloneConfiguration() {
        configuration.put("key1", "value1")
            .put("key2", "value2");

        Configuration configurationClone = configuration.clone();

        // Verify that the clone has the expected values.
        assertEquals(configuration.get("key1"), configurationClone.get("key1"));
        assertEquals(configuration.get("key2"), configurationClone.get("key2"));

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("key2");
        assertTrue(configuration.contains("key2"));
    }

    @Test
    public void loadValueTwice() {
        configuration.put("key1", "value1");

        String value1 = configuration.get("key1");
        String value2 = configuration.get("key1");

        assertEquals(value1, value2);
    }
}
