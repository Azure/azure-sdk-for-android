package com.microsoft.azure.management;

import java.util.ArrayList;

import junit.framework.Assert;

import com.microsoft.azure.management.models.ManagementCertificateGetResponse;
import com.microsoft.azure.management.models.ManagementCertificateListResponse;

public class ManagementCertificateOperationsTests extends ManagementIntegrationTestBase {
    public static void setup() throws Exception {
        createService();       
    }
    
    public void testGetManagementCertificateSuccess() throws Exception {
        
        // arrange
           ManagementCertificateListResponse managementCertificateListResponse = managementClient.getManagementCertificatesOperations().list();
           ArrayList<ManagementCertificateListResponse.SubscriptionCertificate> managementCertificatelist = managementCertificateListResponse.getSubscriptionCertificates();
        
           if (managementCertificatelist.size() > 0) {
               String thumbprint = managementCertificatelist.get(0).getThumbprint();

               ManagementCertificateGetResponse managementCertificateResponse = managementClient.getManagementCertificatesOperations().get(thumbprint);

               // Assert
               Assert.assertEquals(200, managementCertificateResponse.getStatusCode());
               Assert.assertNotNull(managementCertificateResponse.getRequestId()); 
               Assert.assertEquals(thumbprint, managementCertificateResponse.getThumbprint());    
           }
    }
    
    public void testListManagementCertificateSuccess() throws Exception {
        // Arrange  
         ManagementCertificateListResponse managementCertificateListResponse = managementClient.getManagementCertificatesOperations().list();
         ArrayList<ManagementCertificateListResponse.SubscriptionCertificate> managementCertificatelist = managementCertificateListResponse.getSubscriptionCertificates();
         
         Assert.assertNotNull(managementCertificatelist);;        
    }
}
