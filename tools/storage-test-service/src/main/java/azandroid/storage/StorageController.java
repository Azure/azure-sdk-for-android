// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azandroid.storage;

import java.time.OffsetDateTime;

import azandroid.storage.applicationcontext.Settings;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasQueryParameters;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StorageController {
    private static ClientLogger logger = new ClientLogger(StorageController.class);
    private static Settings settings;
    static {
        ApplicationContext context
                = new FileSystemXmlApplicationContext("./settings.xml");
        settings = (Settings) context.getBean("settings");
    }

    @PostMapping("/create-container")
    public Mono<ResponseEntity> createContainer(@RequestBody CreateContainer createContainer) {
        if (createContainer.getContainerName() == null
                || createContainer.getContainerName() == "") {
            return Mono.just(ResponseEntity.badRequest().body(new Error("containerName is required.")));
        }
        String blobEndpointUri;
        try {
            StorageConnectionString connectionString = StorageConnectionString
                    .create(settings.getConnectionString(), logger);
            blobEndpointUri = connectionString.getBlobEndpoint().getPrimaryUri();
        } catch (Exception e) {
            return Mono.just(ResponseEntity
                    .status(500)
                    .body(new Error(e.getMessage())));
        }
        StorageSharedKeyCredential credential;
        try {
            credential = StorageSharedKeyCredential.fromConnectionString(settings.getConnectionString());
        } catch (Exception e) {
            return Mono.just(ResponseEntity
                    .status(500)
                    .body(new Error(e.getMessage())));
        }
        BlobContainerAsyncClient client = new BlobContainerClientBuilder()
                .credential(credential)
                .endpoint(blobEndpointUri)
                .containerName(createContainer.getContainerName())
                .buildAsyncClient();
        return client.create()
                .then(Mono.just(ResponseEntity.status(200).body(createContainer)));
    }

    @PostMapping("/generate-sas")
    public Mono<ResponseEntity> generateSas(@RequestBody CreateSasTokenInput createSasTokenInput) {
        if (createSasTokenInput.getContainerName() == null
                || createSasTokenInput.getContainerName() == "") {
            return Mono.just(ResponseEntity.badRequest().body(new Error("containerName is required.")));
        }
        if (createSasTokenInput.getBlobName() == null
            || createSasTokenInput.getBlobName() == "") {
            return Mono.just(ResponseEntity.badRequest().body(new Error("blobName is required.")));
        }
        if (createSasTokenInput.getAccessDurationInMinutes() == 0) {
            return Mono.just(ResponseEntity
                    .badRequest()
                    .body(new Error("accessDurationInMinutes is required and must be greater than 0")));
        }
        String blobEndpointUri;
        try {
            StorageConnectionString connectionString = StorageConnectionString
                    .create(settings.getConnectionString(), logger);
            blobEndpointUri = connectionString.getBlobEndpoint().getPrimaryUri();
        } catch (Exception e) {
            return Mono.just(ResponseEntity
                    .status(500)
                    .body(new Error(e.getMessage())));
        }
        StorageSharedKeyCredential credential;
        try {
            credential = StorageSharedKeyCredential.fromConnectionString(settings.getConnectionString());
        } catch (Exception e) {
            return Mono.just(ResponseEntity
                    .status(500)
                    .body(new Error(e.getMessage())));
        }
        BlobSasPermission blobPermission = new BlobSasPermission()
                .setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setAddPermission(true);
        BlobServiceSasSignatureValues builder = new BlobServiceSasSignatureValues()
                .setProtocol(SasProtocol.HTTPS_HTTP)
                .setStartTime(OffsetDateTime.now().minusMinutes(1))
                .setExpiryTime(OffsetDateTime.now().plusMinutes(createSasTokenInput.getAccessDurationInMinutes()))
                .setContainerName(createSasTokenInput.getContainerName())
                .setBlobName(createSasTokenInput.getBlobName())
                .setPermissions(blobPermission);
        BlobServiceSasQueryParameters sasQueryParameters = builder
                .generateSasQueryParameters(credential);
        String sasQueryParameter = sasQueryParameters.encode();
        String sasUri = blobEndpointUri
            + "/"
            + createSasTokenInput.getContainerName()
            +  "/"
            + createSasTokenInput.getBlobName()
            + "?" + sasQueryParameter;
        ResponseEntity entity = ResponseEntity
                .status(200)
                .body(new CreateSasTokenOutput(createSasTokenInput.getContainerName(),
                        createSasTokenInput.getBlobName(),
                        sasQueryParameter,
                        sasUri));
        return Mono.just(entity);
    }
}
