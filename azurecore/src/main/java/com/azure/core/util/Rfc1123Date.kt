package com.azure.core.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private val RFC1123Formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)

/**
 * Return a Date? from an Rfc1123 formatted string
 */
fun dateFromRfc1123(from : String) : Date? {

    return try {
        RFC1123Formatter.parse(from)
    } catch (ex : ParseException){
        null
    }
}