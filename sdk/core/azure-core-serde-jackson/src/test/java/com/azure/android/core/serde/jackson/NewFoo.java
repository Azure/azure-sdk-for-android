// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.serde.jackson;

import com.azure.android.core.serde.JsonFlatten;
import com.azure.android.core.serde.SerdeGetKeyValues;
import com.azure.android.core.serde.SerdeIgnoreProperty;
import com.azure.android.core.serde.SerdeProperty;
import com.azure.android.core.serde.SerdeSetKeyValues;
import com.azure.android.core.serde.SerdeSubTypes;
import com.azure.android.core.serde.SerdeTypeInfo;
import com.azure.android.core.serde.SerdeTypeName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for testing serialization.
 */
@JsonFlatten
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "$type")
@SerdeTypeName("newfoo")
@SerdeSubTypes({
        @SerdeSubTypes.Type(name = "newfoochild", value = NewFooChild.class)
})
public class NewFoo {
    @SerdeProperty(value = "properties.bar")
    private String bar;
    @SerdeProperty(value = "properties.props.baz")
    private List<String>  baz;
    @SerdeProperty(value = "properties.props.q.qux")
    private Map<String, String> qux;
    @SerdeProperty(value = "properties.more\\.props")
    private String moreProps;
    @SerdeProperty(value = "props.empty")
    private Integer empty;
    @SerdeIgnoreProperty
    private Map<String, Object> additionalProperties;
    @SerdeProperty(value = "additionalProperties")
    private Map<String, Object> additionalPropertiesProperty;

    public String bar() {
        return bar;
    }

    public void bar(String bar) {
        this.bar = bar;
    }

    public List<String> baz() {
        return baz;
    }

    public void baz(List<String> baz) {
        this.baz = baz;
    }

    public Map<String, String> qux() {
        return qux;
    }

    public void qux(Map<String, String> qux) {
        this.qux = qux;
    }

    public String moreProps() {
        return moreProps;
    }

    public void moreProps(String moreProps) {
        this.moreProps = moreProps;
    }

    public Integer empty() {
        return empty;
    }

    public void empty(Integer empty) {
        this.empty = empty;
    }

    @SerdeSetKeyValues
    private void additionalProperties(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        additionalProperties.put(key.replace("\\.", "."), value);
    }

    @SerdeGetKeyValues
    public Map<String, Object> additionalProperties() {
        return additionalProperties;
    }

    public void additionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Map<String, Object> additionalPropertiesProperty() {
        return additionalPropertiesProperty;
    }

    public void additionalPropertiesProperty(Map<String, Object> additionalPropertiesProperty) {
        this.additionalPropertiesProperty = additionalPropertiesProperty;
    }
}
