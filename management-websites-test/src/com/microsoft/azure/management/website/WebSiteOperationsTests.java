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
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;

import junit.framework.Assert;

import com.microsoft.azure.core.OperationResponse;
import com.microsoft.azure.exception.ServiceException;
import com.microsoft.azure.management.websites.models.*;

import org.xmlpull.v1.XmlPullParserException;

public class WebSiteOperationsTests extends WebSiteManagementIntegrationTestBase {
    private static String websiteName = testWebsitePrefix + "01";
    private static String webSpaceName = WebSpaceNames.WESTUSWEBSPACE; 
    private static String georegionName = GeoRegionNames.WESTUS;
    private static String hostName = ".azurewebsites.net";
    
    @Override
    public void setUp() throws Exception {
        createService();
        tearDown();
        createWebSite();
    }

    @Override
    public void tearDown() {
        WebSiteListParameters  webSiteListParameters = new  WebSiteListParameters();
        ArrayList<String> propertiesToInclude = new ArrayList<String>();
        webSiteListParameters.setPropertiesToInclude(propertiesToInclude);
        
        WebSiteDeleteParameters webSiteDeleteParameters = new WebSiteDeleteParameters();
        webSiteDeleteParameters.setDeleteAllSlots(true);
        webSiteDeleteParameters.setDeleteEmptyServerFarm(true);
        webSiteDeleteParameters.setDeleteMetrics(true);
        
        WebSpacesListWebSitesResponse webSpacesListWebSitesResponse = null;
        try {
            webSpacesListWebSitesResponse = webSiteManagementClient.getWebSpacesOperations().listWebSites(webSpaceName, webSiteListParameters);
        } catch (IOException e1) {
        } catch (XmlPullParserException e) {
        } catch (DatatypeConfigurationException e) {
        } catch (ServiceException e1) {
        } catch (URISyntaxException e1) {
        }
        if (webSpacesListWebSitesResponse != null) {
            ArrayList<WebSite> webSiteslist = webSpacesListWebSitesResponse.getWebSites(); 
            for (WebSite  webSite : webSiteslist)
            { 
                if (webSite.getName().startsWith(testWebsitePrefix ))
                {
                    String websitename = webSite.getName().replaceFirst(hostName, "");
                    try {
                        webSiteManagementClient.getWebSitesOperations().delete(webSpaceName, websitename, webSiteDeleteParameters);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ServiceException e) {
                        e.printStackTrace();
                    }
                }
            }  
        }
    }
    
    private static void createWebSite() throws Exception {
        ArrayList<String> hostNamesValue = new ArrayList<String>();
        hostNamesValue.add(websiteName + hostName); 
        
        WebSiteCreateParameters.WebSpaceDetails webSpaceDetails = new WebSiteCreateParameters.WebSpaceDetails();
        webSpaceDetails.setGeoRegion(georegionName);
        webSpaceDetails.setPlan(WebSpacePlanNames.VIRTUALDEDICATEDPLAN);
        webSpaceDetails.setName(webSpaceName);
        
        //Arrange
        WebSiteCreateParameters createParameters = new WebSiteCreateParameters();
        createParameters.setName(websiteName); 
        createParameters.setWebSpaceName(webSpaceName);
        createParameters.setWebSpace(webSpaceDetails);
        createParameters.setSiteMode(WebSiteMode.Basic);
        createParameters.setComputeMode(WebSiteComputeMode.Shared);
        createParameters.setHostNames(hostNamesValue);
        
        //Act
        WebSiteCreateResponse webSiteCreateResponse = webSiteManagementClient.getWebSitesOperations().create(webSpaceName, createParameters);
        Assert.assertEquals(200,  webSiteCreateResponse.getStatusCode());
        Assert.assertNotNull( webSiteCreateResponse.getRequestId());
    }
    
