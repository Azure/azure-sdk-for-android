// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeXmlProperty;

import java.util.Arrays;

public class Slide {
    @SerdeXmlProperty(localName = "type", isAttribute = true)
    private String type;

    @SerdeProperty("title")
    private String title;

    @SerdeProperty("item")
    private String[] items;

    /**
     * Gets the type of slide.
     *
     * @return The type of slide.
     */
    public String type() {
        return type;
    }

    /**
     * Gets the slide title.
     *
     * @return The title of the slide.
     */
    public String title() {
        return title;
    }

    /**
     * Gets the content strings of the slide.
     *
     * @return The content strings of the slide.
     */
    public String[] items() {
        if (items == null) {
            return new String[0];
        }
        return Arrays.copyOf(items, items.length);
    }
}
