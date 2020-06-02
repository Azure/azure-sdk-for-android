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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Package private.
 *
 * A type that describes a content in the device from which data can be read.
 */
final class ReadableContent {
    private final Context context;
    private final Uri contentUri;
    private final boolean useContentResolver;

    /**
     * Create ReadableContent representing a content in the device from which data can be read.
     *
     * @param context the context
     * @param contentUri the URI identifying the content
     * @param useContentResolver indicates whether to use {@link android.content.ContentResolver} to resolve
     *                           the content URI
     */
    ReadableContent(Context context, Uri contentUri, boolean useContentResolver) {
        this.context = context;
        this.contentUri = contentUri;
        this.useContentResolver = useContentResolver;
    }

    /**
     * Get the {@link Uri} to the content.
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
    boolean isUsingContentResolver() {
        return this.useContentResolver;
    }

    /**
     * Attempt to take persistable read permission on the content.
     *
     * @throws IllegalStateException if read permission is not granted
     */
    @MainThread
    void takePersistableReadPermission() throws IllegalStateException {
        if (this.useContentResolver) {
            this.context.getContentResolver()
                .takePersistableUriPermission(this.contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            checkPersistableReadGranted();
        }
    }

    /**
     * Get the total size of the content in bytes.
     *
     * @return the content size in bytes
     * @throws FileNotFoundException if the content does not exists
     * @throws UnsupportedOperationException if content length is unknown
     * @throws IOException if there is a failure when closing the content opened to fetch the length
     */
    long getLength() throws IOException, UnsupportedOperationException {
        if (this.useContentResolver) {
            final long contentLength;
            // Note: openAssetFileDescriptor throws FileNotFoundException if content not exists.
            try (AssetFileDescriptor descriptor
                     = this.context.getContentResolver().openAssetFileDescriptor(this.contentUri, "r")) {
                contentLength = descriptor.getLength();
            }
            if (contentLength == AssetFileDescriptor.UNKNOWN_LENGTH) {
                throw new UnsupportedOperationException("The size of the content '" + contentUri + "' is unknown.");
            }
            return contentLength;
        } else {
            final File file = new File(contentUri.getPath());
            if (!file.exists() || !file.isFile()) {
                throw new FileNotFoundException("File resource does not exist: " + contentUri.getPath());
            }
            return file.length();
        }
    }

    /**
     * Read a block of bytes from the content.
     *
     * @param blockOffset the start offset of the block
     * @param blockSize the size of the block
     * @return an array of bytes taken from the content in the range [blockOffset, blockOffset + blockSize]
     * @throws FileNotFoundException if the content does not exists
     * @throws IOException the IO error when attempting to read
     * @throws IllegalStateException if read permission is not granted or revoked
     */
    byte[] readBlock(int blockOffset, int blockSize) throws IOException, IllegalStateException {
        if (this.useContentResolver) {
            this.checkPersistableReadGranted();
            // ContentResolver::openFileDescriptor works but we use openAssetFileDescriptor
            // so that providers that return subsections of a file are supported.
            // The "r" (read) mode is used so that the content providers that don't support write can also be consumed.
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
    private void checkPersistableReadGranted() throws IllegalStateException {
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
                throw new IllegalStateException("Read permission for the content '" + contentUri + "' is not granted or revoked.");
            }
        }
    }

    /**
     * Seek the stream read cursor to the given position.
     *
     * @param stream the stream
     * @param seekTo the stream position to seek
     * @throws IOException if seek fails
     */
    private static void seek(FileInputStream stream, long seekTo) throws IOException {
        int totalBytesSkipped = 0;
        while(totalBytesSkipped < seekTo) {
            final long bytesSkipped = stream.skip(seekTo - totalBytesSkipped);
            if (bytesSkipped < 0) {
                throw new IOException("FileInputStream::seek returned negative value.");
            }
            if (bytesSkipped == 0) {
                // 0 can be returned from Stream::skip if EOF reached OR unable to skip at the moment.
                // Read one byte to see it's due to EOF.
                if (stream.read() == -1) {
                    // EOF hence return.
                    return;
                } else {
                    // not EOF but stream::read returned a byte.
                    totalBytesSkipped++;
                }
            } else {
                totalBytesSkipped += bytesSkipped;
            }
        }
    }

    /**
     * Read the stream content into a buffer starting from the stream's read cursor position.
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