    public void testCreateWebSiteSuccess() throws Exception {
        String webSiteName = testWebsitePrefix  + "02";
        ArrayList<String> hostNamesValue = new ArrayList<String>();
        hostNamesValue.add(webSiteName + hostName); 
        
        WebSiteCreateParameters.WebSpaceDetails webSpaceDetails = new WebSiteCreateParameters.WebSpaceDetails();
        webSpaceDetails.setGeoRegion(georegionName);
        webSpaceDetails.setPlan(WebSpacePlanNames.VIRTUALDEDICATEDPLAN);
        webSpaceDetails.setName(webSpaceName);
        
        //Arrange
        WebSiteCreateParameters createParameters = new WebSiteCreateParameters();
        createParameters.setName(webSiteName); 
        createParameters.setWebSpaceName(webSpaceName);
        createParameters.setWebSpace(webSpaceDetails);
        createParameters.setSiteMode(WebSiteMode.Basic);
        createParameters.setComputeMode(WebSiteComputeMode.Shared);
        createParameters.setHostNames(hostNamesValue);
        
        //Act
        WebSiteCreateResponse webSiteCreateResponse = webSiteManagementClient.getWebSitesOperations().create(webSpaceName, createParameters);
            
        //Assert
        Assert.assertEquals(200,  webSiteCreateResponse.getStatusCode());
        Assert.assertNotNull( webSiteCreateResponse.getRequestId());
        Assert.assertEquals(webSiteName, webSiteCreateResponse.getWebSite().getName());
    }  
   
    public void testGetWebSiteSuccess() throws Exception { 
        //Act
        WebSiteGetParameters webSiteGetParameters = new WebSiteGetParameters();
        WebSiteGetResponse webSiteGetResponse = webSiteManagementClient.getWebSitesOperations().get(webSpaceName, websiteName, webSiteGetParameters);
        
        //Assert
        Assert.assertEquals(200, webSiteGetResponse.getStatusCode());
        Assert.assertNotNull(webSiteGetResponse.getRequestId()); 
        Assert.assertEquals(websiteName, webSiteGetResponse.getWebSite().getName());
    } 
    
    public void testUpdateWebSiteSuccess() throws Exception {
        //Arrange 
        WebSiteGetParameters webSiteGetParameters = new WebSiteGetParameters(); 
        
        //Act
        WebSiteGetResponse webSiteGetResponse = webSiteManagementClient.getWebSitesOperations().get(webSpaceName, websiteName, webSiteGetParameters);
        Assert.assertEquals(200, webSiteGetResponse.getStatusCode());
        
        WebSiteUpdateParameters updateParameters = new WebSiteUpdateParameters(); 
        updateParameters.setAvailabilityState(WebSpaceAvailabilityState.Limited);
        updateParameters.setSiteMode(WebSiteMode.Limited);
        updateParameters.setHostNames(null);
        updateParameters.setHostNameSslStates(null);
        updateParameters.setSslCertificates(null);
        
        
        OperationResponse updateoperationResponse = webSiteManagementClient.getWebSitesOperations().update(webSpaceName, websiteName, updateParameters);            
        //Assert
        Assert.assertEquals(200, updateoperationResponse.getStatusCode());
        Assert.assertNotNull(updateoperationResponse.getRequestId());
    } 
    
    public void testRepositoryoperationSuccess() throws Exception { 
        //Act             
        OperationResponse createResponse = webSiteManagementClient.getWebSitesOperations().createRepository(webSpaceName, websiteName);
        Assert.assertEquals(200, createResponse.getStatusCode());
        Assert.assertNotNull(createResponse.getRequestId());
        
        WebSiteGetRepositoryResponse  getResponse = webSiteManagementClient.getWebSitesOperations().getRepository(webSpaceName, websiteName);
        
        //Assert
        Assert.assertEquals(200, getResponse.getStatusCode());
        Assert.assertNotNull(getResponse.getRequestId());
        Assert.assertNotNull(getResponse.getUri());
    }
        
