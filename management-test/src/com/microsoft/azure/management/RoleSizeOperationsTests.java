package com.microsoft.azure.management;

import junit.framework.Assert;

import com.microsoft.azure.management.models.RoleSizeListResponse;

public class RoleSizeOperationsTests  extends ManagementIntegrationTestBase {
    @Override
    public void setUp() throws Exception {
        createService();
    }

    public void testListRoleSizeSuccess() throws Exception {
        RoleSizeListResponse roleSizeListResponse = managementClient.getRoleSizesOperations().list();

        Assert.assertEquals(200, roleSizeListResponse.getStatusCode());
        Assert.assertNotNull(roleSizeListResponse.getRequestId());
        Assert.assertTrue(roleSizeListResponse.getRoleSizes().size() > 0);
    }
}