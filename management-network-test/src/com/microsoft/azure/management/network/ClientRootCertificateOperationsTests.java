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

import java.util.ArrayList;

import junit.framework.Assert;

import com.microsoft.azure.core.OperationResponse;
import com.microsoft.azure.exception.ServiceException;
import com.microsoft.azure.management.network.models.*;

public class ClientRootCertificateOperationsTests extends NetworkManagementIntegrationTestBase {
    public static void setup() throws Exception {
        testNetworkName =  testNetworkPrefix + randomString(10);;
        createService();
        networkOperations = networkManagementClient.getNetworksOperations();
        createNetwork(testNetworkName);
        clientRootCertificateOperations = networkManagementClient.getClientRootCertificatesOperations();
    }

    public static void cleanup() throws Exception {
        try {
            ClientRootCertificateListResponse ClientRootCertificateListResponse = clientRootCertificateOperations.list(testNetworkName);
            ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
            for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist) {
                clientRootCertificateOperations.delete(testNetworkName, clientRootCertificate.getThumbprint());
            }
        } catch (ServiceException e) {
        }
    }
    
    public void createClientInvalidRootCertificatesFailed() throws Exception {
        try {
            String certificateValue = "InvalidRootCertificate";
            // Arrange
            ClientRootCertificateCreateParameters createParameters = new ClientRootCertificateCreateParameters();
            createParameters.setCertificate(certificateValue); 
            
            // Act
            OperationResponse operationResponse = clientRootCertificateOperations.create(testNetworkName, createParameters);
            
            // Assert
            Assert.assertEquals(201, operationResponse.getStatusCode());
            Assert.assertNotNull(operationResponse.getRequestId());
            
            fail("should have thrown");
        } catch (ServiceException e) {
            // success
        }
    }
    
    public void testGetClientRootCertificates() throws Exception {
        ClientRootCertificateListResponse ClientRootCertificateListResponse = networkManagementClient.getClientRootCertificatesOperations().list(testNetworkName);
        ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
        for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist) { 
            ClientRootCertificateGetResponse clientRootCertificateGetResponse = networkManagementClient.getClientRootCertificatesOperations().get(testNetworkName, clientRootCertificate.getThumbprint());
            Assert.assertEquals(200, clientRootCertificateGetResponse.getStatusCode());
            Assert.assertNotNull(clientRootCertificateGetResponse.getRequestId());
            Assert.assertNotNull(clientRootCertificateGetResponse.getCertificate());
        }
    }
    
    public void testListClientRootCertificatesSuccess() throws Exception {
        try {
             ClientRootCertificateListResponse ClientRootCertificateListResponse = networkManagementClient.getClientRootCertificatesOperations().list(testNetworkName);
             ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
             for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist) {
                Assert.assertNotNull(clientRootCertificate.getThumbprint());
                Assert.assertNotNull(clientRootCertificate.getExpirationTime());
                Assert.assertNotNull(clientRootCertificate.getSubject());
             }
        } catch (ServiceException e) {
        }
    }
}