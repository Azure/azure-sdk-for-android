package com.azure.data.azurecognitivecomputervision.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

public class ComputerVisionErrorException extends HttpResponseException {
    private ComputerVisionError value;

    public ComputerVisionErrorException(String message, HttpResponse response) {
        super(message, response);
    }

    public ComputerVisionError value() {
        return this.value;
    }
}
