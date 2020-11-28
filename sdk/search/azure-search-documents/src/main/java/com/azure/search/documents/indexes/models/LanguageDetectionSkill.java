// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.List;

/**
 * A skill that detects the language of input text and reports a single
 * language code for every document submitted on the request. The language code
 * is paired with a score indicating the confidence of the analysis.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Skills.Text.LanguageDetectionSkill")
@Fluent
public final class LanguageDetectionSkill extends SearchIndexerSkill {
    /**
     * Constructor of {@link LanguageDetectionSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     */
    public LanguageDetectionSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }
}
