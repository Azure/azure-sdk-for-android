// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.List;

/**
 * Text analytics positive-negative sentiment analysis, scored as a floating
 * point value in a range of zero to 1.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Skills.Text.SentimentSkill")
@Fluent
public final class SentimentSkill extends SearchIndexerSkill {
    /*
     * A value indicating which language code to use. Default is en. Possible
     * values include: 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'it', 'no',
     * 'pl', 'pt-PT', 'ru', 'es', 'sv', 'tr'
     */
    @SerdeProperty(value = "defaultLanguageCode")
    private SentimentSkillLanguage defaultLanguageCode;

    /**
     * Constructor of {@link SearchIndexerSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     */
    public SentimentSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }

    /**
     * Get the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'da', 'nl', 'en',
     * 'fi', 'fr', 'de', 'el', 'it', 'no', 'pl', 'pt-PT', 'ru', 'es', 'sv',
     * 'tr'.
     *
     * @return the defaultLanguageCode value.
     */
    public SentimentSkillLanguage getDefaultLanguageCode() {
        return this.defaultLanguageCode;
    }

    /**
     * Set the defaultLanguageCode property: A value indicating which language
     * code to use. Default is en. Possible values include: 'da', 'nl', 'en',
     * 'fi', 'fr', 'de', 'el', 'it', 'no', 'pl', 'pt-PT', 'ru', 'es', 'sv',
     * 'tr'.
     *
     * @param defaultLanguageCode the defaultLanguageCode value to set.
     * @return the SentimentSkill object itself.
     */
    public SentimentSkill setDefaultLanguageCode(SentimentSkillLanguage defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
        return this;
    }
}
