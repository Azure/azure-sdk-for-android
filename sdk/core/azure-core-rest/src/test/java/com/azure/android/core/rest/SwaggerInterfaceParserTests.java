// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.rest.annotation.ExpectedResponses;
import com.azure.android.core.rest.annotation.Get;
import com.azure.android.core.rest.annotation.Host;
import com.azure.android.core.rest.annotation.ServiceInterface;
import com.azure.core.logging.ClientLogger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SwaggerInterfaceParserTests {
    private final ClientLogger logger = new ClientLogger(SwaggerInterfaceParserTests.class);

    interface TestInterface1 {
        String testMethod1();
    }

    @Host("https://management.azure.com")
    interface TestInterface2 {
    }

    @Host("https://management.azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface3 {
    }

    @Test
    public void hostWithNoHostAnnotation() {
        Exception ex = null;
        try {
            new SwaggerInterfaceParser(TestInterface1.class, null);
        } catch (RuntimeException e) {
            ex = e;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex.getMessage().contains("Host annotation must be defined on the interface"));
    }

    @Test
    public void hostWithNoServiceNameAnnotation() {
        Exception ex = null;
        try {
            new SwaggerInterfaceParser(TestInterface2.class, null);
        } catch (RuntimeException e) {
            ex = e;
        }
        Assertions.assertNotNull(ex);
        Assertions.assertTrue(ex.getMessage().contains("ServiceInterface annotation must be defined on the interface"));
    }

    @Test
    public void hostWithHostAnnotation() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface3.class, null);
        assertEquals("https://management.azure.com", interfaceParser.getHost());
        assertEquals("myService", interfaceParser.getServiceName());
    }

    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface4 {
        @Get("my/url/path")
        @ExpectedResponses({200})
        void testMethod4(Callback<Response> callback);
    }

    @Test
    public void getMethodParser() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface4.class, null);
        final Method testMethod3 = TestInterface4.class.getDeclaredMethods()[0];
        assertEquals("testMethod4", testMethod3.getName());

        final SwaggerMethodParser methodParser0 = interfaceParser.getMethodParser(testMethod3, this.logger);
        assertNotNull(methodParser0);
        assertEquals("com.azure.android.core.rest.SwaggerInterfaceParserTests$TestInterface4.testMethod4", methodParser0.getMethodFullName());

        final SwaggerMethodParser methodParser1 = interfaceParser.getMethodParser(testMethod3, this.logger);
        assertSame(methodParser0, methodParser1);
    }
}
