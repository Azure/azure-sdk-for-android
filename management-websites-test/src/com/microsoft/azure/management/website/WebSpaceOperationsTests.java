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

import java.util.ArrayList;

import junit.framework.Assert;

import com.microsoft.azure.exception.ServiceException;
import com.microsoft.azure.management.websites.models.WebSite;
import com.microsoft.azure.management.websites.models.WebSiteListParameters;
import com.microsoft.azure.management.websites.models.WebSpaceAvailabilityState;
import com.microsoft.azure.management.websites.models.WebSpacesGetDnsSuffixResponse;
import com.microsoft.azure.management.websites.models.WebSpacesGetResponse;
import com.microsoft.azure.management.websites.models.WebSpacesListGeoRegionsResponse;
import com.microsoft.azure.management.websites.models.WebSpacesListPublishingUsersResponse;
import com.microsoft.azure.management.websites.models.WebSpacesListResponse;
import com.microsoft.azure.management.websites.models.WebSpacesListWebSitesResponse;

public class WebSpaceOperationsTests extends WebSiteManagementIntegrationTestBase {
    @Override
    public void setUp() throws Exception {
        createService();
        tearDown();
    }

    @Override
    public void tearDown() throws Exception {
        String webSpaceName = "northcentraluswebspace"; 
        try {
            webSiteManagementClient.getServerFarmsOperations().delete(webSpaceName);
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public void testGetWebSpaceSuccess() throws Exception {
        String webSpaceName = "eastuswebspace";

        // Act
        WebSpacesGetResponse webSpaceGetResponse = webSiteManagementClient.getWebSpacesOperations().get(webSpaceName);

        // Assert
        Assert.assertEquals(200, webSpaceGetResponse.getStatusCode());
        Assert.assertNotNull(webSpaceGetResponse.getRequestId()); 
        
        //Assert.assertEquals(3, webSpaceGetResponse.getCurrentNumberOfWorkers());
        //Assert.assertEquals(WebSpaceWorkerSize.Medium, webSpaceGetResponse.getCurrentWorkerSize());       
        //Assert.assertEquals("eastuswebspace", webSpaceGetResponse.getName());  
//        Assert.assertEquals(WebSpaceStatus.Ready, webSpaceGetResponse.getStatus());
//        Assert.assertEquals(WebSpaceWorkerSize.Medium, webSpaceGetResponse.getWorkerSize());
//        
//        Assert.assertEquals(WebSpaceAvailabilityState.Normal, webSpaceGetResponse.getAvailabilityState());  
//        Assert.assertEquals("East US", webSpaceGetResponse.getGeoLocation());  
//        Assert.assertEquals(3, webSpaceGetResponse.getWorkerSize());
    }

    public void testGetDnsSuffixSuccess() throws Exception {        
        WebSpacesGetDnsSuffixResponse  webSpacesGetDnsSuffixResponse = webSiteManagementClient.getWebSpacesOperations().getDnsSuffix();
        // Assert
        Assert.assertEquals(200, webSpacesGetDnsSuffixResponse.getStatusCode());
        Assert.assertNotNull(webSpacesGetDnsSuffixResponse.getRequestId()); 
        Assert.assertEquals("azurewebsites.net", webSpacesGetDnsSuffixResponse.getDnsSuffix());
    }

    public void testListPublishingUsersSuccess() throws Exception {
        // Act
        WebSpacesListPublishingUsersResponse webSpacesListPublishingUsersResponse = webSiteManagementClient.getWebSpacesOperations().listPublishingUsers();

        // Assert
        Assert.assertEquals(200,webSpacesListPublishingUsersResponse.getStatusCode());
        Assert.assertNotNull(webSpacesListPublishingUsersResponse.getRequestId());

        ArrayList< WebSpacesListPublishingUsersResponse.User> userlist =  webSpacesListPublishingUsersResponse.getUsers(); 
        for (WebSpacesListPublishingUsersResponse.User user : userlist) { 
             Assert.assertNotNull(user.getName());
        }
    }
    
    public void testListGeoRegionsSuccess() throws Exception {
        // Act
        WebSpacesListGeoRegionsResponse  webSpacesListGeoRegionsResponse = webSiteManagementClient.getWebSpacesOperations().listGeoRegions();
        // Assert
        Assert.assertEquals(200,  webSpacesListGeoRegionsResponse.getStatusCode());
        Assert.assertNotNull(webSpacesListGeoRegionsResponse.getRequestId());    

        ArrayList<WebSpacesListGeoRegionsResponse.GeoRegion> geoRegionslist = webSpacesListGeoRegionsResponse.getGeoRegions(); 
        for (WebSpacesListGeoRegionsResponse.GeoRegion geoRegion : geoRegionslist) { 
            Assert.assertNotNull(geoRegion.getName());
        }
    }
    
    public void testListWebSpaceSuccess() throws Exception {
        // Act
        WebSpacesListResponse webSpacesListResponse = webSiteManagementClient.getWebSpacesOperations().list();
        // Assert
        Assert.assertEquals(200,  webSpacesListResponse.getStatusCode());
        Assert.assertNotNull( webSpacesListResponse.getRequestId());

        ArrayList<WebSpacesListResponse.WebSpace> webSpacelist = webSpacesListResponse.getWebSpaces(); 
        for (WebSpacesListResponse.WebSpace  webspace : webSpacelist) {
            Assert.assertNotNull(webspace.getAvailabilityState());
            Assert.assertNotNull(webspace.getName()); 
        }
    }
    
    public void testListWebSitesSuccess() throws Exception {
        String webSpaceName = "eastuswebspace"; 
        WebSiteListParameters  webSiteListParameters = new  WebSiteListParameters();
        ArrayList<String> propertiesToInclude = new ArrayList<String>();
        webSiteListParameters.setPropertiesToInclude(propertiesToInclude);

        // Act
        WebSpacesListWebSitesResponse webSpacesListWebSitesResponse = webSiteManagementClient.getWebSpacesOperations().listWebSites(webSpaceName, webSiteListParameters);

        // Assert
        Assert.assertEquals(200, webSpacesListWebSitesResponse.getStatusCode());
        Assert.assertNotNull(webSpacesListWebSitesResponse.getRequestId());
        
        ArrayList<WebSite> webSiteslist = webSpacesListWebSitesResponse.getWebSites(); 
        for (WebSite  webSite : webSiteslist) { 
             //Assert
             Assert.assertEquals(WebSpaceAvailabilityState.Normal, webSite.getAvailabilityState());
             Assert.assertNotNull(webSite.getName()); 
        }
    }
}