// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.implementation.util;

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
}
