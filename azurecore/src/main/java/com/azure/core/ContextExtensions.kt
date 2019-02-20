import android.content.Context
import android.os.Build
import com.azure.core.BuildConfig
import com.azure.core.http.HttpHeaderValue
import okhttp3.Headers
import java.util.*
import android.content.Context.UI_MODE_SERVICE
import android.app.UiModeManager
import android.content.res.Configuration
import com.azure.core.http.HttpHeader

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@Suppress("DEPRECATION")
fun Context.getCurrentLocale() : Locale {

    //this.assets.locales   //app/system locale list based on resources/assets

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.resources.configuration.locales[0]
    } else {
        /*
        This field was deprecated in API level 24. Do not set or read this directly.
        Use getLocales() and setLocales(LocaleList). If only the primary locale is needed,
        getLocales().get(0) is now the preferred accessor
        */
        this.resources.configuration.locale
    }
}

fun Context.getDefaultHeaders() : Headers {

    val builder = Headers.Builder()

    // set the accept encoding header
    // Accept-Encoding HTTP Header; see https://tools.ietf.org/html/rfc7230#section-4.2.3
    builder.add(HttpHeader.AcceptEncoding.value, HttpHeaderValue.AcceptEncoding)

    val currentLocale = this.getCurrentLocale()

    //set the accepted locales/languages
//        val mappedLocales = .take(6).mapIndexed { index, locale ->
//            val rank = 1.0 - (index * 0.1)
//            "${locale};q=$rank"
//        }.joinToString()

    // Accept-Language HTTP Header; see https://tools.ietf.org/html/rfc7231#section-5.3.5
    builder.add(HttpHeader.AcceptLanguage.value, currentLocale.language)

    // User-Agent Header; see https://tools.ietf.org/html/rfc7231#section-5.5.3
    // Example: `iOS Example/1.1.0 (com.azure.data; build:23; iOS 10.0.0) AzureData/2.0.0`

    builder.add(HttpHeader.UserAgent.value, this.getUserAgentString())

    return builder.build()
}

fun Context.getUserAgentString() : String {

    // User-Agent Header; see https://tools.ietf.org/html/rfc7231#section-5.5.3
    // Example: `iOS Example/1.1.0 (com.azure.data; build:23; iOS 10.0.0) AzureData/2.0.0`

    try {
        val pkgManager = this.packageManager
        val pkgName = this.packageName
        val pInfo = pkgManager.getPackageInfo(pkgName, 0)

        val appName = pInfo?.applicationInfo?.loadLabel(pkgManager) ?: "Unknown"
        val appVersion = pInfo?.versionName ?: "Unknown"
        val appVersionCode = pInfo?.versionCode ?: "Unknown"
        var os = "Android"

        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager

        when {
            uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION -> os += " TV"
            uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_WATCH -> os += " Wear"
            uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_CAR -> os += " Auto"
            uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_APPLIANCE -> os += " IOT"
            uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_VR_HEADSET -> os += " VR"
        }

        val osDetails = "$os ${android.os.Build.VERSION.RELEASE}"
        val azureDataVersion = "AzureData/${BuildConfig.VERSION_NAME}"

        return "$appName/$appVersion ($pkgName; build:$appVersionCode; $osDetails) $azureDataVersion"
    } catch (e: Exception) {
        return "AzureMobile.Data"
    }
}