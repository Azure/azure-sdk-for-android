// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import androidx.annotation.MainThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Package private.
 *
 * A type that describes and enables operations on a content identifiable by a content URI.
 * The actual content is the data that needs to be transferred to Azure Blob Storage.
 */
final class ContentDescription {
    private final Context context;
    private final Uri contentUri;
    private final boolean useContentResolver;

    /**
     * Create ContentDescription representing a content in the device identified a given content URI.
     *
     * @param context the context
     * @param contentUri the content URI identifying the content
     * @param useContentResolver indicate whether to use {@link android.content.ContentResolver} to resolve
     *                           the content URI
     */
    ContentDescription(Context context, String contentUri, boolean useContentResolver) {
        this(context, Uri.parse(contentUri), useContentResolver);
    }

    /**
     * Create ContentDescription representing a content in the device identified a given content URI.
     *
     * @param context the context
     * @param contentUri the content URI identifying the content
     * @param useContentResolver indicate whether to use {@link android.content.ContentResolver} to resolve
     *                           the content URI
     */
    ContentDescription(Context context, Uri contentUri, boolean useContentResolver) {
        this.context = context;
        this.contentUri = contentUri;
        this.useContentResolver = useContentResolver;
    }

    /**
     * Get the {@link Uri} to the content to upload.
     *
     * @return the content URI
     */
    Uri getUri() {
        return this.contentUri;
    }

    /**
     * Check whether to use {@link android.content.ContentResolver} to resolve the content URI.
     *
     * @return true if resolving content URI requires content resolver.
     */
    boolean isUseContentResolver() {
        return this.useContentResolver;
    }

    /**
     * Attempt to take persistable read permission on the content.
     *
     * @throws Throwable if granting of read permission failed.
     */
    @MainThread
    void takePersistableReadPermission() throws Throwable {
        if (this.useContentResolver) {
            this.context.getContentResolver()
                .takePersistableUriPermission(contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            checkPersistableReadGranted();
        }
    }

    /**
     * Get the total size of the content in bytes.
     *
     * @return the content size in bytes
     * @throws Throwable if retrieval of content length fails
     */
    long getLength() throws Throwable {
        final long contentLength;
        if (this.useContentResolver) {
            try (AssetFileDescriptor descriptor
                     = this.context.getContentResolver().openAssetFileDescriptor(contentUri, "r")) {
                contentLength = descriptor.getLength();
            }
        } else {
            File file = new File(contentUri.getPath());
            contentLength = file.length();
        }
        if (contentLength == -1) {
            throw new Throwable("Unable to get size of the content '" + contentUri + "'.");
        }
        return contentLength;
    }

    /**
     * Get a block of bytes from the content.
     *
     * @param blockOffset the start offset of the block
     * @param blockSize the size of the block
     * @return block of bytes in the range [blockOffset, blockOffset + blockSize]
     * @throws Throwable if reading of block fails
     */
    byte [] getBlock(int blockOffset, int blockSize) throws Throwable {
        if (this.useContentResolver) {
            this.checkPersistableReadGranted();
            try (AssetFileDescriptor descriptor
                     = this.context.getContentResolver().openAssetFileDescriptor(this.contentUri, "r")) {
                try (FileInputStream fileInputStream = descriptor.createInputStream()) {
                    seek(fileInputStream, blockOffset);
                    byte [] blockContent = new byte[blockSize];
                    read(fileInputStream, blockContent);
                    return blockContent;
                }
            }
        } else {
            File file = new File(this.contentUri.getPath());
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                seek(fileInputStream, blockOffset);
                byte [] blockContent = new byte[blockSize];
                read(fileInputStream, blockContent);
                return blockContent;
            }
        }
    }

    /**
     * Check a persistable read permission is granted on the content.
     *
     * @throws Throwable if permission is not granted
     */
    private void checkPersistableReadGranted() throws Throwable {
        if (this.useContentResolver) {
            final List<UriPermission> permissions = this.context.getContentResolver().getPersistedUriPermissions();
            boolean grantedRead = false;
            for (UriPermission permission : permissions) {
                if (permission.isReadPermission()) {
                    grantedRead = true;
                    break;
                }
            }
            if (!grantedRead) {
                throw new Throwable("Read permission for the content '" + contentUri + "' is not granted or revoked.");
            }
        }
    }

    /**
     * Seek the stream read cursor to the given position.
     *
     * @param stream the stream
     * @param seekTo the seek position
     * @throws IOException if seek fails
     */
    private static void seek(FileInputStream stream, long seekTo) throws IOException {
        int skipped = 0;
        while(skipped < seekTo) {
            long m = stream.skip(seekTo - skipped);
            if (m < 0) {
                throw new IOException("FileInputStream::seek returns negative value.");
            }
            if (m == 0) {
                if (stream.read() == -1) {
                    return;
                } else {
                    skipped++;
                }
            } else {
                skipped += m;
            }
        }
    }

    /**
     * Read the stream content into a buffer starting from stream's read cursor position.
     *
     * @param stream the file stream
     * @param buffer the output buffer
     * @return the number of bytes read
     * @throws IOException if read fails
     */
    private static int read(FileInputStream stream, byte [] buffer) throws IOException {
        int bytesToRead = buffer.length;
        int bytesRead = 0;
        while (bytesRead < bytesToRead) {
            int m = stream.read(buffer, bytesRead, bytesToRead - bytesRead);
            if (m == -1) {
                break;
            }
            bytesRead += m;
        }
        return bytesRead;
    }
}
