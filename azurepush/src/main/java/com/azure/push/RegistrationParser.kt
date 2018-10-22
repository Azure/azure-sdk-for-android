package com.azure.push

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class RegistrationParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: String): List<Registration> {
        val stream = StringReader(input)
        stream.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it)
            parser.nextTag()
            return readRegistrations(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRegistrations(parser: XmlPullParser): List<Registration> {
        val registrations = mutableListOf<Registration>()

        parser.require(XmlPullParser.START_TAG, null, "feed")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "AppleRegistrationDescription" || parser.name == "AppleTemplateRegistrationDescription") {
                registrations.add(readRegistration(parser))
            } else {
                skip(parser)
            }
        }

        return registrations
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRegistration(parser: XmlPullParser): Registration {
        var id = ""
        var etag = ""
        var deviceToken = ""
        var expiresAt = Date()
        var tags = listOf<String>()
        var templateName = ""
        var templateBody = ""
        var templateExpiry = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "RegistrationId" -> id = readTextContent(parser.name, parser)
                "ETag" -> etag = readTextContent(parser.name, parser)
                "DeviceToken" -> deviceToken = readTextContent(parser.name, parser)
                "Tags" -> tags = readTextContent(parser.name, parser).split(",")
                "TemplateName" -> templateName = readTextContent(parser.name, parser)
                "BodyTemplate" -> templateBody = readTextContent(parser.name, parser)
                "Expiry" -> templateExpiry = readTextContent(parser.name, parser)
                "ExpirationTime" -> {
                    val format = SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSSZ", Locale.US)
                    expiresAt = format.parse(readTextContent(parser.name, parser))
                }
            }
        }

        return Registration(id, etag, deviceToken, expiresAt, tags, Registration.Template(templateName, templateBody, templateExpiry))
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTextContent(tag: String, parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, tag)
        val result = parser.nextText()
        parser.require(XmlPullParser.END_TAG, null, tag)
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth -= 1
                XmlPullParser.START_TAG -> depth += 1
            }
        }
    }
}