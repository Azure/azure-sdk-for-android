package com.microsoft.azure.management;

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.Assert;

import com.microsoft.azure.management.models.SubscriptionGetResponse;
import com.microsoft.azure.management.models.SubscriptionListOperationsParameters;
import com.microsoft.azure.management.models.SubscriptionListOperationsResponse;

public class SubscriptionOperationsTests  extends ManagementIntegrationTestBase {
    @Override
    public void setUp() throws Exception {
        createService();
    }

    public void testGetSubscriptionSuccess() throws Exception {
        // Act
        SubscriptionGetResponse subscriptionGetResponse = managementClient.getSubscriptionsOperations().get();
        // Assert
        Assert.assertEquals(200, subscriptionGetResponse.getStatusCode());
        Assert.assertNotNull(subscriptionGetResponse.getRequestId());
        Assert.assertNotNull(subscriptionGetResponse.getAccountAdminLiveEmailId());
        Assert.assertNotNull(subscriptionGetResponse.getSubscriptionID());
        
        Assert.assertEquals("Azure SDK sandbox", subscriptionGetResponse.getSubscriptionName());
        Assert.assertEquals(10, subscriptionGetResponse.getMaximumVirtualNetworkSites());
        Assert.assertEquals(10, subscriptionGetResponse.getMaximumLocalNetworkSites());
        Assert.assertEquals(9, subscriptionGetResponse.getMaximumDnsServers());
        Assert.assertEquals(20, subscriptionGetResponse.getMaximumStorageAccounts());
    }
    
    public void testListSubscriptionsSuccess() throws Exception {
         // Arrange  
         SubscriptionListOperationsParameters parameters = new  SubscriptionListOperationsParameters();

         Calendar now = Calendar.getInstance();
         Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         startTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 5));
         Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         endTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 1));
         parameters.setStartTime(startTime);
         parameters.setEndTime(endTime);
        
         SubscriptionListOperationsResponse subscriptionListOperationsResponse = managementClient.getSubscriptionsOperations().listOperations(parameters);
        
         Assert.assertEquals(200, subscriptionListOperationsResponse.getStatusCode());
         Assert.assertNotNull(subscriptionListOperationsResponse.getRequestId());
    }
}
    