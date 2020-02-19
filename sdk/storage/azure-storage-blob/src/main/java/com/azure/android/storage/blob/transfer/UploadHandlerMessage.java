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
 *
 * A factory to create handler messages, those when delivered to a {@link UploadHandler},
 * the handler react appropriately. The handler uses these messages to communicate and
 * react to various stages of a file upload.
 */
final class UploadHandlerMessage {
    private static final String MESSAGE_TYPE_KEY = "mtk";
    private static final String PROCESSED_BLOCK_ID_KEY = "pbik";

    /**
     * Create a message advising handler to perform file upload initialization.
     *
     * @param handler the handler that handles this message
     * @return the message
     */
    static Message createInitMessage(UploadHandler handler) {
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt(MESSAGE_TYPE_KEY, Type.INIT);
        msg.setData(bundle);
        return msg;
    }

    /**
     * Create a message describing a successful block upload.
     *
     * @param msgTarget the handler that handles this message
     * @param blockId the base64 id that identifies the block
     * @return the message
     */
    static Message createStagingCompletedMessage(UploadHandler msgTarget, String blockId) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt(MESSAGE_TYPE_KEY, Type.STAGING_COMPLETED);
        bundle.putString(PROCESSED_BLOCK_ID_KEY, blockId);
        msg.setData(bundle);
        return msg;
    }

    /**
     * Create a message describing a failed block upload.
     *
     * @param msgTarget the handler that handles this message
     * @param blockId the base64 id that identifies the block
     * @return the message
     */
    static Message createStagingFailedMessage(UploadHandler msgTarget, String blockId) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt(MESSAGE_TYPE_KEY, Type.STAGING_FAILED);
        bundle.putString(PROCESSED_BLOCK_ID_KEY, blockId);
        msg.setData(bundle);
        return msg;
    }

    /**
     * Create a message describing a successful blocks commit.
     *
     * @param msgTarget the handler that handles this message
     * @return the message
     */
    static Message createCommitCompletedMessage(UploadHandler msgTarget) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt(MESSAGE_TYPE_KEY, Type.COMMIT_COMPLETED);
        msg.setData(bundle);
        return msg;
    }

    /**
     * Create a message describing a failed blocks commit.
     *
     * @param msgTarget the handler that handles this message
     * @return the message
     */
    static Message createCommitFailedMessage(UploadHandler msgTarget) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt(MESSAGE_TYPE_KEY, Type.COMMIT_FAILED);
        msg.setData(bundle);
        return msg;
    }

    /**
     * Create a message indicating handler to park, stop and exit.
     *
     * @param msgTarget the handler that handles this message
     * @return the message
     */
    static Message createStopMessage(UploadHandler msgTarget) {
        Message msg = msgTarget.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt(MESSAGE_TYPE_KEY, Type.STOP);
        msg.setData(bundle);
        return msg;
    }

    /**
     * Get the value of the message {@link Type} field in the message.
     *
     * @param message the message
     * @return the message type
     */
    @Type
    static int getMessageType(Message message) {
        Bundle data = message.getData();
        return data.getInt(MESSAGE_TYPE_KEY);
    }

    /**
     * Get the base64 block id from the message
     *
     * @param message the message
     * @return the block id
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
        Type.STAGING_COMPLETED,
        Type.STAGING_FAILED,
        Type.COMMIT_COMPLETED,
        Type.COMMIT_FAILED,
        Type.STOP
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int INIT = 0;
        int STAGING_COMPLETED = 1;
        int STAGING_FAILED = 2;
        int COMMIT_COMPLETED = 3;
        int COMMIT_FAILED = 4;
        int STOP = 5;
    }
}
