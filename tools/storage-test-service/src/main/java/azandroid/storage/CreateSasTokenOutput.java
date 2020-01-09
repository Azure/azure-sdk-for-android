// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azandroid.storage;

public class CreateSasTokenOutput {
    private final String containerName;
    private final String blobName;
    private final String sasToken;
    private final String sasUri;

    public CreateSasTokenOutput(String containerName, String blobName, String sasToken, String sasUri) {
        this.containerName = containerName;
        this.blobName = blobName;
        this.sasToken = sasToken;
        this.sasUri = sasUri;
    }

    public String getContainerName() {
        return this.containerName;
    }

    public String getBlobName() {
        return this.blobName;
    }

    public String getSasToken() {
        return this.sasToken;
    }

    public String getSasUri() {
        return this.sasUri;
    }
}
