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
package com.microsoft.azure.management.storage;

import java.net.URI;
import java.util.Random;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import com.microsoft.azure.core.utils.KeyStoreType;
import com.microsoft.azure.management.ManagementClient;
import com.microsoft.azure.management.ManagementService;
import com.microsoft.azure.management.configuration.ManagementConfiguration;
import com.microsoft.azure.Configuration;
import com.microsoft.azure.management.models.LocationAvailableServiceNames;
import com.microsoft.azure.management.models.LocationsListResponse;


public abstract class StorageManagementIntegrationTestBase extends TestCase {

    protected static String testStorageAccountPrefix = "aztst";
    protected static String storageLocation = null;

    protected static StorageManagementClient storageManagementClient;
    protected static ManagementClient managementClient;

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        storageManagementClient = StorageManagementService.create(config);
    }

    protected static void createManagementClient() throws Exception {
        Configuration config = createConfiguration();
        managementClient = ManagementService.create(config);
    }

    protected static Configuration createConfiguration() throws Exception {
        Configuration configs = Configuration.load();
        String baseUri = System.getenv(ManagementConfiguration.URI);
        return ManagementConfiguration.configure(
                baseUri != null ? new URI(baseUri) : null,
                configs.getProperty(ManagementConfiguration.SUBSCRIPTION_ID).toString(),
                configs.getProperty(ManagementConfiguration.KEYSTORE_PATH).toString(),
                configs.getProperty(ManagementConfiguration.KEYSTORE_PASSWORD).toString(),
                KeyStoreType.fromString("bks")
        );
    }

    protected static void getLocation() throws Exception {
        ArrayList<String> serviceName = new ArrayList<String>();
        serviceName.add(LocationAvailableServiceNames.STORAGE);

        LocationsListResponse locationsListResponse = managementClient.getLocationsOperations().list();
        for (LocationsListResponse.Location location : locationsListResponse) {
            ArrayList<String> availableServicelist = location.getAvailableServices();
            String locationName = location.getName();
            if (availableServicelist.containsAll(serviceName)== true) {
                if (locationName.contains("West US") == true)
                {
                    storageLocation = locationName;
                }
                if (storageLocation==null)
                {
                    storageLocation = locationName;
                }
            }
        }
    }
    
    protected static String randomString(int length)
    {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++)
        {
                stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}