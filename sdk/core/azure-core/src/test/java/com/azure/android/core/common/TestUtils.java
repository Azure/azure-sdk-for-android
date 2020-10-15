// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.core.http.ServiceClient;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.credential.SasTokenCredential;
import com.azure.android.storage.blob.interceptor.SasTokenCredentialInterceptor;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;

import org.threeten.bp.OffsetDateTime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertFalse;

public final class TestUtils {
    private TestUtils() {
        // Empty constructor to prevent instantiation of this class.
    }

    // ----------------------- CONSTANT VALUES ----------------------------
    /*
    The values below are used to create data-driven tests for access conditions.
     */
    static final OffsetDateTime oldDate = OffsetDateTime.now().minusDays(1);

    static final OffsetDateTime newDate = OffsetDateTime.now().plusDays(1);

    /*
    Note that this value is only used to check if we are depending on the received etag. This value will not actually
    be used.
     */

    static final String receivedEtag = "received";

    static final String garbageEtag = "garbage";


    // -------------------- GENERATING CLIENTS --------------------------
    public static StorageBlobClient.Builder initializeDefaultSyncBlobClientBuilder(Interceptor ... interceptors) {
        return initializeDefaultSyncBlobClientBuilder(enableFiddler(), interceptors);
    }

    public static StorageBlobClient.Builder initializeDefaultSyncBlobClientBuilder(boolean enableFiddler,
                                                                                   Interceptor ... interceptors) {
        return new StorageBlobClient.Builder(getServiceBuilderWithOptionalProxy(enableFiddler, interceptors))
            .setBlobServiceUrl(getDefaultEndpointString(useHttps()))
            .setCredentialInterceptor(new SasTokenCredentialInterceptor(new SasTokenCredential(getDefaultSasToken())));
    }

    public static StorageBlobAsyncClient.Builder initializeDefaultAsyncBlobClientBuilder(Interceptor ... interceptors) {
        return initializeDefaultAsyncBlobClientBuilder(enableFiddler(), interceptors);
    }

    public static StorageBlobAsyncClient.Builder initializeDefaultAsyncBlobClientBuilder(boolean enableFiddler,
                                                                                         Interceptor ... interceptors) {
        return new StorageBlobAsyncClient.Builder(UUID.randomUUID().toString(),
            getServiceBuilderWithOptionalProxy(enableFiddler, interceptors))
            .setBlobServiceUrl(getDefaultEndpointString(useHttps()))
            .setCredentialInterceptor(new SasTokenCredentialInterceptor(new SasTokenCredential(getDefaultSasToken())));
    }

    public static String getDefaultEndpointString(boolean useHttps) {
        String protocol = useHttps ? "https" : "http";
        return String.format("%s://%s.blob.core.windows.net", protocol, getDefaultAccountName());
    }

    public static String getDefaultAccountName() {
        return System.getenv("AZURE_STORAGE_ANDROID_ACCOUNT_NAME");
    }

    public static String getDefaultSasToken() {
        return System.getenv("AZURE_STORAGE_ANDROID_SAS_TOKEN");
    }

    public static ServiceClient.Builder getServiceBuilderWithOptionalProxy(boolean enableFiddler,
                                                                           Interceptor ... interceptors) {
        ServiceClient.Builder serviceBuilder = enableFiddler ?
            new ServiceClient.Builder(new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))) :
            new ServiceClient.Builder();

        for (Interceptor interceptor : interceptors) {
            serviceBuilder.addInterceptor(interceptor);
        }

       return serviceBuilder;
    }

    // ---------------------- CONFIG OPTIONS -----------------------------

    public static boolean useHttps() {
        return false;
    }

    public static boolean enableFiddler() {
        return true;
    }

    // --------------------- GENERATING TEST RESOURCES ------------------------

    public static String generateResourceName() {
        return UUID.randomUUID().toString();
    }

    public static String generateBlockID() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    public static OkHttpClient buildOkHttpClientWithInterceptor(Interceptor interceptor) {
        return new OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .build();
    }

    // -------------------------- HELPER METHODS -----------------------

    public static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(method + " didn't produce any result.", true);
        }
    }

    public static boolean validateBasicHeaders(Headers headers) {
        return headers.get("etag") != null &&
            // Quotes should be scrubbed from etag header values
            !headers.get("etag").contains("\"") &&
            headers.get("last-modified") != null &&
            headers.get("x-ms-request-id") != null &&
            headers.get("x-ms-version") != null &&
            headers.get("date") != null;
    }

    public static boolean validateBlobProperties(BlobGetPropertiesHeaders headers, String cacheControl, String contentDisposition, String contentEncoding, String contentLanguage, byte[] contentMd5, String contentType) {
        return Objects.equals(headers.getCacheControl(), cacheControl) &&
            Objects.equals(headers.getContentDisposition(), contentDisposition) &&
            Objects.equals(headers.getContentEncoding(), contentEncoding) &&
            Objects.equals(headers.getContentLanguage(), contentLanguage) &&
            Arrays.equals(headers.getContentMD5(), contentMd5) &&
            Objects.equals(headers.getContentType(), contentType);
    }

    public static String setupMatchCondition(StorageBlobClient client, String containerName, String blobName, String match) {
        if (receivedEtag.equals(match)) {
            return client.getBlobProperties(containerName, blobName).getETag();
        } else {
            return match;
        }
    }

    public static OkHttpClient buildOkHttpClientWithInterceptors(List<Interceptor> interceptors) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();

        for (Interceptor interceptor : interceptors) {
            clientBuilder.addInterceptor(interceptor);
        }

        return clientBuilder.build();
    }

    public static Request getSimpleRequest(MockWebServer mockWebServer) {
        return new Request.Builder()
            .url(mockWebServer.url("/"))
            .build();
    }

    public static Request getSimpleRequestWithHeader(MockWebServer mockWebServer,
                                              String name,
                                              String value) {
        return new Request.Builder()
            .url(mockWebServer.url("/"))
            .addHeader(name, value)
            .build();
    }

    public static Request getSimpleRequestWithHeaders(MockWebServer mockWebServer,
                                               Map<String, String> headers) {
        Request.Builder builder = new Request.Builder()
            .url(mockWebServer.url("/"));

        for (Map.Entry header : headers.entrySet()) {
            builder.addHeader((String) header.getKey(), (String) header.getValue());
        }

        return builder.build();
    }

    public static Request getSimpleRequestWithQueryParam(MockWebServer mockWebServer,
                                                  String name,
                                                  String value) {
        String path = "/?" + name + "=" + value;

        return new Request.Builder()
            .url(mockWebServer.url(path))
            .build();
    }

    public static Request getSimpleRequestWithQueryParams(MockWebServer mockWebServer,
                                                   Map<String, String> queryParams) {
        StringBuilder pathStringBuilder = new StringBuilder("/?");

        for (Map.Entry queryParam : queryParams.entrySet()) {
            pathStringBuilder.append(queryParam.getKey())
                .append("=")
                .append(queryParam.getValue())
                .append("&");
        }

        pathStringBuilder.setLength(pathStringBuilder.length() - 1);

        return new Request.Builder()
            .url(mockWebServer.url(pathStringBuilder.toString()))
            .build();
    }

    public static String getStackTraceString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();

        return stringWriter.toString();
    }

    // ------------------------ GENERATING DATA ---------------------------

    public static byte[] getDefaultData() {
        return "Hello World".getBytes(StandardCharsets.UTF_8);
    }

    public static String getDefaultString() {
        return "Hello World";
    }
}
