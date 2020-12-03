// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serde.jackson;

import com.azure.core.serde.JsonFlatten;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.List;
import java.util.Map;

/**
 * Class for testing serialization.
 */
@JsonFlatten
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "$type")
@SerdeTypeName("foo")
@SerdeSubTypes({
        @SerdeSubTypes.Type(name = "foochild", value = FooChild.class)
})
public class Foo {
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
    @SerdeProperty(value = "")
    private Map<String, Object> additionalProperties;

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

    public Map<String, Object> additionalProperties() {
        return additionalProperties;
    }

    public void additionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
