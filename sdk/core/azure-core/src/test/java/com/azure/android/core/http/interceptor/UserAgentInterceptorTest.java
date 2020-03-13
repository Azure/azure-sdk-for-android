// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.interceptor;

import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.provider.ApplicationInformationProvider;
import com.azure.android.core.provider.LocaleInformationProvider;
import com.azure.android.core.provider.PlatformInformationProvider;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.http.interceptor.TestUtils.buildOkHttpClientWithInterceptor;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequest;
import static com.azure.android.core.http.interceptor.TestUtils.getSimpleRequestWithHeader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserAgentInterceptorTest {
    private final MockWebServer mockWebServer = new MockWebServer();

    @Rule
    public EnqueueMockResponse enqueueMockResponse = new EnqueueMockResponse(mockWebServer);

    @Test
    public void userAgentHeader_isPopulated_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with no input data in its constructor.
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(null,
            null,
            null,
            null,
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated.
        assertNotNull(mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_isPrependedToNonEmptyHeader_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with no input data in its constructor...
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(null,
            null,
            null,
            null,
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);
        // ...and a request where the "User-Agent" header is already populated.
        String userAgent = "Test User Agent";
        Request request = getSimpleRequestWithHeader(mockWebServer, HttpHeader.USER_AGENT, userAgent);

        // When executing said request.
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be contain the result of prepending the generated user agent string to
        // the existing value.
        assertEquals("azsdk-android-/ ( - ; : -> ; _) " + userAgent,
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_hasCorrectFormatWithoutApplicationId_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with no user-provided applicationId.
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(null,
            null,
            null,
            null,
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while not
        // including the applicationId as a prefix.
        assertEquals("azsdk-android-/ ( - ; : -> ; _)",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_hasCorrectFormatWithApplicationId_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with a user-provided applicationId.
        String userApplicationId = "UserApplicationId";
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(userApplicationId,
            null,
            null,
            null,
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including the applicationId as a prefix.
        assertEquals("[" + userApplicationId + "] azsdk-android-/ ( - ; : -> ; _)",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_hasTrimmedApplicationId_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with a user-provided applicationId containing spaces.
        String userApplicationId = "User Application Id";
        String trimmedUserApplicationId = "UserApplicationId";
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(userApplicationId,
            null,
            null,
            null,
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including a trimmed applicationId as a prefix.
        assertEquals("[" + trimmedUserApplicationId + "] azsdk-android-/ ( - ; : -> ; _)",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_hasTruncatedApplicationId_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with a user-provided applicationId longer than 24 characters.
        String userApplicationId = "UserApplicationIdThatIsVeryLong";
        String truncatedUserApplicationId = "UserApplicationIdThatIsV";
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(userApplicationId,
            null,
            null,
            null,
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including a truncated applicationId as a prefix.
        assertEquals("[" + truncatedUserApplicationId + "] azsdk-android-/ ( - ; : -> ; _)",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_includesBasicInfo_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with a user-provided applicationId, sdkName and sdkVersion.
        String userApplicationId = "UserApplicationId";
        String sdkName = "SDK Name";
        String sdkVersion = "SDK Version";
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(userApplicationId,
            sdkName,
            sdkVersion,
            null,
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including the given applicationId as a prefix.
        assertEquals("[" + userApplicationId + "] azsdk-android-" + sdkName + "/" + sdkVersion + " ( - ; : -> ; _)",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_includesPlatformInfo_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with an PlatformInformationProvider.
        String deviceName = "Test Device";
        int osVersion = 123;
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(null,
            null,
            null,
            new TestPlatformInformationProvider(deviceName, osVersion),
            null,
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including the given platform info.
        assertEquals("azsdk-android-/ (" + deviceName + " - " + osVersion + "; : -> ; _)",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_includesApplicationInfo_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with an ApplicationInformationProvider.
        String applicationId = "Application ID";
        String applicationVersion = "1.0";
        int targetSdkVersion = 21;
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(null,
            null,
            null,
            null,
            new TestApplicationInformationProvider(applicationId, applicationVersion, targetSdkVersion),
            null);
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including the given application info.
        assertEquals("azsdk-android-/ ( - ; " + applicationId + ":" + applicationVersion + " -> " + targetSdkVersion + "; _)",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_includesLocaleInfo_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with an LocaleInformationProvider.
        String defaultSystemLanguage = "en";
        String systemRegion = "US";
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(null,
            null,
            null,
            null,
            null,
            new TestLocaleInformationProvider(defaultSystemLanguage, systemRegion));
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including the given locale info.
        assertEquals("azsdk-android-/ ( - ; : -> ; " + defaultSystemLanguage + "_" + systemRegion + ")",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    @Test
    public void userAgentHeader_includesAllInfo_onRequest() throws InterruptedException, IOException {
        // Given a client with a UserAgentInterceptor with an all information provided to its constructor.
        String userApplicationId = "UserApplicationId";
        String sdkName = "SDK Name";
        String sdkVersion = "SDK Version";
        String deviceName = "Test Device";
        int osVersion = 123;
        String applicationId = "Application ID";
        String applicationVersion = "1.0";
        int targetSdkVersion = 21;
        String defaultSystemLanguage = "en";
        String systemRegion = "US";
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(userApplicationId,
            sdkName,
            sdkVersion,
            new TestPlatformInformationProvider(deviceName, osVersion),
            new TestApplicationInformationProvider(applicationId, applicationVersion, targetSdkVersion),
            new TestLocaleInformationProvider(defaultSystemLanguage, systemRegion));
        OkHttpClient okHttpClient = buildOkHttpClientWithInterceptor(userAgentInterceptor);

        // When executing a request.
        Request request = getSimpleRequest(mockWebServer);
        okHttpClient.newCall(request).execute();

        // Then the 'User-Agent' header should be populated following the format specified by the guidelines while
        // including the given info.
        assertEquals("[" + userApplicationId + "] azsdk-android-" + sdkName + "/" + sdkVersion + " (" + deviceName + " - " +
                osVersion + "; " + applicationId + ":" + applicationVersion + " -> " + targetSdkVersion + "; " +
                defaultSystemLanguage + "_" + systemRegion + ")",
            mockWebServer.takeRequest().getHeader(HttpHeader.USER_AGENT));
    }

    private static class TestPlatformInformationProvider implements PlatformInformationProvider {
        String deviceName;
        int osVersion;

        TestPlatformInformationProvider(String deviceName,
                                        int osVersion) {
            this.deviceName = deviceName;
            this.osVersion = osVersion;
        }

        @Override
        public String getDeviceName() {
            return deviceName;
        }

        @Override
        public int getOsVersion() {
            return osVersion;
        }
    }

    private static class TestApplicationInformationProvider implements ApplicationInformationProvider {
        String applicationId, applicationVersion;
        int targetSdkVersion;

        TestApplicationInformationProvider(String applicationId,
                                           String applicationVersion,
                                           int targetSdkVersion) {
            this.applicationId = applicationId;
            this.applicationVersion = applicationVersion;
            this.targetSdkVersion = targetSdkVersion;
        }

        @Override
        public String getApplicationId() {
            return applicationId;
        }

        @Override
        public String getApplicationVersion() {
            return applicationVersion;
        }

        @Override
        public int getTargetSdkVersion() {
            return targetSdkVersion;
        }
    }

    private static class TestLocaleInformationProvider implements LocaleInformationProvider {
        String defaultSystemLanguage, systemRegion;

        TestLocaleInformationProvider(String defaultSystemLanguage,
                                      String systemRegion) {
            this.defaultSystemLanguage = defaultSystemLanguage;
            this.systemRegion = systemRegion;
        }

        @Override
        public String getDefaultSystemLanguage() {
            return defaultSystemLanguage;
        }

        @Override
        public String getSystemRegion() {
            return systemRegion;
        }
    }
}
