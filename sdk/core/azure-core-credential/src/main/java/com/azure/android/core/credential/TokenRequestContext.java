// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.credential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains details of a request to get a token.
 */
public class TokenRequestContext {
    private final List<String> scopes;

    /**
     * Creates a token request instance.
     */
    public TokenRequestContext() {
        this.scopes = new ArrayList<>();
    }

    /**
     * Gets the scopes required for the token.
     * @return the scopes required for the token
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes required for the token.
     * @param scopes the scopes required for the token
     * @return the TokenRequestContext itself
     * @throws NullPointerException if {@code scopes} is null
     */
    public TokenRequestContext setScopes(List<String> scopes) {
        if (scopes == null) {
            throw new NullPointerException("'scopes' cannot be null.");
        }
        this.scopes.clear();
        this.scopes.addAll(scopes);
        return this;
    }

    /**
     * Adds one or more scopes to the request scopes.
     * @param scopes one or more scopes to add
     * @return the TokenRequestContext itself
     */
    public TokenRequestContext addScopes(String... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }
}
