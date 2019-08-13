package com.azure.core.implementation.http;

import okhttp3.Request;
import okhttp3.Response;

// TODO:anuchan impl interface but explore other designs without this.
public interface UnwrapOkHttp {
    interface InnerRequest {
        Request unwrap();
    }

    interface InnerResponse {
        Response unwrap();
    }
}
