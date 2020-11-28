// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.List;

/**
 * A skill for reshaping the outputs. It creates a complex type to support
 * composite fields (also known as multipart fields).
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Skills.Util.ShaperSkill")
@Fluent
public final class ShaperSkill extends SearchIndexerSkill {
    /**
     * Constructor of {@link ShaperSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     */
    public ShaperSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }
}
