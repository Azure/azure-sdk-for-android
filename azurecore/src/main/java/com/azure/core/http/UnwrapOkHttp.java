package com.azure.core.http;

import okhttp3.Request;
import okhttp3.Response;

// TODO:anuchan private interface but explore other designs without this.
interface UnwrapOkHttp {
    interface InnerRequest {
        Request unwrap();
    }

    interface InnerResponse {
        Response unwrap();
    }
}
