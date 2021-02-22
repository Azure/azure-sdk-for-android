// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.serde.SerdeProperty;
import com.azure.android.core.serde.SerdeToPojo;
import com.azure.android.core.serde.SerdeXmlProperty;
import com.azure.android.core.serde.SerdeXmlRootElement;

import java.util.List;

@SerdeXmlRootElement(localName = "SignedIdentifiers")
public class SignedIdentifiersWrapper {
    @SerdeXmlProperty(localName = "SignedIdentifier")
    private final List<SignedIdentifierInner> signedIdentifiers;

    /**
     * Creates a wrapper for {@code signedIdentifiers}.
     *
     * @param signedIdentifiers Identifiers to wrap.
     */
    @SerdeToPojo
    public SignedIdentifiersWrapper(@SerdeProperty("signedIdentifiers") List<SignedIdentifierInner> signedIdentifiers) {
        this.signedIdentifiers = signedIdentifiers;
    }

    /**
     * Get the SignedIdentifiers value.
     *
     * @return the SignedIdentifiers value
     */
    public List<SignedIdentifierInner> signedIdentifiers() {
        return signedIdentifiers;
    }
}
