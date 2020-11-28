// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.customserde.json.jackson;

import com.azure.core.customserde.JsonSerializerProvider;
import com.azure.core.customserde.MemberNameConverterProvider;

/**
 * Implementation of {@link JsonSerializerProvider} and {@link MemberNameConverterProvider}.
 */
public final class JacksonJsonSerializerProvider implements JsonSerializerProvider, MemberNameConverterProvider {
    @Override
    public JacksonJsonSerializer createInstance() {
        return new JacksonJsonSerializerBuilder().build();
    }
}
