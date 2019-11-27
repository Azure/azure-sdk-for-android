package com.azure.android.storage.common.interceptor;

import androidx.annotation.NonNull;

import com.azure.android.core.util.function.BiConsumer;
import com.azure.android.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ResponseValidationInterceptor implements Interceptor {
    private final ClientLogger logger = new ClientLogger(ResponseValidationInterceptor.class);
    private final Iterable<BiConsumer<Response, ClientLogger>> assertions;

    /**
     * Creates an interceptor that executes each provided assertion on responses.
     *
     * @param assertions The assertions to apply.
     */
    ResponseValidationInterceptor(Iterable<BiConsumer<Response, ClientLogger>> assertions) {
        Collection<BiConsumer<Response, ClientLogger>> assertionsCopy = new ArrayList<>();

        for (BiConsumer<Response, ClientLogger> assertion : assertions) {
            assertionsCopy.add(assertion);
        }

        this.assertions = assertionsCopy;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        for (BiConsumer<Response, ClientLogger> assertion : assertions) {
            assertion.accept(response, logger);
        }

        return response;
    }
}
