package com.azure.data.model

import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Wrapper class around Date that allows us to use custom serialization
 */
class Timestamp(date: Long = 0) : Date(date)