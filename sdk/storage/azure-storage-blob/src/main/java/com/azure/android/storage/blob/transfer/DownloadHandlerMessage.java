// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import android.os.Bundle;
import android.os.Message;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Package private.
 * <p>
 * A factory to create handler messages to be delivered to a {@link DownloadHandler} which will react appropriately.
 * The handler uses these messages to communicate and react to various stages of a blob download.
 */
final class DownloadHandlerMessage {
    private static final String MESSAGE_TYPE_KEY = "mtk";
    private static final String PROCESSED_BLOCK_ID_KEY = "pbik";

    /**
     * Create a message advising handler to perform blob download initialization.
     *
     * @param handler The handler that handles this message.
     * @return The message.
     */
    static Message createInitMessage(DownloadHandler handler) {
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();

        bundle.putInt(MESSAGE_TYPE_KEY, Type.INIT);
        msg.setData(bundle);

        return msg;
    }

    /**
     * Create a message describing a successful block download.
     *
     * @param msgTarget The handler that handles this message.
     * @param blockId The base64 ID that identifies the block.
     * @return The message.
     */
    static Message createBlockDownloadCompletedMessage(DownloadHandler msgTarget, String blockId) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();

        bundle.putInt(MESSAGE_TYPE_KEY, Type.DOWNLOAD_COMPLETED);
        bundle.putString(PROCESSED_BLOCK_ID_KEY, blockId);
        msg.setData(bundle);

        return msg;
    }

    /**
     * Create a message describing a failed block download.
     *
     * @param msgTarget The handler that handles this message.
     * @param blockId The base64 ID that identifies the block.
     * @return The message.
     */
    static Message createBlockDownloadFailedMessage(DownloadHandler msgTarget, String blockId) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();

        bundle.putInt(MESSAGE_TYPE_KEY, Type.DOWNLOAD_FAILED);
        bundle.putString(PROCESSED_BLOCK_ID_KEY, blockId);
        msg.setData(bundle);

        return msg;
    }

    /**
     * Create a message describing a successful blob download.
     *
     * @param msgTarget The handler that handles this message.
     * @return The message.
     */
    static Message createBlobDownloadCompletedMessage(DownloadHandler msgTarget) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();

        bundle.putInt(MESSAGE_TYPE_KEY, Type.DOWNLOAD_COMPLETED);
        msg.setData(bundle);

        return msg;
    }

    /**
     * Create a message describing a failed blob download.
     *
     * @param msgTarget The handler that handles this message.
     * @return The message.
     */
    static Message createBlobDownloadFailedMessage(DownloadHandler msgTarget) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();

        bundle.putInt(MESSAGE_TYPE_KEY, Type.DOWNLOAD_FAILED);
        msg.setData(bundle);

        return msg;
    }

    /**
     * Create a message indicating handler to park, stop and exit.
     *
     * @param msgTarget The handler that handles this message.
     * @return The message.
     */
    static Message createStopMessage(DownloadHandler msgTarget) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();

        bundle.putInt(MESSAGE_TYPE_KEY, Type.STOP);
        msg.setData(bundle);

        return msg;
    }

    /**
     * Get the value of the message {@link Type} field in the message.
     *
     * @param message The message.
     * @return The message type.
     */
    @Type
    static int getMessageType(Message message) {
        Bundle data = message.getData();

        return data.getInt(MESSAGE_TYPE_KEY);
    }

    /**
     * Get the base64 block ID from the message.
     *
     * @param message The message.
     * @return The block ID.
     */
    static String getBlockIdFromMessage(Message message) {
        Bundle data = message.getData();

        return data.getString(PROCESSED_BLOCK_ID_KEY);
    }

    /**
     * Describes values of message type field in Handler Message instances.
     */
    @IntDef({
        Type.INIT,
        Type.DOWNLOAD_COMPLETED,
        Type.DOWNLOAD_FAILED,
        Type.STOP
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int INIT = 0;
        int DOWNLOAD_COMPLETED = 1;
        int DOWNLOAD_FAILED = 2;
        int STOP = 3;
    }
}
