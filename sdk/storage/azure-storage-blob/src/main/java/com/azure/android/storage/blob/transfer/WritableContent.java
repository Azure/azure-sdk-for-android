// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.MainThread;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Package private.
 *
 * A type that describes a content in the device to which data can be written.
 */
final class WritableContent {
    private final Context context;
    private final Uri contentUri;
    private final boolean useContentResolver;
    // Channel to write to the Content if ContentResolver is required to resolve the Content,
    // i.e. when useContentResolver == true
    private WriteToContentChannel contentChannel;

    /**
     * Create WritableContent describing a content in the device on which data can be written.
     *
     * @param context the context
     * @param contentUri the content URI identifying the content
     * @param useContentResolver indicate whether to use {@link android.content.ContentResolver} to resolve
     *                           the content URI
     */
    WritableContent(Context context, Uri contentUri, boolean useContentResolver) {
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
    boolean isUseContentResolver() {
        return this.useContentResolver;
    }

    /**
     * Attempt to take persistable write permission on the content.
     *
     * @throws IllegalStateException if write permission is not granted
     */
    @MainThread
    void takePersistableWritePermission() throws IllegalStateException {
        if (this.useContentResolver) {
            this.context.getContentResolver()
                .takePersistableUriPermission(this.contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            checkPersistableWriteGranted(this.context, this.contentUri);
        }
    }

    /**
     * Open the content for writing.
     *
     * @throws IOException if fails to open underlying content resource in write mode
     * @throws IllegalStateException if write permission to the content is not granted/revoked or
     *     the channel was already opened and disposed
     */
    void openForWrite(Context context) throws IOException, IllegalStateException {
        if (this.useContentResolver) {
            synchronized (this) {
                if (this.contentChannel == null) {
                    this.contentChannel = WriteToContentChannel.create(context, this.contentUri);
                } else if (this.contentChannel.isClosed()) {
                    throw new IllegalStateException("A closed Content Channel cannot be opened.");
                }
            }
        }
    }

    /**
     * Write a block of bytes to the content.
     *
     * @param blockOffset the start offset to write the block to
     * @param block the block of bytes to write
     *
     * @throws IOException the IO error when attempting to write
     * @throws IllegalStateException if write permission is not granted or revoked
     */
    void writeBlock(long blockOffset, byte [] block) throws IOException, IllegalStateException {
        if (this.useContentResolver) {
            if (this.contentChannel == null) {
                throw new IOException("openForWrite(..) must be called before invoking writeBlock(..).");
            }
            // When `useContentResolver` is true then ContentUri must be treated as an opaque handle
            // and the raw-path must not be used.
            // https://commonsware.com/blog/2016/03/15/how-consume-content-uri.html
            // Obtaining a RandomAccessFile requires the raw-path to the file backing the ContentUri.
            //
            // So to write to Content, instead of RandomAccessFile we will use FileChannel, specifically
            // we a shared instance of FileChannel to write the blocks downloaded by concurrent (OkHttp) threads.
            // Operations in FileChannel instance are concurrent safe.
            // https://developer.android.com/reference/java/nio/channels/FileChannel
            //
            // The FileChannel instance is obtained from a FileOutputStream for the ContentUri
            // (see WriteToContentChannel). Only one instance of FileOutputStream can be opened
            // in "write-mode" so we share the FileOutputStream instance and it's backing
            // FileChannel instance.
            // https://docs.oracle.com/javase/7/docs/api/java/io/FileOutputStream.html
            //
            this.contentChannel.writeBlock(blockOffset, block);
        } else {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(this.contentUri.getPath(), "rw")) {
                randomAccessFile.seek(blockOffset);
                randomAccessFile.write(block);
            }
        }
    }

    /**
     * close the content.
     *
     * @throws IOException if close operation fails
     */
    void close() throws IOException {
        if (this.useContentResolver) {
            synchronized (this) {
                if (this.contentChannel != null) {
                    this.contentChannel.close();
                }
            }
        }
    }

    /**
     * Check a persistable write permission is granted on the content.
     *
     * @param context the context to access content resolver
     * @param contentUri the content uri
     * @throws IllegalStateException if permission is not granted
     */
    private static void checkPersistableWriteGranted(Context context, Uri contentUri) throws IllegalStateException {
        final List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        boolean grantedWrite = false;
        for (UriPermission permission : permissions) {
            if (permission.isWritePermission()) {
                grantedWrite = true;
                break;
            }
        }

        if (!grantedWrite) {
            throw new IllegalStateException("Write permission for the content '" + contentUri + "' is not granted or revoked.");
        }
    }

    /**
     * A Channel to write to a Content identified by a ContentUri.
     */
    private static class WriteToContentChannel implements Closeable {
        private final Context context;
        private final Uri contentUri;
        private final ParcelFileDescriptor parcelFileDescriptor;
        private final FileOutputStream fileOutputStream;
        private final FileChannel fileChannel;
        private final AtomicBoolean isClosed = new AtomicBoolean(false);

        /**
         * Creates WriteToContentChannel to write to the Content identified by the given ContentUri.
         *
         * @param context the context to resolve the content uri
         * @param contentUri the uri of the content to write to using this Channel.
         *
         * @throws IOException if fails to open underlying content resource in write mode
         * @throws IllegalStateException if write permission to the content is not granted or revoked
         */
        static WriteToContentChannel create(Context context, Uri contentUri) throws IOException, IllegalStateException {
            WritableContent.checkPersistableWriteGranted(context, contentUri);
            return new WriteToContentChannel(context, contentUri);
        }

        /**
         * Write a block of bytes to the Channel.
         *
         * @param blockOffset the start offset in the content to write the block
         * @param block the block of bytes to write
         * @throws IOException if write fails
         * @throws IllegalStateException if write permission is not granted or revoked
         */
        void writeBlock(long blockOffset, byte [] block) throws IOException, IllegalStateException {
            WritableContent.checkPersistableWriteGranted(this.context, this.contentUri);
            this.fileChannel.write(ByteBuffer.wrap(block), blockOffset);
        }

        /**
         * @return true if the Channel is closed
         */
        boolean isClosed() {
            return this.isClosed.get();
        }

        /**
         * Close the Channel.
         *
         * @throws IOException if close fails
         */
        @Override
        public void close() throws IOException {
            if (this.isClosed.getAndSet(true)) {
                if (this.parcelFileDescriptor != null) {
                    this.parcelFileDescriptor.close();
                }
                if (this.fileOutputStream != null) {
                    this.fileOutputStream.close();
                }
                if (this.fileChannel != null) {
                    this.fileChannel.close();
                }
            }
        }

        private WriteToContentChannel(Context context, Uri contentUri) throws IOException {
            this.context = context;
            this.contentUri = contentUri;
            this.parcelFileDescriptor = context.getContentResolver().openFileDescriptor(this.contentUri, "w");
            if (this.parcelFileDescriptor == null) {
                throw new IOException("FileDescriptor for the content '" + this.contentUri + "' cannot be opened.");
            }
            this.fileOutputStream = new FileOutputStream(this.parcelFileDescriptor.getFileDescriptor());
            this.fileChannel = this.fileOutputStream.getChannel();
        }
    }
}
