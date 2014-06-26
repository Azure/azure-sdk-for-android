/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.management.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import com.microsoft.azure.Configuration;
import com.microsoft.azure.management.configuration.ManagementConfiguration;
import com.microsoft.azure.management.configuration.PublishSettingsLoader;

/**
 * The certificate used in the test publishsettings file is a dummy one
 * generated for this unit test. Here's how to create one (press
 * <code>ENTER</code> at any prompt):
 * 
 * <p>
 * <code>
 *    openssl req -x509 -nodes -days 365 -newkey rsa:1024 -keyout mycert.pem -out mycert.pem<br/>
 *    openssl pkcs12 -export -out mycert.pfx -in mycert.pem -name "My Certificate"<br/>
 *    base64 mycert.pfx > mycert.b64<br/>
 *  </code>
 * </p>
 * 
 * Remove all line-breaks in mycert.b64 and copy & paste into the
 * ManagementCertificate attribute.
 */
public class PublishSettingsLoaderTest extends TestCase {
    public void testShouldCreateManagementConfigurationFromVersion1()
            throws Exception {
        // Arrange
    	InputStream input = getClass().getResourceAsStream("/Assets/v1.publishsettings");
    	BufferedReader r = new BufferedReader(new InputStreamReader(input));
    	StringBuilder total = new StringBuilder();
    	String line;
    	while ((line = r.readLine()) != null) {
    	    total.append(line);
    	}
    	
        String file = total.toString();
        String expectedKeyStoreLocation = System.getProperty("user.home") + File.separator
                + ".azure" + File.separator + "1234.out";

        // Act
        Configuration config = PublishSettingsLoader
                .createManagementConfiguration(file, "1234");

        // Assert
        assertEquals("subscriptionId", "1234",
                config.getProperty(ManagementConfiguration.SUBSCRIPTION_ID));
        assertEquals("keyStoreLocation", expectedKeyStoreLocation,
                config.getProperty(ManagementConfiguration.KEYSTORE_PATH));
        assertEquals("keyStorePassword", "",
                config.getProperty(ManagementConfiguration.KEYSTORE_PASSWORD));
    }

    public void testShouldCreateManagementConfigurationFromVersion2()
            throws Exception {
        // Arrange
        String file = getClass().getResource("v2.publishsettings").getFile();
        String expectedKeyStoreLocation = System.getProperty("user.home") + File.separator
                + ".azure" + File.separator + "2345.out";

        // Act
        Configuration config = PublishSettingsLoader
                .createManagementConfiguration(file, "2345");

        // Assert
        assertEquals("subscriptionId", "2345",
                config.getProperty(ManagementConfiguration.SUBSCRIPTION_ID));
        assertEquals("keyStoreLocation", expectedKeyStoreLocation,
                config.getProperty(ManagementConfiguration.KEYSTORE_PATH));
        assertEquals("keyStorePassword", "",
                config.getProperty(ManagementConfiguration.KEYSTORE_PASSWORD));
    }

    public void shouldFailOnNonExistingFile() throws Exception {
        try {
            // Act
            PublishSettingsLoader.createManagementConfiguration("nonexisting",
                    "1234");
            fail("should have thrown");
        } catch (IOException e) {
            // success
        }
    }

    public void shouldFailOnFaultyFile() throws Exception {
        try {
            // Arrange
            String file = getClass().getResource("faulty.publishsettings")
                    .getFile();
    
            // Act
            PublishSettingsLoader.createManagementConfiguration(file, "1234");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

}
