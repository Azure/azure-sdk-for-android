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

package com.microsoft.azure.management.website;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.Assert;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import com.microsoft.azure.exception.ServiceException;
import com.microsoft.azure.management.websites.models.ServerFarmCreateParameters;
import com.microsoft.azure.management.websites.models.ServerFarmCreateResponse;
import com.microsoft.azure.management.websites.models.ServerFarmGetResponse;
import com.microsoft.azure.management.websites.models.ServerFarmListResponse;
import com.microsoft.azure.management.websites.models.ServerFarmStatus;
import com.microsoft.azure.management.websites.models.ServerFarmUpdateParameters;
import com.microsoft.azure.management.websites.models.ServerFarmUpdateResponse;
import com.microsoft.azure.management.websites.models.ServerFarmWorkerSize;

public class ServerFarmOperationsTests extends WebSiteManagementIntegrationTestBase {
    @Override
    public void setUp() throws Exception {
        createService();
    }

    @Override
    public void tearDown() throws Exception {
        deleteServerFarm("northcentraluswebspace");
        deleteServerFarm("eastuswebspace");
    }

    private static void deleteServerFarm(String webSpaceName) throws IOException, ParserConfigurationException, SAXException {
        try {
            webSiteManagementClient.getServerFarmsOperations().delete(webSpaceName);
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
    }
    
    private void createServerFarm(String webSpaceName) throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException, URISyntaxException, XmlPullParserException, DatatypeConfigurationException {
        int currentNumberOfWorkersValue = 2; 
        int numberOfWorkersValue = 2;
        ServerFarmCreateParameters createParameters = new ServerFarmCreateParameters();
        createParameters.setCurrentNumberOfWorkers(currentNumberOfWorkersValue); 
        createParameters.setCurrentWorkerSize(ServerFarmWorkerSize.Small);
        createParameters.setNumberOfWorkers(numberOfWorkersValue);
        createParameters.setStatus(ServerFarmStatus.Pending);
        createParameters.setWorkerSize(ServerFarmWorkerSize.Small);
        webSiteManagementClient.getServerFarmsOperations().create(webSpaceName, createParameters);
    }

    public void testCreateServerFarmSuccess() throws Exception {
        String webSpaceName = "northcentraluswebspace";
        int currentNumberOfWorkersValue = 2; 
        int numberOfWorkersValue = 2;
        
        // Arrange
        ServerFarmCreateParameters createParameters = new ServerFarmCreateParameters();
        createParameters.setCurrentNumberOfWorkers(currentNumberOfWorkersValue); 
        createParameters.setCurrentWorkerSize(ServerFarmWorkerSize.Small);
        createParameters.setNumberOfWorkers(numberOfWorkersValue);
        createParameters.setStatus(ServerFarmStatus.Pending);        
        createParameters.setWorkerSize(ServerFarmWorkerSize.Small);       
        
        // Act
        ServerFarmCreateResponse serverFarmCreateResponse = webSiteManagementClient.getServerFarmsOperations().create(webSpaceName, createParameters);
        
        // Assert
        Assert.assertEquals(200,  serverFarmCreateResponse.getStatusCode());
        Assert.assertNotNull(serverFarmCreateResponse.getRequestId());
        Assert.assertEquals(currentNumberOfWorkersValue, serverFarmCreateResponse.getCurrentNumberOfWorkers()); 
        Assert.assertEquals(ServerFarmWorkerSize.Small, serverFarmCreateResponse.getCurrentWorkerSize()); 
    }

    public void testGetServerFarmSuccess() throws Exception {
        String webSpaceName = "eastuswebspace";
        String serverFarmName = "";

        // Act
        ServerFarmGetResponse serverFarmGetResponse = webSiteManagementClient.getServerFarmsOperations().get(webSpaceName, serverFarmName);

        // Assert
        Assert.assertEquals(200, serverFarmGetResponse.getStatusCode());
        Assert.assertNotNull(serverFarmGetResponse.getRequestId());
    }

    public void testListServerFarmSuccess() throws Exception {
        String webSpaceName = "eastuswebspace"; 
       
        // Act
        ServerFarmListResponse serverFarmListResponse = webSiteManagementClient.getServerFarmsOperations().list(webSpaceName);

        // Assert
        Assert.assertEquals(200, serverFarmListResponse.getStatusCode());
        Assert.assertNotNull(serverFarmListResponse.getRequestId());
        
         ArrayList<ServerFarmListResponse.ServerFarm> serverFarmslist = serverFarmListResponse.getServerFarms(); 
           for (ServerFarmListResponse.ServerFarm serverFarm : serverFarmslist) { 
                // Assert               
             Assert.assertNotNull(serverFarm.getCurrentWorkerSize());
             Assert.assertEquals("Default1", serverFarm.getName());  
             Assert.assertNotNull(serverFarm.getStatus());
             Assert.assertNotNull(serverFarm.getWorkerSize());             
           }
    }

    public void testUpdateServerFarmSuccess() throws Exception {
        String webSpaceName = "eastuswebspace"; 
        
        int currentNumberOfWorkersValue = 3;
        int numberOfWorkersValue = 3;
        
        // Arrange 
        createServerFarm(webSpaceName);

        // Act             
        ServerFarmUpdateParameters updateParameters = new ServerFarmUpdateParameters();
        updateParameters.setCurrentNumberOfWorkers(currentNumberOfWorkersValue);
        updateParameters.setCurrentWorkerSize(ServerFarmWorkerSize.Medium);       
        updateParameters.setNumberOfWorkers(numberOfWorkersValue); 
        updateParameters.setStatus(ServerFarmStatus.Ready);
        updateParameters.setWorkerSize(ServerFarmWorkerSize.Medium); 
        ServerFarmUpdateResponse updateOperationResponse = webSiteManagementClient.getServerFarmsOperations().update(webSpaceName, updateParameters);
        
        // Assert
        Assert.assertEquals(200,  updateOperationResponse.getStatusCode());
        Assert.assertNotNull(updateOperationResponse.getRequestId());
       
        Assert.assertEquals(3, updateOperationResponse.getCurrentNumberOfWorkers());
        Assert.assertEquals(3, updateOperationResponse.getNumberOfWorkers());
        Assert.assertEquals(ServerFarmWorkerSize.Medium, updateOperationResponse.getCurrentWorkerSize());
        Assert.assertEquals("DefaultServerFarm", updateOperationResponse.getName());  
        Assert.assertEquals(ServerFarmStatus.Ready, updateOperationResponse.getStatus());
        Assert.assertEquals(ServerFarmWorkerSize.Medium, updateOperationResponse.getWorkerSize()); 
    }
}