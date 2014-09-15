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
package com.microsoft.azure.management.website;

import java.net.URI;

import junit.framework.TestCase;

import com.microsoft.azure.Configuration;
import com.microsoft.azure.core.utils.KeyStoreType;
import com.microsoft.azure.management.configuration.ManagementConfiguration;
import com.microsoft.azure.management.websites.WebSiteManagementClient;
import com.microsoft.azure.management.websites.WebSiteManagementService;

public abstract class WebSiteManagementIntegrationTestBase extends TestCase {
    protected static String testWebsitePrefix = "azuresdktstswebsite";
    protected static WebSiteManagementClient webSiteManagementClient;

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        webSiteManagementClient = WebSiteManagementService.create(config);
    }

    protected static Configuration createConfiguration() throws Exception {
        Configuration configs = Configuration.load();
        return ManagementConfiguration.configure(
                new URI(configs.getProperty(ManagementConfiguration.URI).toString()),
                configs.getProperty(ManagementConfiguration.SUBSCRIPTION_ID).toString(),
                configs.getProperty(ManagementConfiguration.KEYSTORE_PATH).toString(),
                configs.getProperty(ManagementConfiguration.KEYSTORE_PASSWORD).toString(),
                KeyStoreType.fromString(configs.getProperty(ManagementConfiguration.KEYSTORE_TYPE).toString())
        );
    }
}