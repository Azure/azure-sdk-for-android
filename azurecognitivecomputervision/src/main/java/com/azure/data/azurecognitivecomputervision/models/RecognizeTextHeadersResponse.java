package com.azure.data.azurecognitivecomputervision.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

public class RecognizeTextHeadersResponse extends ResponseBase<RecognizeTextHeaders, Void> {
    public RecognizeTextHeadersResponse(HttpRequest request, int statusCode, HttpHeaders headers, Void value, RecognizeTextHeaders deserializedHeaders) {
        super(request, statusCode, headers, value, deserializedHeaders);
    }
}
