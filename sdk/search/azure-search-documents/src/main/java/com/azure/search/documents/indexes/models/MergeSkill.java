// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

import java.util.List;

/**
 * A skill for merging two or more strings into a single unified string, with
 * an optional user-defined delimiter separating each component part.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Skills.Text.MergeSkill")
@Fluent
public final class MergeSkill extends SearchIndexerSkill {
    /*
     * The tag indicates the start of the merged text. By default, the tag is
     * an empty space.
     */
    @SerdeProperty(value = "insertPreTag")
    private String insertPreTag;

    /*
     * The tag indicates the end of the merged text. By default, the tag is an
     * empty space.
     */
    @SerdeProperty(value = "insertPostTag")
    private String insertPostTag;

    /**
     * Constructor of {@link MergeSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     */
    public MergeSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        super(inputs, outputs);
    }

    /**
     * Get the insertPreTag property: The tag indicates the start of the merged
     * text. By default, the tag is an empty space.
     *
     * @return the insertPreTag value.
     */
    public String getInsertPreTag() {
        return this.insertPreTag;
    }

    /**
     * Set the insertPreTag property: The tag indicates the start of the merged
     * text. By default, the tag is an empty space.
     *
     * @param insertPreTag the insertPreTag value to set.
     * @return the MergeSkill object itself.
     */
    public MergeSkill setInsertPreTag(String insertPreTag) {
        this.insertPreTag = insertPreTag;
        return this;
    }

    /**
     * Get the insertPostTag property: The tag indicates the end of the merged
     * text. By default, the tag is an empty space.
     *
     * @return the insertPostTag value.
     */
    public String getInsertPostTag() {
        return this.insertPostTag;
    }

    /**
     * Set the insertPostTag property: The tag indicates the end of the merged
     * text. By default, the tag is an empty space.
     *
     * @param insertPostTag the insertPostTag value to set.
     * @return the MergeSkill object itself.
     */
    public MergeSkill setInsertPostTag(String insertPostTag) {
        this.insertPostTag = insertPostTag;
        return this;
    }
}
