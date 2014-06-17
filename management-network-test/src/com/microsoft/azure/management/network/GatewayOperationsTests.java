/*
 * Copyright Microsoft.
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

package com.microsoft.azure.management.network;

import java.io.IOException;
import java.util.ArrayList;

import com.microsoft.azure.core.OperationResponse;
import com.microsoft.azure.exception.ServiceException;
import com.microsoft.azure.management.network.models.*;

import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.xmlpull.v1.XmlPullParserException;

public class GatewayOperationsTests extends NetworkManagementIntegrationTestBase {
    public static void setup() throws Exception {
        createService();
        networkOperations = networkManagementClient.getNetworksOperations();
        gatewayOperations = networkManagementClient.getGatewaysOperations();
        testNetworkName = testNetworkPrefix + randomString(10);
        testGatewayName = testGatewayPrefix + randomString(10);
        createNetwork(testNetworkName);
    }

    public static void cleanup() {
        deleteNetwork(testNetworkName);
        
        try {
            gatewayOperations.delete(testNetworkName);
        } catch (IOException e) {
        } catch (XmlPullParserException e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (ServiceException e) {
        }
    }
    
    public void testCreateGatewayOnEmptyNetworkFailed() throws Exception { 
        try {
            // Arrange
            GatewayCreateParameters gatewayCreateParameters = new GatewayCreateParameters();
            gatewayCreateParameters.setGatewayType(GatewayType.StaticRouting);
            
            // Act
            OperationResponse operationResponse = gatewayOperations.create(testNetworkName, gatewayCreateParameters);
            
            // Assert
            Assert.assertEquals(201, operationResponse.getStatusCode());
            Assert.assertNotNull(operationResponse.getRequestId());
            
            fail("Exception expected");
        } catch (ExecutionException e) {
            // success
        }
    }
    
    public void testGetGatewaySuccess() throws Exception {
        // Act
        GatewayGetResponse gatewayGetResponse = gatewayOperations.get(testNetworkName);
        // Assert
        Assert.assertEquals(200, gatewayGetResponse.getStatusCode());
        Assert.assertNotNull(gatewayGetResponse.getRequestId());
    }
    
    public void testListGatewayFailedWithInsufficientPermission() throws Exception {
        try {
            // Arrange  
            GatewayListConnectionsResponse gatewayListConnectionsResponse = gatewayOperations.listConnections(testNetworkName);
            ArrayList<GatewayListConnectionsResponse.GatewayConnection> gatewayConnectionlist = gatewayListConnectionsResponse.getConnections();
            for (GatewayListConnectionsResponse.GatewayConnection gatewayConnection : gatewayConnectionlist )    { 
                Assert.assertNotNull(gatewayConnection.getAllocatedIPAddresses());
                Assert.assertNotNull(gatewayConnection.getConnectivityState());
                Assert.assertNotNull(gatewayConnection.getEgressBytesTransferred());
                Assert.assertNotNull(gatewayConnection.getIngressBytesTransferred());
                Assert.assertNotNull(gatewayConnection.getLastConnectionEstablished());
                Assert.assertNotNull(gatewayConnection.getLastEvent());
                Assert.assertNotNull(gatewayConnection.getLocalNetworkSiteName());
            }
            
            fail("should have thrown");
        } catch (ServiceException e) {
            // success
        }
    }
}