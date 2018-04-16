package com.azure.core.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class DateUtil {

    enum class Format(val formatString: String) {

        Rfc1123Format( "EEE, dd MMM yyyy HH:mm:ss")
    }

    companion object {

        fun getDateFromatter(format: Format) : SimpleDateFormat {

            val dateFormatter = SimpleDateFormat(format.formatString, Locale.ROOT)
            dateFormatter.timeZone = TimeZone.getTimeZone("GMT")

            return dateFormatter
        }
    }
}