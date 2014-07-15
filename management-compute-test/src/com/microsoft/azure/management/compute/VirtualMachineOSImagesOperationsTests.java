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

package com.microsoft.azure.management.compute;

import java.net.URI;
import java.util.ArrayList;

import junit.framework.Assert;

import com.microsoft.azure.core.OperationResponse;
import com.microsoft.azure.exception.ServiceException;
import com.microsoft.azure.management.compute.models.*;

public class VirtualMachineOSImagesOperationsTests extends ComputeManagementIntegrationTestBase {
    static int random = (int)(Math.random()* 100);
    static String virtualMachineOSImageName = testVMPrefix + "OSImage" + random; 

    static String storageAccountName = testStoragePrefix + randomString(10);
    static String storageContainer = "image-store";

    static String vhdfileName = "oneGBFixedWS2008R2.vhd";
    static String filePath = "D:\\test\\vhdfile\\";

    @Override
    public void setUp() throws Exception {
        //create storage service for storage account creation
        createStorageManagementClient();
        //create compute management service for all compute management operation
        createComputeManagementClient();
        //create management service for accessing management operation
        createManagementClient();
        //dynamic get location for vm storage/hosted service
        getLocation();
        //create a new storage account for vm .vhd storage.
        createStorageAccount(storageAccountName, storageContainer);
        uploadFileToBlob(storageAccountName, storageContainer, vhdfileName, filePath);
        createVirtualMachineOSImage();
    }

    @Override
    public void tearDown() throws Exception {
        deleteVirtualMachineOSImages();
        cleanBlob(storageAccountName, storageContainer);
        cleanStorageAccount(storageAccountName);
    }
    
    private static void deleteVirtualMachineOSImages() throws Exception {
        try
        {
            VirtualMachineOSImageListResponse virtualMachineOSImageListResponse = computeManagementClient.getVirtualMachineOSImagesOperations().list();
            ArrayList<VirtualMachineOSImageListResponse.VirtualMachineOSImage> virtualMachineOSImagelist = virtualMachineOSImageListResponse.getImages();
            for (VirtualMachineOSImageListResponse.VirtualMachineOSImage virtualMachineOSImage : virtualMachineOSImagelist)
            {
                if (virtualMachineOSImage.getName().contains(virtualMachineOSImageName))
                {
                    computeManagementClient.getVirtualMachineOSImagesOperations().delete(virtualMachineOSImage.getName(), true);
                }
            }
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public static void createVirtualMachineOSImage() throws Exception {
         String virtualMachineOSImageDescription =  virtualMachineOSImageName + "Description";
         URI mediaLinkUriValue =  new URI("http://"+ blobhost+ "/" +storageContainer+ "/" + vhdfileName);
         
        //Arrange
        VirtualMachineOSImageCreateParameters createParameters = new VirtualMachineOSImageCreateParameters();
        createParameters.setName(virtualMachineOSImageName);
        createParameters.setLabel(virtualMachineOSImageDescription);
        createParameters.setMediaLinkUri(mediaLinkUriValue);
        createParameters.setOperatingSystemType(VirtualMachineOSImageOperatingSystemType.WINDOWS);
        
        //Act
        OperationResponse operationResponse = computeManagementClient.getVirtualMachineOSImagesOperations().create(createParameters);        
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }

    public void testGetVirtualMachineOSImages() throws Exception {
        //Act
        VirtualMachineOSImageGetResponse virtualMachineOSImageResponse = computeManagementClient.getVirtualMachineOSImagesOperations().get(virtualMachineOSImageName);

        //Assert
        Assert.assertEquals(200, virtualMachineOSImageResponse.getStatusCode());
        Assert.assertNotNull(virtualMachineOSImageResponse.getRequestId());
        Assert.assertEquals(virtualMachineOSImageName, virtualMachineOSImageResponse.getName());
    }

    public void testListVirtualMachineOSImagesSuccess() throws Exception {
        //Arrange
        VirtualMachineOSImageListResponse virtualMachineOSImageListResponse = computeManagementClient.getVirtualMachineOSImagesOperations().list();
        ArrayList<VirtualMachineOSImageListResponse.VirtualMachineOSImage> virtualMachineOSImagelist = virtualMachineOSImageListResponse.getImages();
        Assert.assertTrue(virtualMachineOSImagelist.size() >= 1);
    }

    public void testUpdateVirtualMachineOSImagesuccess() throws Exception {
        //Arrange
        String virtualMachineOSImageLabel = virtualMachineOSImageName + "Label";
        String expectedUpdatedVirtualMachineOSImageLabel = virtualMachineOSImageLabel + "updated";        
        String expectedDescription = "updateVirtualMachineOSImagesuccess";

        //Act
        VirtualMachineOSImageUpdateParameters updateParameters = new VirtualMachineOSImageUpdateParameters();      
        updateParameters.setLabel(expectedUpdatedVirtualMachineOSImageLabel);
        updateParameters.setDescription(expectedDescription);
        OperationResponse updateoperationResponse = computeManagementClient.getVirtualMachineOSImagesOperations().update(virtualMachineOSImageName, updateParameters);
 
        //Assert
        Assert.assertEquals(200, updateoperationResponse.getStatusCode());
        Assert.assertNotNull(updateoperationResponse.getRequestId());
    }
}