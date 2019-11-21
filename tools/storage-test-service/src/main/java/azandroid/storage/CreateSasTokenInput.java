// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azandroid.storage;

public class CreateSasTokenInput {
    private String containerName;
    private String blobName;
    private int accessDurationInMinutes;

    public String getContainerName() {
        return this.containerName;
    }

    public String getBlobName() {
        return this.blobName;
    }

    public int getAccessDurationInMinutes() {
        return this.accessDurationInMinutes;
    }
}