    public void testIsHostnameAvailableSuccess() throws Exception {        
        String webSiteNameInValid = websiteName;       
        String webSiteNameValid =testWebsitePrefix + "invalidsite"; 
      
        //Act               
        WebSiteIsHostnameAvailableResponse webSiteIsHostnameAvailableResponseInvalid = webSiteManagementClient.getWebSitesOperations().isHostnameAvailable(webSiteNameInValid);
        
        //Assert
        Assert.assertEquals(200, webSiteIsHostnameAvailableResponseInvalid.getStatusCode());
        Assert.assertNotNull(webSiteIsHostnameAvailableResponseInvalid.getRequestId()); 
        Assert.assertEquals(false, webSiteIsHostnameAvailableResponseInvalid.isAvailable());  
        
        WebSiteIsHostnameAvailableResponse webSiteIsHostnameAvailableResponseValid = webSiteManagementClient.getWebSitesOperations().isHostnameAvailable(webSiteNameValid);
        Assert.assertEquals(200, webSiteIsHostnameAvailableResponseValid.getStatusCode());
        Assert.assertNotNull(webSiteIsHostnameAvailableResponseValid.getRequestId()); 
        Assert.assertEquals(true, webSiteIsHostnameAvailableResponseValid.isAvailable());  
    }  
    
    public void testRestartWebSiteSuccess() throws Exception {       
        OperationResponse  operationResponse = webSiteManagementClient.getWebSitesOperations().restart(webSpaceName, websiteName);
        
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId()); 
    }
    
    public void testGeneratePasswordSuccess() throws Exception {        
        OperationResponse operationResponse = webSiteManagementClient.getWebSitesOperations().generatePassword(webSpaceName, websiteName);
        
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());        
    } 
    
    @SuppressWarnings("static-access")
    public void testGetConfigurationSuccess() throws Exception {
        //Act           
        WebSiteGetConfigurationResponse  webSiteGetConfigurationResponse = webSiteManagementClient.getWebSitesOperations().getConfiguration(webSpaceName, websiteName);

        //Assert
        Assert.assertEquals(200, webSiteGetConfigurationResponse.getStatusCode());
        Assert.assertNotNull(webSiteGetConfigurationResponse.getRequestId()); 
        Assert.assertEquals(false, (boolean)webSiteGetConfigurationResponse.isWebSocketsEnabled());  
        Assert.assertNull(webSiteGetConfigurationResponse.getDocumentRoot()); 
        Assert.assertEquals(35, webSiteGetConfigurationResponse.getLogsDirectorySizeLimit().intValue());  
        
        Assert.assertEquals(Calendar.YEAR, webSiteGetConfigurationResponse.getRequestTracingExpirationTime().YEAR);
        Assert.assertEquals(null, webSiteGetConfigurationResponse.getRemoteDebuggingVersion()); 
        Assert.assertEquals(0, webSiteGetConfigurationResponse.getConnectionStrings().size());     
    } 
    
    public void testGetHistoricalUsageMetricsSuccess() throws Exception {
        WebSiteGetHistoricalUsageMetricsParameters parameters = new WebSiteGetHistoricalUsageMetricsParameters();
        ArrayList<String> list = new ArrayList<String>();
        list.add("test");
        parameters.setMetricNames(list);
        
        Calendar now = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 5));            
        Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 1));
        parameters.setStartTime(startTime);
        parameters.setEndTime(endTime);
        
        //Act           
        WebSiteGetHistoricalUsageMetricsResponse webSiteGetHistoricalUsageMetricsResponse = webSiteManagementClient.getWebSitesOperations().getHistoricalUsageMetrics(webSpaceName, websiteName, parameters);
        
        //Assert
        Assert.assertEquals(200, webSiteGetHistoricalUsageMetricsResponse.getStatusCode());
        Assert.assertNotNull(webSiteGetHistoricalUsageMetricsResponse.getRequestId());
    }

    protected static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}