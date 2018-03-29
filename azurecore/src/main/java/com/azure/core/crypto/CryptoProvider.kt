package com.azure.core.crypto

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
class CryptoProvider {

    companion object {

        fun hmacEncrypt(data: String, key: String, algorithm: HmacAlgorithm = HmacAlgorithm.SHA256): String? {
            try {
                val decodedKey = org.apache.commons.codec.binary.Base64.decodeBase64(key.toByteArray())

                val hmac = Mac.getInstance(algorithm.value)
                val keySpec = SecretKeySpec(decodedKey, algorithm.value)
                hmac.init(keySpec)

                val hashPayLoad = hmac.doFinal(data.toByteArray(charset("UTF-8")))

                return Base64.encodeToString(hashPayLoad, Base64.DEFAULT).replace("\n", "")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }
}