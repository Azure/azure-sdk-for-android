package com.azure.push

import android.content.Context
import com.azure.data.service.Response

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class AzurePush {

    companion object {

        //region

        /**
         * Configures AzurePush to work with an Azure Notification Hub.
         *
         * @param context the Android context.
         * @param notificationHubName the name of the Azure notification hub.
         * @param connectionString the `DefaultListenSharedAccess` connection string of the notification hub.
         * @exception  AzurePushError  if the connection string is invalid or malformed.
         */
        @Throws(AzurePushError::class)
        fun configure(context: Context, notificationHubName: String, connectionString: String) {
            NotificationClient.shared.configure(context, notificationHubName, connectionString)
        }

        //endregion

        //region

        /**
         * Registers the current device to receive native push notifications from the Azure Notification Hub.
         *
         * @param deviceToken a globally unique token that identifies this device.
         * @param tags An optional list of tags (a tag is any string of up to 120 characters).
         *             Some notifications from the notification hub can target a specific set of tags. For such a push
         *             notification, if one of tags provided by this method during the registration is included
         *             in the set of tags the push notification targets, the current device will receive the push notification.
         *             See https://docs.microsoft.com/en-us/previous-versions/azure/azure-services/dn530749(v=azure.100)
         * @param completion a closure called after the registration is completed. The `Response` parameter in the closure informs whether
         *                   the registration was successful or not.
         */
        fun registerForRemoteNotifications(deviceToken: String, tags: List<String> = listOf(), completion: (Response<Registration>) -> Unit) {
            NotificationClient.shared.registerForRemoteNotifications(deviceToken, tags, completion)
        }

        /**
         * Registers the current device to receive native push notifications from the Azure Notification Hub.
         *
         * @param deviceToken a globally unique token that identifies this device.
         * @param tags An optional list of tags (a tag is any string of up to 120 characters).
         *             Some notifications from the notification hub can target a specific set of tags. For such a push
         *             notification, if one of tags provided by this method during the registration is included
         *             in the set of tags the push notification targets, the current device will receive the push notification.
         *             See https://docs.microsoft.com/en-us/previous-versions/azure/azure-services/dn530749(v=azure.100)
         * @param template a template used to specify the exact format of the notifications this device can receive.
         * @param completion a closure called after the registration is completed. The `Response` parameter in the closure informs whether
         *                   the registration was successful or not.
         */
        fun registerForRemoteNotifications(deviceToken: String, template: Registration.Template, priority: String? = null, tags: List<String> = listOf(), completion: (Response<Registration>) -> Unit) {
            NotificationClient.shared.registerFormRemoteNotifications(deviceToken, template, priority, tags, completion)
        }

        //endregion

        //region

        /** Cancels the registration. Any push notification sent by the notification hub,
         *  matching this registration, will no longer be received by this device.
         */
        fun cancelRegistration(registration: Registration, completion: (Response<String>) -> Unit) {
            NotificationClient.shared.cancelRegistration(registration, completion)
        }

        /** Cancels all the registrations made with the specified device token. The device will no longer receive
         *  push notifications sent through the Azure Notification Hub.
         */
        fun cancelAllRegistrations(deviceToken: String, completion: (Response<String>) -> Unit) {
            NotificationClient.shared.cancelAllRegistrations(deviceToken, completion)
        }

        //endregion
    }
}