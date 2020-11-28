// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.List;

/**
 * A skill that enables scenarios that require a Boolean operation to determine
 * the data to assign to an output.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Skills.Util.ConditionalSkill")
@Fluent
public final class ConditionalSkill extends SearchIndexerSkill {
    /**
     * Constructor of {@link SearchIndexerSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     */
    public ConditionalSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }
}
