// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.ArchiveStatus;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobProperties;
import com.azure.android.storage.blob.models.BlobRequestConditions;

// Internal Util-Class used by convenience layer API impl.
final class ClientUtil {
    private ClientUtil() {
    }

    static BlobProperties buildBlobProperties(BlobGetPropertiesHeaders headers) {
        // blobSize determination - contentLength only returns blobSize if the download is not chunked.
        BlobProperties properties = new BlobProperties(headers.getCreationTime(), headers.getLastModified(),
            headers.getETag(),
            headers.getContentLength() == null ? 0 : headers.getContentLength(),
            headers.getContentType(),
            headers.getContentMD5(), headers.getContentEncoding(),
            headers.getContentDisposition(),
            headers.getContentLanguage(), headers.getCacheControl(),
            headers.getBlobSequenceNumber(), headers.getBlobType(),
            headers.getLeaseStatus(), headers.getLeaseState(),
            headers.getLeaseDuration(), headers.getCopyId(),
            headers.getCopyStatus(), headers.getCopySource(),
            headers.getCopyProgress(),
            headers.getCopyCompletionTime(),
            headers.getCopyStatusDescription(),
            headers.isServerEncrypted(), headers.isIncrementalCopy(), headers.getDestinationSnapshot(),
            AccessTier.fromString(headers.getAccessTier()), headers.isAccessTierInferred(),
            ArchiveStatus.fromString(headers.getArchiveStatus()), headers.getEncryptionKeySha256(),
            headers.getEncryptionScope(), headers.getAccessTierChangeTime(),
            headers.getMetadata(), headers.getBlobCommittedBlockCount());
        return properties;
    }

    static com.azure.android.storage.blob.implementation.models.GetBlobPropertiesOptions toImplOptions(
        com.azure.android.storage.blob.models.GetBlobPropertiesOptions options) {

        com.azure.android.storage.blob.implementation.models.GetBlobPropertiesOptions implOptions
            = new com.azure.android.storage.blob.implementation.models.GetBlobPropertiesOptions();

        implOptions.setCancellationToken(options.getCancellationToken());
        implOptions.setCpkInfo(options.getCpkInfo());
        final BlobRequestConditions blobRequestConditions = options.getBlobRequestConditions();
        if (blobRequestConditions != null) {
            implOptions.setLeaseId(blobRequestConditions.getLeaseId());
            implOptions.setIfMatch(blobRequestConditions.getIfMatch());
            implOptions.setIfNoneMatch(blobRequestConditions.getIfNoneMatch());
            implOptions.setIfModifiedSince(blobRequestConditions.getIfModifiedSince());
            implOptions.setIfUnmodifiedSince(blobRequestConditions.getIfUnmodifiedSince());
        }
        implOptions.setRequestId(options.getRequestId());
        implOptions.setTimeout(options.getTimeout());
        implOptions.setSnapshot(options.getSnapshot());
        implOptions.setVersion(options.getVersion());
        return implOptions;
    }
}
