// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.micro.util.CancellationToken;
import com.azure.android.core.rest.annotation.ExpectedResponses;
import com.azure.android.core.rest.annotation.Get;
import com.azure.android.core.rest.annotation.Host;
import com.azure.android.core.rest.annotation.ServiceInterface;
import com.azure.core.logging.ClientLogger;
import com.azure.core.serde.jackson.JacksonSerderAdapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class SwaggerMethodParserTests {
    private final ClientLogger logger = new ClientLogger(SwaggerMethodParserTests.class);

    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface InvalidCallbackParamMethods {
        @Get("my/url/path0")
        @ExpectedResponses({200})
        void missingCallbackParam(Integer param0);

        @Get("my/url/path1")
        @ExpectedResponses({200})
        void callbackMissingTypeArg(Integer param0, Callback callback);
    }

    @Test
    public void invalidCallbackParam() throws NoSuchMethodException {
        Class<InvalidCallbackParamMethods> clazz = InvalidCallbackParamMethods.class;
        Method missingCallbackParamMethod = clazz.getDeclaredMethod("missingCallbackParam", Integer.class);

        IllegalStateException ex0 = null;
        try {
            new SwaggerMethodParser("https://raw.host.com",
                missingCallbackParamMethod,
                new JacksonSerderAdapter(),
                this.logger);
        } catch (IllegalStateException e) {
            ex0 = e;
        }
        Assertions.assertNotNull(ex0);
        Assertions.assertTrue(ex0.getMessage().contains("must have a com.azure.android.core.rest.Callback parameter, it must be the last parameter and parameterized"));

        Method callbackMissingTypeArgMethod = clazz.getDeclaredMethod("callbackMissingTypeArg", Integer.class,
            Callback.class);

        IllegalStateException ex1 = null;
        try {
            new SwaggerMethodParser("https://raw.host.com",
                callbackMissingTypeArgMethod,
                new JacksonSerderAdapter(),
                this.logger);
        } catch (IllegalStateException e) {
            ex1 = e;
        }
        Assertions.assertNotNull(ex1);
        Assertions.assertTrue(ex1.getMessage().contains("must have a com.azure.android.core.rest.Callback parameter, it must be the last parameter and parameterized"));
    }


    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface CancellationTokenParamMethods {
        @Get("my/url/path0")
        @ExpectedResponses({200})
        void cancellationTokenCorrectIndex(Integer param0,
                                           CancellationToken cancellationToken,
                                           Callback<Response<Void>> callback);

        @Get("my/url/path1")
        @ExpectedResponses({200})
        void cancellationTokenIncorrectIndex(CancellationToken cancellationToken,
                                             Integer param0,
                                             Callback<Response<Void>> callback);
    }

    @Test
    public void cancellationTokenIndex() throws NoSuchMethodException {
        Class<CancellationTokenParamMethods> clazz = CancellationTokenParamMethods.class;

        Method cancellationTokenCorrectIndexMethod = clazz.getDeclaredMethod("cancellationTokenCorrectIndex",
            Integer.class, CancellationToken.class, Callback.class);

        SwaggerMethodParser methodParser0 = new SwaggerMethodParser("https://raw.host.com",
            cancellationTokenCorrectIndexMethod,
            new JacksonSerderAdapter(),
            this.logger);

        Assertions.assertEquals(1, methodParser0.cancellationTokenArgIndex);

        Method cancellationTokenIncorrectIndexMethod = clazz.getDeclaredMethod("cancellationTokenIncorrectIndex",
            CancellationToken.class, Integer.class, Callback.class);

        SwaggerMethodParser methodParser1 = new SwaggerMethodParser("https://raw.host.com",
            cancellationTokenIncorrectIndexMethod,
            new JacksonSerderAdapter(),
            this.logger);

        // CancellationToken when present must be second last arg, its the contract, presence of
        // it in any other index (like in this case) is ignored.
        Assertions.assertEquals(-1,  methodParser1.cancellationTokenArgIndex);
    }
}
