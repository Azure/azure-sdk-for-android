// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.ArchiveStatus;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobListDetails;
import com.azure.android.storage.blob.models.BlobProperties;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;

import java.util.ArrayList;
import java.util.List;

// Internal Util-Class used by convenience layer API impl.
final class ClientUtil {
    private ClientUtil() {
    }

    static BlobProperties buildBlobProperties(BlobGetPropertiesHeaders headers) {
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

    static BlockBlobItem buildBlockBlobItem(BlockBlobCommitBlockListHeaders headers) {
        return new BlockBlobItem(headers.getETag(), headers.getLastModified(), headers.getContentMD5(),
            headers.isServerEncrypted(), headers.getEncryptionKeySha256());
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

    static com.azure.android.storage.blob.implementation.models.StageBlockOptions toImplOptions(
        com.azure.android.storage.blob.models.StageBlockOptions options) {

        com.azure.android.storage.blob.implementation.models.StageBlockOptions implOptions
            = new com.azure.android.storage.blob.implementation.models.StageBlockOptions();

        implOptions.setCancellationToken(options.getCancellationToken());
        implOptions.setCpkInfo(options.getCpkInfo());
        implOptions.setLeaseId(options.getLeaseId());
        implOptions.setRequestId(options.getRequestId());
        implOptions.setTimeout(options.getTimeout());
        implOptions.setTransactionalContentCrc64(options.getTransactionalContentCrc64());
        return implOptions;
    }

    static com.azure.android.storage.blob.implementation.models.CommitBlockListOptions toImplOptions(
        com.azure.android.storage.blob.models.CommitBlockListOptions options) {

        com.azure.android.storage.blob.implementation.models.CommitBlockListOptions implOptions
            = new com.azure.android.storage.blob.implementation.models.CommitBlockListOptions();

        implOptions.setCancellationToken(options.getCancellationToken());
        implOptions.setTransactionalContentMD5(options.getTransactionalContentMD5());
        implOptions.setTransactionalContentCrc64(options.getTransactionalContentCrc64());
        implOptions.setTimeout(options.getTimeout());
        implOptions.setBlobHttpHeaders(options.getBlobHttpHeaders());
        implOptions.setMetadata(options.getMetadata());
        implOptions.setRequestId(options.getRequestId());
        implOptions.setCpkInfo(options.getCpkInfo());
        final BlobRequestConditions blobRequestConditions = options.getBlobRequestConditions();
        if (blobRequestConditions != null) {
            implOptions.setLeaseId(blobRequestConditions.getLeaseId());
            implOptions.setIfMatch(blobRequestConditions.getIfMatch());
            implOptions.setIfNoneMatch(blobRequestConditions.getIfNoneMatch());
            implOptions.setIfModifiedSince(blobRequestConditions.getIfModifiedSince());
            implOptions.setIfUnmodifiedSince(blobRequestConditions.getIfUnmodifiedSince());
        }
        implOptions.setTier(options.getTier());
        return implOptions;

    }

    static com.azure.android.storage.blob.implementation.models.BlobDeleteOptions toImplOptions(
        com.azure.android.storage.blob.models.BlobDeleteOptions options) {

        com.azure.android.storage.blob.implementation.models.BlobDeleteOptions implOptions
            = new com.azure.android.storage.blob.implementation.models.BlobDeleteOptions();

        implOptions.setCancellationToken(options.getCancellationToken());
        implOptions.setSnapshot(options.getSnapshot());
        implOptions.setTimeout(options.getTimeout());
        implOptions.setVersion(options.getVersion());
        implOptions.setDeleteSnapshots(options.getDeleteSnapshots());

        final BlobRequestConditions blobRequestConditions = options.getBlobRequestConditions();
        if (blobRequestConditions != null) {
            implOptions.setLeaseId(blobRequestConditions.getLeaseId());
            implOptions.setIfMatch(blobRequestConditions.getIfMatch());
            implOptions.setIfNoneMatch(blobRequestConditions.getIfNoneMatch());
            implOptions.setIfModifiedSince(blobRequestConditions.getIfModifiedSince());
            implOptions.setIfUnmodifiedSince(blobRequestConditions.getIfUnmodifiedSince());
        }

        implOptions.setRequestId(options.getRequestId());

        return implOptions;
    }

    static com.azure.android.storage.blob.implementation.models.ListBlobFlatSegmentOptions toImplOptions(String pageId, ListBlobsOptions options) {
        com.azure.android.storage.blob.implementation.models.ListBlobFlatSegmentOptions implOptions
            = new com.azure.android.storage.blob.implementation.models.ListBlobFlatSegmentOptions();
        implOptions.setCancellationToken(options.getCancellationToken());
        implOptions.setMarker(pageId);
        implOptions.setMaxResults(options.getMaxResultsPerPage());
        implOptions.setPrefix(options.getPrefix());
        implOptions.setTimeout(options.getTimeout());
        implOptions.setRequestId(options.getRequestId());
        List<ListBlobsIncludeItem> includeItems = new ArrayList<>();
        BlobListDetails blobListDetails = options.getDetails();
        if (blobListDetails != null) {
            if (blobListDetails.getRetrieveCopy()) {
                includeItems.add(ListBlobsIncludeItem.COPY);
            }
            if (blobListDetails.getRetrieveDeletedBlobs()) {
                includeItems.add(ListBlobsIncludeItem.DELETED);
            }
            if (blobListDetails.getRetrieveMetadata()) {
                includeItems.add(ListBlobsIncludeItem.METADATA);
            }
            if (blobListDetails.getRetrieveSnapshots()) {
                includeItems.add(ListBlobsIncludeItem.SNAPSHOTS);
            }
            if (blobListDetails.getRetrieveUncommittedBlobs()) {
                includeItems.add(ListBlobsIncludeItem.UNCOMMITTEDBLOBS);
            }
        }

        if (includeItems.size() > 0) {
            implOptions.setInclude(includeItems);
        }

        return implOptions;
    }
}
