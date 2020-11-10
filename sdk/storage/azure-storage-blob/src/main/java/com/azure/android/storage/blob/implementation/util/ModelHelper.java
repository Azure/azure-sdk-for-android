// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.implementation.util;

import androidx.annotation.Nullable;

import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobTag;
import com.azure.android.storage.blob.models.BlobTags;

import java.util.HashMap;
import java.util.Map;

public class ModelHelper {

    /**
     * Transforms {@link BlobTags} to a public map of tags.
     *
     * @param generatedTags {@link BlobTags}
     * @return The map of tags.
     */
    public static Map<String, String> populateBlobTags(BlobTags generatedTags) {
        Map<String, String> tags = null;
        if (generatedTags.getBlobTagSet() != null) {
            tags = new HashMap<>();
            for (BlobTag tag : generatedTags.getBlobTagSet()) {
                tags.put(tag.getKey(), tag.getValue());
            }
        }
        return tags;
    }

    /**
     * Validates only certain request conditions properties can be set.
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @param matchConditions Whether or not match conditions can be set.
     * @param modifiedConditions Whether or not modified conditions can be set.
     * @param leaseId Whether or not lease id can be set.
     * @param tagsConditions Whether or not tags conditions can be set.
     */
    public static void validateRequestConditions(@Nullable BlobRequestConditions requestConditions,
                                                 boolean matchConditions, boolean modifiedConditions, boolean leaseId,
                                                 boolean tagsConditions) {
        if (requestConditions == null) {
            return;
        }
        if (!matchConditions && (requestConditions.getIfMatch() != null
            || requestConditions.getIfNoneMatch() != null)) {
            throw new UnsupportedOperationException("Match conditions are not supported for this API.");
        }
        if (!modifiedConditions && (requestConditions.getIfModifiedSince() != null
            || requestConditions.getIfUnmodifiedSince() != null)) {
            throw new UnsupportedOperationException("Modified conditions are not supported for this API.");
        }
        if (!leaseId && requestConditions.getLeaseId() != null) {
            throw new UnsupportedOperationException("Lease id condition is not supported for this API.");
        }
        if (!tagsConditions && requestConditions.getTagsConditions() != null) {
            throw new UnsupportedOperationException("Tags conditions are not supported for this API.");
        }
    }
}
