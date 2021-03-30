// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

final class UrlEscapers {
    private static final String UNRESERVED_SYMBOLS = "-._~";
    private static final String SUB_DELIMS = "!$&'()*+,;=";
    static final PercentEscaper PATH_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS + SUB_DELIMS + ":@", false);
    static final PercentEscaper QUERY_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS + "/?", false);
    static final PercentEscaper FORM_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS, true);
}
