// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.rest.annotation.Host;
import com.azure.android.core.rest.annotation.ServiceInterface;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.serde.jackson.JacksonSerder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The type responsible for creating individual Swagger interface method parsers from a Swagger
 * interface.
 */
final class SwaggerInterfaceParser {
    private final String host;
    private final String serviceName;
    private final JacksonSerder serdeAdapter;
    private static final Object METHOD_PARSERS_LOCK = new Object();
    private static final Map<Method, SwaggerMethodParser> METHOD_PARSERS = new HashMap<>();

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface
     * name.
     * @param swaggerInterface The interface that will be parsed.
     * @param serdeAdapter The serializer that will be used to serialize non-String header values and query values.
     */
    SwaggerInterfaceParser(Class<?> swaggerInterface, JacksonSerder serdeAdapter) {
        this(swaggerInterface, serdeAdapter, null);
    }

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface
     * name.
     * @param swaggerInterface The interface that will be parsed.
     * @param serdeAdapter The serializer that will be used to serialize non-String header values and query values.
     * @param host The host of URLs that this Swagger interface targets.
     */
    SwaggerInterfaceParser(Class<?> swaggerInterface, JacksonSerder serdeAdapter, String host) {
        this.serdeAdapter = serdeAdapter;

        if (host != null && host.length() != 0) {
            this.host = host;
        } else {
            final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
            if (hostAnnotation != null && !hostAnnotation.value().isEmpty()) {
                this.host = hostAnnotation.value();
            } else {
                throw new RuntimeException("Host annotation must be defined on the interface "
                    + swaggerInterface.getName());
            }
        }

        ServiceInterface serviceAnnotation = swaggerInterface.getAnnotation(ServiceInterface.class);
        if (serviceAnnotation != null && !serviceAnnotation.name().isEmpty()) {
            serviceName = serviceAnnotation.name();
        } else {
            throw new RuntimeException("ServiceInterface annotation must be defined on the interface "
                + swaggerInterface.getName());
        }
    }

    /**
     * Get the method parser that is associated with the provided swaggerMethod. The method parser
     * can be used to get details about the Swagger REST API call.
     *
     * @param swaggerMethod the method to generate a parser for
     * @param logger the logger
     * @return the SwaggerMethodParser associated with the provided swaggerMethod
     */
    SwaggerMethodParser getMethodParser(Method swaggerMethod, ClientLogger logger) {
        synchronized (METHOD_PARSERS_LOCK) {
            SwaggerMethodParser methodParser = METHOD_PARSERS.get(swaggerMethod);
            if (methodParser == null) {
                methodParser = new SwaggerMethodParser(this.host, swaggerMethod, this.serdeAdapter, logger);
                METHOD_PARSERS.put(swaggerMethod, methodParser);
            }
            return methodParser;
        }
    }

    /**
     * Get the desired host that the provided Swagger interface will target with its REST API
     * calls. This value is retrieved from the @Host annotation placed on the Swagger interface.
     * @return The value of the @Host annotation.
     */
    String getHost() {
        return host;
    }

    String getServiceName() {
        return serviceName;
    }
}
