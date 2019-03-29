package com.azure.core.util

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

inline fun <reified A : Annotation> getAnnotatedPropertyValues(target: Any) : List<String> {

    return target.javaClass.kotlin.memberProperties
            .filter { it.annotations.any { annotation -> annotation is A } }
            .map { prop -> prop.get(target).toString() }
}

inline fun <reified A : Annotation> getAnnotatedProperties(target: Any) : List<KProperty1<Any, *>> {

    return target.javaClass.kotlin.memberProperties
            .filter { it.annotations.any { annotation -> annotation is A } }
}