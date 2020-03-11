package com.azure.android.core.http;

import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.http.interceptor.LoggingInterceptor;
import com.azure.android.core.internal.util.serializer.SerializerAdapter;
import com.azure.android.core.internal.util.serializer.SerializerFormat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import retrofit2.Retrofit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceClientTest {
    private static final String BASE_URL = "http://127.0.0.1/";
    private static ServiceClient serviceClient;

    @BeforeClass
    public static void setUp() {
        serviceClient = new ServiceClient.Builder()
            .setBaseUrl(BASE_URL)
            .setSerializationFormat(SerializerFormat.JSON)
            .build();
    }

    @Test
    public void getRetrofit() {
        Retrofit retrofit = serviceClient.getRetrofit();

        assertNotNull(retrofit);
        assertEquals(BASE_URL, retrofit.baseUrl().toString());
    }

    @Test
    public void getBaseUrl() {
        assertEquals(BASE_URL, serviceClient.getBaseUrl());
    }

    @Test
    public void getSerializerAdapter() {
        assertEquals(SerializerAdapter.createDefault(), serviceClient.getSerializerAdapter());
    }

    @Test
    public void buildServiceClient_fromAnotherInstance() {
        Retrofit retrofit = serviceClient.getRetrofit();
        ServiceClient otherServiceClient = serviceClient.newBuilder().build();
        Retrofit otherRetrofit = otherServiceClient.getRetrofit();

        assertNotNull(retrofit);
        assertNotNull(otherRetrofit);
        assertEquals(otherRetrofit.baseUrl().toString(), retrofit.baseUrl().toString());
    }

    @Test
    public void buildClient_withAllParameters() {
        ServiceClient otherServiceClient = new ServiceClient.Builder()
            .setConnectionPool(new ConnectionPool())
            .addNetworkInterceptor(new LoggingInterceptor(null))
            .addInterceptor(new AddDateInterceptor())
            .setSerializationFormat(SerializerFormat.JSON)
            .setBaseUrl(BASE_URL)
            .setConnectionTimeout(100, TimeUnit.SECONDS)
            .setCredentialsInterceptor((Interceptor) chain -> chain.proceed(chain.request()))
            .setDispatcher(new Dispatcher())
            .setReadTimeout(50, TimeUnit.SECONDS)
            .build();

        Retrofit retrofit = otherServiceClient.getRetrofit();

        assertNotNull(retrofit);
        assertEquals(BASE_URL, retrofit.baseUrl().toString());
        assertEquals(BASE_URL, otherServiceClient.getBaseUrl());
        assertEquals(SerializerAdapter.createDefault(), otherServiceClient.getSerializerAdapter());
    }

    @Test
    public void buildClient_basedOnAnotherClient() {
        ServiceClient otherServiceClient = serviceClient.newBuilder()
            .addNetworkInterceptor(new LoggingInterceptor(null))
            .addInterceptor(new AddDateInterceptor())
            .setSerializationFormat(SerializerFormat.JSON)
            .build();

        Retrofit retrofit = serviceClient.getRetrofit();
        Retrofit otherRetrofit = otherServiceClient.getRetrofit();

        assertEquals(retrofit.baseUrl().toString(), otherRetrofit.baseUrl().toString());
        assertEquals(serviceClient.getBaseUrl(), otherServiceClient.getBaseUrl());
        assertEquals(serviceClient.getSerializerAdapter(), otherServiceClient.getSerializerAdapter());
    }

    @Test
    public void buildClient_withNoSlashAtTheEndOfBaseUrl() {
        ServiceClient otherServiceClient = new ServiceClient.Builder()
            .setBaseUrl("http://127.0.0.1")
            .setSerializationFormat(SerializerFormat.JSON)
            .build();

        assertEquals("http://127.0.0.1/", otherServiceClient.getBaseUrl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildClient_withNoBaseUrl_willThrowException() {
        ServiceClient otherServiceClient = new ServiceClient.Builder()
            .setSerializationFormat(SerializerFormat.JSON)
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildClient_withNoSerializationFormat_willThrowException() {
        ServiceClient otherServiceClient = new ServiceClient.Builder()
            .setBaseUrl(BASE_URL)
            .build();
    }

    @AfterClass
    public static void tearDown() {
        serviceClient.close();
    }
}
