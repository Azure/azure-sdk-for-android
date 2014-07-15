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
package com.microsoft.azure.management.sql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.Assert;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import com.microsoft.azure.exception.ServiceException;
import com.microsoft.azure.management.sql.models.DatabaseOperation;
import com.microsoft.azure.management.sql.models.DatabaseOperationGetResponse;
import com.microsoft.azure.management.sql.models.DatabaseOperationListResponse;

public class DatabaseOperationOperationsIntegrationTest extends SqlManagementIntegrationTestBase {

    private static Map<String, String> databaseToBeRemoved = new HashMap<String, String>();
    private static DatabaseOperationOperations databaseOperationOperations;

    @Override
    public void setUp() throws Exception {
        createService();
        databaseOperations = sqlManagementClient.getDatabasesOperations();
        serverOperations = sqlManagementClient.getServersOperations();
        databaseOperationOperations = sqlManagementClient.getDatabaseOperationsOperations();
    }

    @Override
    public void tearDown() throws Exception {
        for (String databaseName : databaseToBeRemoved.keySet()) {
            String serverName = databaseToBeRemoved.get(databaseName);
            databaseOperations.delete(serverName, databaseName);
        }

        for (String serverName : serverToBeRemoved) {
            serverOperations.delete(serverName);
        }
    }

    public void testListDatabaseOperationsOperationSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException, DatatypeConfigurationException, XmlPullParserException {
        // arrange 
        String serverName = createServer();
        createDatabase(serverName);
        
        // act 
        DatabaseOperationListResponse databaseOperationOperationsListResponse = databaseOperationOperations.listByServer(serverName);
        
        // assert
        Assert.assertEquals(1, databaseOperationOperationsListResponse.getDatabaseOperations().size());
    }

    public void testGetDatabaseOperationsOperationSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException, DatatypeConfigurationException, XmlPullParserException {
        // arrange 
        String serverName = createServer();
        createDatabase(serverName);

        // act 
        DatabaseOperationListResponse databaseOperationOperationsListResponse = databaseOperationOperations.listByServer(serverName);
        DatabaseOperation databaseOperation = databaseOperationOperationsListResponse.getDatabaseOperations().get(0);
        DatabaseOperationGetResponse databaseOperationGetResponse = databaseOperationOperations.get(serverName, databaseOperation.getId());
        DatabaseOperation actualDatabaseOperation = databaseOperationGetResponse.getDatabaseOperation();

        // assert
        Assert.assertEquals(databaseOperation.getDatabaseName(), actualDatabaseOperation.getDatabaseName());
        Assert.assertEquals(databaseOperation.getId(), actualDatabaseOperation.getId());
    }
}