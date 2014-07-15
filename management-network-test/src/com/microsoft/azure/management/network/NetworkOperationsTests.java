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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import com.microsoft.azure.core.OperationResponse;
import com.microsoft.azure.core.utils.BOMInputStream;
import com.microsoft.azure.management.network.models.*;

import org.w3c.dom.Document;

public class NetworkOperationsTests extends NetworkManagementIntegrationTestBase {
    @Override
    public void setUp() throws Exception {
        createService(); 
        networkOperations = networkManagementClient.getNetworksOperations();
    }

    public void testGetConfiguration() throws Exception {
        //act
        NetworkGetConfigurationResponse operationResponse = networkOperations.getConfiguration();
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
        Assert.assertNotNull(operationResponse.getConfiguration());
    }
    
    public void testSetConfiguration() throws Exception {
        //act
        NetworkGetConfigurationResponse operationResponse = networkOperations.getConfiguration();

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
        Assert.assertNotNull(operationResponse.getConfiguration());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseDoc = documentBuilder.parse(new BOMInputStream(new ByteArrayInputStream(operationResponse.getConfiguration().getBytes())));

        DOMSource domSource = new DOMSource(responseDoc);
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, streamResult);

        NetworkSetConfigurationParameters parameters = new NetworkSetConfigurationParameters();
        parameters.setConfiguration(stringWriter.toString());
        OperationResponse response = networkOperations.setConfiguration(parameters);

        //Assert
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull(response.getRequestId());
    }
    
    public void testListNetworksSuccess() throws Exception {
        // Arrange  
        NetworkListResponse NetworkListResponse = networkOperations.list();
        ArrayList<NetworkListResponse.VirtualNetworkSite> virtualnetwoksitelist = NetworkListResponse.getVirtualNetworkSites();
        for (NetworkListResponse.VirtualNetworkSite networksite : virtualnetwoksitelist) {
            assertNotNull(networksite.getName());
            assertNotNull(networksite.getAffinityGroup());
            assertNotNull(networksite.getId());
            assertNotNull(networksite.getState());
            assertNotNull(networksite.getAddressSpace());
            assertNotNull(networksite.getSubnets());
        }
    }
}