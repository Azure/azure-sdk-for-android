package com.azure.push

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

import java.util.Date

/** A `Registration` associates a specific device with a set of tags and possibly a template.
 * The device will receive all push notifications targeting any or all of the tags associated and
 * if a template is provided, the push notifications contents will be in the format specified by
 * the template.
 */
class Registration(val id: String, val etag: String, val deviceToken: String, val expiresAt: Date, val tags: List<String>, val template: Template?) {

    /** A `Template` enables a client to specify the exact format of the notifications it wants to receive.
     * https://docs.microsoft.com/en-us/previous-versions/azure/azure-services/dn530748(v%3dazure.100)
     */
    class Template(val name: String, val body: String, val expiry: String)

    internal var name: String = template?.name ?: Registration.defaultName

    companion object {
        internal const val defaultName = "\$Default"

        internal fun payload(deviceToken: String, tags: List<String>): String {
            val tagsNode = if (tags.isEmpty())  "" else "<Tags>${tags.joinToString(",")}</Tags>"
            return "<entry xmlns=\"http://www.w3.org/2005/Atom\"><content type=\"text/xml\"><AppleRegistrationDescription xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">$tagsNode<DeviceToken>$deviceToken</DeviceToken></AppleRegistrationDescription></content></entry>"
        }

        internal fun payload(deviceToken: String, template: Template, priority: String? = null, tags: List<String>): String {
            val expiryNode = if (template.expiry.isEmpty()) "" else "<Expiry>${template.expiry}</Expiry>"
            val priorityNode = if (priority.isNullOrEmpty()) "" else "<Priority>$priority</Priority>"
            val tagsNode = if (tags.isEmpty())  "" else "<Tags>${tags.joinToString(",")}</Tags>"
            return "<entry xmlns=\"http://www.w3.org/2005/Atom\"><content type=\"text/xml\"><AppleTemplateRegistrationDescription xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">$tagsNode}<DeviceToken>$deviceToken</DeviceToken><BodyTemplate><![CDATA[${template.body}]]></BodyTemplate>$expiryNode$priorityNode<TemplateName>${template.name}</TemplateName></AppleTemplateRegistrationDescription></content></entry>"
        }

        internal fun validateTemplateName(name: String): AzurePushError? {
            if (name == defaultName) {
                return AzurePushError.reservedTemplateName
            }

            if (name.contains(":")) {
                return AzurePushError.invalidTemplateName
            }

            return null
        }
    }
}