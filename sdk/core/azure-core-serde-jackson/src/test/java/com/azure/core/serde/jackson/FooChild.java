// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import com.azure.core.serde.JsonFlatten;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Class for testing serialization.
 */
@JsonFlatten
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "$type")
@SerdeTypeName("foochild")
public class FooChild extends Foo {
}
