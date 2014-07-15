/*
 * Copyright 2013 Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.management;

import java.net.URI;

import junit.framework.Assert;

import com.microsoft.azure.Configuration;
import com.microsoft.azure.credentials.SubscriptionCloudCredentials;

public class ManagementClientTests extends ManagementIntegrationTestBase { 
    public void testGetCredential() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        managementClient = ManagementService.create(config);
        
        SubscriptionCloudCredentials subscriptionCloudCredentials = managementClient.getCredentials();      
        
        Assert.assertNotNull(subscriptionCloudCredentials.getSubscriptionId());          
    }
    
    public void testGetUri() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        managementClient = ManagementService.create(config);
        
        URI uri = managementClient.getBaseUri(); 
        URI expectUri = new URI("https://management.core.windows.net");
        
        Assert.assertEquals(expectUri.getHost(), uri.getHost());     
    }
}