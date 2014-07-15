package com.microsoft.azure.management;

import junit.framework.Assert;

import com.microsoft.azure.management.models.LocationsListResponse;

public class LocationOperationsTests  extends ManagementIntegrationTestBase {
    @Override
    public void setUp() throws Exception {
        createService();
    }

    public void testListLocationSuccess() throws Exception {
        LocationsListResponse locationsListResponse = managementClient.getLocationsOperations().list();
        Assert.assertEquals(200, locationsListResponse.getStatusCode());
        Assert.assertNotNull(locationsListResponse.getRequestId());
        Assert.assertTrue(locationsListResponse.getLocations().size() > 0);
    }
}