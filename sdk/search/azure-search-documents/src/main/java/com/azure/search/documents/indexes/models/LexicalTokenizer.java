// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Base type for tokenizers.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = LexicalTokenizer.class)
@SerdeTypeName("LexicalTokenizer")
@SerdeSubTypes({
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.ClassicTokenizer", value = ClassicTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.EdgeNGramTokenizer", value = EdgeNGramTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.KeywordTokenizer", value = KeywordTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.KeywordTokenizerV2", value = KeywordTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.MicrosoftLanguageTokenizer",
        value = MicrosoftLanguageTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.MicrosoftLanguageStemmingTokenizer",
        value = MicrosoftLanguageStemmingTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.NGramTokenizer", value = NGramTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.PathHierarchyTokenizerV2",
        value = PathHierarchyTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.PatternTokenizer", value = PatternTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.StandardTokenizer", value = LuceneStandardTokenizer.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.UaxUrlEmailTokenizer", value = UaxUrlEmailTokenizer.class)
})
@Fluent
public abstract class LexicalTokenizer {
    /*
     * The name of the tokenizer. It must only contain letters, digits, spaces,
     * dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @SerdeProperty(value = "name")
    private String name;

    /**
     * Constructor of {@link LexicalTokenizer}.
     *
     * @param name The name of the token filter. It must only contain letters, digits,
     * spaces, dashes or underscores, can only start and end with alphanumeric
     * characters, and is limited to 128 characters.
     */
    @SerdeToPojo
    public LexicalTokenizer(@SerdeProperty(value = "name") String name) {
        this.name = name;
    }

    /**
     * Get the name property: The name of the tokenizer. It must only contain
     * letters, digits, spaces, dashes or underscores, can only start and end
     * with alphanumeric characters, and is limited to 128 characters.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

}
