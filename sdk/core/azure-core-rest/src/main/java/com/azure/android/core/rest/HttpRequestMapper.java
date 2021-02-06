// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.util.UrlBuilder;
import com.azure.android.core.rest.annotation.BodyParam;
import com.azure.android.core.rest.annotation.Delete;
import com.azure.android.core.rest.annotation.FormParam;
import com.azure.android.core.rest.annotation.Get;
import com.azure.android.core.rest.annotation.Head;
import com.azure.android.core.rest.annotation.HeaderParam;
import com.azure.android.core.rest.annotation.Headers;
import com.azure.android.core.rest.annotation.HostParam;
import com.azure.android.core.rest.annotation.Patch;
import com.azure.android.core.rest.annotation.PathParam;
import com.azure.android.core.rest.annotation.Post;
import com.azure.android.core.rest.annotation.Put;
import com.azure.android.core.rest.annotation.QueryParam;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.serde.SerdeAdapter;
import com.azure.android.core.serde.SerdeEncoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class HttpRequestMapper {
    private static final Pattern PATTERN_COLON_SLASH_SLASH = Pattern.compile("://");

    private final String rawHost;
    private final SerdeAdapter serdeAdapter;
    private final HttpMethod httpMethod;
    private final String relativePath;
    private final List<MethodParameterMapping> hostMappings = new ArrayList<>();
    private final List<MethodParameterMapping> pathMappings = new ArrayList<>();
    private final List<MethodParameterMapping> queryMappings = new ArrayList<>();
    private final List<MethodParameterMapping> headerMappings = new ArrayList<>();
    private final List<MethodParameterMapping> formDataEntriesMapping = new ArrayList<>();
    private final Integer contentArgIndex;
    private final String contentType;
    private final HttpHeaders headers = new HttpHeaders();

    HttpRequestMapper(String rawHost, Method swaggerMethod, SerdeAdapter serdeAdapter) {
        this.rawHost = rawHost;
        this.serdeAdapter = serdeAdapter;

        if (swaggerMethod.isAnnotationPresent(Get.class)) {
            this.httpMethod = HttpMethod.GET;
            this.relativePath = swaggerMethod.getAnnotation(Get.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Put.class)) {
            this.httpMethod = HttpMethod.PUT;
            this.relativePath = swaggerMethod.getAnnotation(Put.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Head.class)) {
            this.httpMethod = HttpMethod.HEAD;
            this.relativePath = swaggerMethod.getAnnotation(Head.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Delete.class)) {
            this.httpMethod = HttpMethod.DELETE;
            this.relativePath = swaggerMethod.getAnnotation(Delete.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Post.class)) {
            this.httpMethod = HttpMethod.POST;
            this.relativePath = swaggerMethod.getAnnotation(Post.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Patch.class)) {
            this.httpMethod = HttpMethod.PATCH;
            this.relativePath = swaggerMethod.getAnnotation(Patch.class).value();
        } else {
            final String errorMessage = "Either Get, Put, Head, Delete, Post or Patch "
                + "annotation must be defined on the method "
                + swaggerMethod.getDeclaringClass().getName() + "." + swaggerMethod.getName() + "().";
            throw new RuntimeException(errorMessage);
        }

        if (swaggerMethod.isAnnotationPresent(Headers.class)) {
            final Headers headersAnnotation = swaggerMethod.getAnnotation(Headers.class);
            final String[] headers = headersAnnotation.value();
            for (final String header : headers) {
                final int colonIndex = header.indexOf(":");
                if (colonIndex >= 0) {
                    final String headerName = header.substring(0, colonIndex).trim();
                    if (!headerName.isEmpty()) {
                        final String headerValue = header.substring(colonIndex + 1).trim();
                        if (!headerValue.isEmpty()) {
                            this.headers.put(headerName, headerValue);
                        }
                    }
                }
            }
        }

        Integer contentArgIndex = null;
        String contentType = null;
        final Annotation[][] allParametersAnnotations = swaggerMethod.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < allParametersAnnotations.length; parameterIndex++) {
            final Annotation[] parameterAnnotations = allParametersAnnotations[parameterIndex];
            for (final Annotation annotation : parameterAnnotations) {
                final Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.equals(HostParam.class)) {
                    final HostParam hostAnnotation = (HostParam) annotation;
                    this.hostMappings.add(new MethodParameterMapping(parameterIndex, hostAnnotation.value(),
                        !hostAnnotation.encoded()));
                } else if (annotationType.equals(PathParam.class)) {
                    final PathParam pathAnnotation = (PathParam) annotation;
                    this.pathMappings.add(new MethodParameterMapping(parameterIndex, pathAnnotation.value(),
                        !pathAnnotation.encoded()));
                } else if (annotationType.equals(QueryParam.class)) {
                    final QueryParam queryAnnotation = (QueryParam) annotation;
                    this.queryMappings.add(new MethodParameterMapping(parameterIndex, queryAnnotation.value(),
                        !queryAnnotation.encoded()));
                } else if (annotationType.equals(HeaderParam.class)) {
                    final HeaderParam headerAnnotation = (HeaderParam) annotation;
                    this.headerMappings.add(new MethodParameterMapping(parameterIndex, headerAnnotation.value(),
                        false));
                } else if (annotationType.equals(FormParam.class)) {
                    final FormParam formAnnotation = (FormParam) annotation;
                    this.formDataEntriesMapping.add(new MethodParameterMapping(parameterIndex, formAnnotation.value(),
                        !formAnnotation.encoded()));
                } else if (annotationType.equals(BodyParam.class)) {
                    contentArgIndex = parameterIndex;
                    contentType = ((BodyParam) annotation).value();
                }
            }
        }

        if (!this.formDataEntriesMapping.isEmpty() && contentArgIndex != null) {
            throw new RuntimeException("'FormParam' and 'BodyParam' are mutually exclusive, but the method "
                + swaggerMethod.getDeclaringClass().getName() + "." + swaggerMethod.getName() + "() "
                + "has both the annotations.");
        }

        this.contentArgIndex = contentArgIndex;
        this.contentType = contentType;
    }

    HttpRequest map(Object[] swaggerMethodArgs) throws IOException {
        final String path = this.applyPathMappings(swaggerMethodArgs);
        UrlBuilder urlBuilder = UrlBuilder.parse(path);

        // Sometimes a full URL will be provided as the value of PathParam annotated argument.
        // This mainly happens in paging scenarios, in such cases, we use the full URL
        // (a simple scheme presence check to determine full URL) and ignore the Host annotation.
        if (urlBuilder.getScheme() == null) {
            urlBuilder = this.applySchemeAndHostMapping(swaggerMethodArgs, new UrlBuilder());
            // Set the path after host, concatenating the path segment in the host.
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                String hostPath = urlBuilder.getPath();
                if (hostPath == null || hostPath.isEmpty() || "/".equals(hostPath) || path.contains("://")) {
                    urlBuilder.setPath(path);
                } else {
                    urlBuilder.setPath(hostPath + "/" + path);
                }
            }
        }

        this.applyQueryMappings(swaggerMethodArgs, urlBuilder);

        final HttpRequest request = new HttpRequest(this.httpMethod, urlBuilder.toString());

        if (!this.formDataEntriesMapping.isEmpty()) {
            final String formData = this.applyFormDataMapping(swaggerMethodArgs);
            if (formData == null) {
                request.getHeaders().put("Content-Length", "0");
            } else {
                request.getHeaders().put("Content-Type", "application/x-www-form-urlencoded");
                request.setBody(formData);
            }
        } else {
            final Object content = this.retrieveContentArg(swaggerMethodArgs);
            if (content == null) {
                request.getHeaders().put("Content-Length", "0");
            } else {
                String contentType = this.contentType;
                if (contentType == null || contentType.isEmpty()) {
                    if (content instanceof byte[] || content instanceof String) {
                        contentType = "application/octet-stream";
                    } else {
                        contentType = "application/json";
                    }
                }

                request.getHeaders().put("Content-Type", contentType);
                boolean isJson = false;
                final String[] contentTypeParts = contentType.split(";");
                for (final String contentTypePart : contentTypeParts) {
                    if (contentTypePart.trim().equalsIgnoreCase("application/json")) {
                        isJson = true;
                        break;
                    }
                }

                if (isJson) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    this.serdeAdapter.serialize(content, SerdeEncoding.JSON, stream);
                    request.setHeader("Content-Length", String.valueOf(stream.size()));
                    request.setBody(stream.toByteArray());
                } else if (content instanceof byte[]) {
                    request.setBody((byte[]) content);
                } else if (content instanceof String) {
                    final String contentString = (String) content;
                    request.setBody(contentString);
                } else {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    this.serdeAdapter.serialize(content,
                        SerdeEncoding.fromHeaders(request.getHeaders().toMap()),
                        stream);
                    request.setHeader("Content-Length", String.valueOf(stream.size()));
                    request.setBody(stream.toByteArray());
                }
            }
        }

        // Headers from Swagger method arguments always take precedence over inferred headers from body types.
        HttpHeaders httpHeaders = request.getHeaders();
        this.applyHeaderMappings(swaggerMethodArgs, httpHeaders);

        return request;
    }

    HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    UrlBuilder applySchemeAndHostMapping(Object[] swaggerMethodArgs, UrlBuilder urlBuilder) {
        final String substitutedHost = this.applyUrlMapping(this.rawHost, this.hostMappings, swaggerMethodArgs);
        final String[] substitutedHostParts = PATTERN_COLON_SLASH_SLASH.split(substitutedHost);

        if (substitutedHostParts.length >= 2) {
            urlBuilder.setScheme(substitutedHostParts[0]);
            urlBuilder.setHost(substitutedHostParts[1]);
        } else if (substitutedHostParts.length == 1) {
            urlBuilder.setScheme(substitutedHostParts[0]);
            urlBuilder.setHost(substitutedHost);
        } else {
            urlBuilder.setHost(substitutedHost);
        }
        return urlBuilder;
    }

    String applyPathMappings(Object[] swaggerMethodArgs) {
        return this.applyUrlMapping(this.relativePath, this.pathMappings, swaggerMethodArgs);
    }

    UrlBuilder applyQueryMappings(Object[] swaggerMethodArgs, UrlBuilder urlBuilder) {
        if (swaggerMethodArgs != null) {
            for (MethodParameterMapping queryParameterMapping : this.queryMappings) {
                if (queryParameterMapping.argIndex < swaggerMethodArgs.length) {
                    final Object methodArg = swaggerMethodArgs[queryParameterMapping.argIndex];
                    String parameterValue = this.serialize(methodArg);
                    if (parameterValue != null) {
                        if (queryParameterMapping.shouldEncode) {
                            parameterValue = UrlEscapers.QUERY_ESCAPER.escape(parameterValue);
                        }
                        urlBuilder.setQueryParameter(queryParameterMapping.mapToName, parameterValue);
                    }
                }
            }
        }
        return urlBuilder;
    }

    HttpHeaders applyHeaderMappings(Object[] swaggerMethodArgs, HttpHeaders httpHeaders) {
        for (HttpHeader header : this.headers) {
            httpHeaders.put(header.getName(), header.getValue());
        }
        if (swaggerMethodArgs != null) {
            for (MethodParameterMapping headerParameterMapping : this.headerMappings) {
                if (headerParameterMapping.argIndex < swaggerMethodArgs.length) {
                    final Object methodArg = swaggerMethodArgs[headerParameterMapping.argIndex];
                    if (methodArg instanceof Map) {
                        @SuppressWarnings("unchecked")
                        final Map<String, ?> headerCollection = (Map<String, ?>) methodArg;
                        final String headerCollectionPrefix = headerParameterMapping.mapToName;
                        for (final Map.Entry<String, ?> headerCollectionEntry : headerCollection.entrySet()) {
                            final String headerName = headerCollectionPrefix + headerCollectionEntry.getKey();
                            final String headerValue = this.serialize(headerCollectionEntry.getValue());
                            if (headerValue != null) {
                                httpHeaders.put(headerName, headerValue);
                            }
                        }
                    } else {
                        final String headerValue = this.serialize(methodArg);
                        if (headerValue != null) {
                            httpHeaders.put(headerParameterMapping.mapToName, headerValue);
                        }
                    }
                }
            }
        }
        return httpHeaders;
    }

    String applyFormDataMapping(Object[] swaggerMethodArgs) {
        if (swaggerMethodArgs == null || this.formDataEntriesMapping.isEmpty()) {
            return null;
        }
        StringBuilder formDataBuilder = new StringBuilder();
        for (MethodParameterMapping formParameterMapping : this.formDataEntriesMapping) {
            final String formDataEntry = this.serializeFormDataEntry(formParameterMapping.mapToName,
                swaggerMethodArgs[formParameterMapping.argIndex],
                formParameterMapping.shouldEncode);
            if (formDataEntry != null) {
                formDataBuilder.append(formDataEntry);
                formDataBuilder.append("&");
            }
        }
        if (formDataBuilder.length() > 0) {
            formDataBuilder.deleteCharAt(formDataBuilder.length() - 1);
        }
        final String formData = formDataBuilder.toString();
        if (!formData.isEmpty()) {
            return formData;
        } else {
            return null;
        }
    }

    Object retrieveContentArg(Object[] swaggerMethodArgs) {
        if (swaggerMethodArgs == null
            || this.contentArgIndex == null
            || this.contentArgIndex >= swaggerMethodArgs.length) {
            return null;
        } else {
            return swaggerMethodArgs[this.contentArgIndex];
        }
    }

    String serializeFormDataEntry(String entryKey, Object entryValue, boolean shouldEncode) {
        if (entryValue == null) {
            return null;
        }

        final String encodedEntryKey = UrlEscapers.FORM_ESCAPER.escape(entryKey);
        if (entryValue instanceof List<?>) {
            StringBuilder formDataEntryBuilder = new StringBuilder();
            final List<?> listElements = (List<?>) entryValue;
            for (Object element : listElements) {
                if (element != null) {
                    String serializedElement = this.serdeAdapter.serializeRaw(element);
                    if (serializedElement != null) {
                        if (shouldEncode) {
                            formDataEntryBuilder.append(encodedEntryKey + "="
                                + UrlEscapers.FORM_ESCAPER.escape(serializedElement));
                        } else {
                            formDataEntryBuilder.append(encodedEntryKey + "=" + serializedElement);
                        }
                        formDataEntryBuilder.append("&");
                    }
                }
            }
            if (formDataEntryBuilder.length() > 0) {
                formDataEntryBuilder.deleteCharAt(formDataEntryBuilder.length() - 1);
            }
            final String formData = formDataEntryBuilder.toString();
            if (!formData.isEmpty()) {
                return formData;
            } else {
                return null;
            }
        } else {
            final String serializedValue = this.serdeAdapter.serializeRaw(entryValue);
            if (serializedValue != null) {
                if (shouldEncode) {
                    return encodedEntryKey + "=" + UrlEscapers.FORM_ESCAPER.escape(serializedValue);
                } else {
                    return encodedEntryKey + "=" + serializedValue;
                }
            } else {
                return null;
            }
        }
    }

    String applyUrlMapping(String urlTemplate,
                           Iterable<MethodParameterMapping> urlParameterMappings,
                           Object[] swaggerMethodArgs) {
        String mappedUrl = urlTemplate;
        if (swaggerMethodArgs != null) {
            for (MethodParameterMapping urlParameterMapping : urlParameterMappings) {
                if (urlParameterMapping.argIndex < swaggerMethodArgs.length) {
                    final Object methodArg = swaggerMethodArgs[urlParameterMapping.argIndex];

                    String value = this.serialize(methodArg);
                    if (value != null && !value.isEmpty() && urlParameterMapping.shouldEncode) {
                        value = UrlEscapers.PATH_ESCAPER.escape(value);
                    }
                    // if a parameter is null, we treat it as empty string. This is assuming
                    // no {...} will be allowed otherwise in a path template
                    if (value == null) {
                        value = "";
                    }
                    mappedUrl = mappedUrl.replace("{" + urlParameterMapping.mapToName + "}", value);
                }
            }
        }
        return mappedUrl;
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return this.serdeAdapter.serializeRaw(value);
        }
    }

    /**
     * Describes mapping of a swagger interface method parameter.
     * <p>
     * When the user invokes the method with values for each parameter, the mapping describes how to place
     * those values in the corresponding HTTP message. Whether the value should be encoded or not,
     * what is the query or header name to which the value is mapped to, the place holder name
     * in the URL, e.g. "http://{host}.com/{fileName}.html", if the value belongs to host or path segment.
     * </p>
     */
    private static final class MethodParameterMapping {
        final int argIndex;
        final String mapToName;
        final boolean shouldEncode;

        /**
         * For a swagger interface method, creates a mapping for the parameter at position {@code index}.
         *
         * @param argIndex The index of the parameter in the swagger interface method.
         * @param mapToName identifies where in the HTTP message to map the actual value for the parameter
         *     at {@code index}. If the parameter represents a query or header value then {@code mapToName} is
         *     the query or header name, if the parameter represents host or path segment value then
         *     {@code mapToName} is the corresponding place holder identifier in the URL.
         * @param shouldEncode Indicate while mapping should the actual value for the parameter at {@code index}
         *     needs to be encoded or not.
         */
        MethodParameterMapping(int argIndex, String mapToName, boolean shouldEncode) {
            this.argIndex = argIndex;
            this.mapToName = mapToName;
            this.shouldEncode = shouldEncode;
        }
    }
}
