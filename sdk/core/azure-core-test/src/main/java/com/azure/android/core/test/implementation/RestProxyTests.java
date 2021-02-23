// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.test.implementation;

import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.http.exception.HttpResponseException;
import com.azure.android.core.http.policy.HttpLogDetailLevel;
import com.azure.android.core.http.policy.HttpLogOptions;
import com.azure.android.core.http.policy.HttpLoggingPolicy;
import com.azure.android.core.http.policy.PortPolicy;
import com.azure.android.core.rest.Callback;
import com.azure.android.core.rest.Response;
import com.azure.android.core.rest.ResponseBase;
import com.azure.android.core.rest.RestProxy;
import com.azure.android.core.rest.StreamResponse;
import com.azure.android.core.rest.annotation.BodyParam;
import com.azure.android.core.rest.annotation.Delete;
import com.azure.android.core.rest.annotation.ExpectedResponses;
import com.azure.android.core.rest.annotation.FormParam;
import com.azure.android.core.rest.annotation.Get;
import com.azure.android.core.rest.annotation.Head;
import com.azure.android.core.rest.annotation.HeaderParam;
import com.azure.android.core.rest.annotation.Headers;
import com.azure.android.core.rest.annotation.Host;
import com.azure.android.core.rest.annotation.HostParam;
import com.azure.android.core.rest.annotation.Patch;
import com.azure.android.core.rest.annotation.PathParam;
import com.azure.android.core.rest.annotation.Post;
import com.azure.android.core.rest.annotation.Put;
import com.azure.android.core.rest.annotation.QueryParam;
import com.azure.android.core.rest.annotation.ServiceInterface;
import com.azure.android.core.rest.annotation.UnexpectedResponseExceptionType;
import com.azure.android.core.rest.annotation.UnexpectedResponseExceptionTypes;
import com.azure.android.core.test.MyRestException;
import com.azure.android.core.test.implementation.entities.HttpBinFormDataJSON;
import com.azure.android.core.test.implementation.entities.HttpBinHeaders;
import com.azure.android.core.test.implementation.entities.HttpBinJSON;
import com.azure.android.core.serde.SerdeAdapter;
import com.azure.android.core.serde.jackson.JacksonSerderAdapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class RestProxyTests {

    /**
     * Get the HTTP client that will be used for each test. This will be called once per test.
     *
     * @return The HTTP client to use for each test.
     */
    protected abstract HttpClient createHttpClient();

    /**
     * Get the dynamic port the WireMock server is using to properly route the request.
     *
     * @return The HTTP port WireMock is using.
     */
    protected abstract int getWireMockPort();

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(500, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }

    private static final class CallbackResult<T> {
        Response<T> response;
        Throwable error;
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service1")
    private interface Service1 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        void getByteArray(Callback<Response<byte[]>> callback);
    }

    @Test
    public void requestWithByteArrayReturnType() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<byte[]> cbResult = new CallbackResult<>();

        createService(Service1.class).getByteArray(new Callback<Response<byte[]>>() {
            @Override
            public void onSuccess(Response<byte[]> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "requestWithByteArrayReturnType");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<byte[]> response = cbResult.response;
            Assertions.assertNotNull(response);
            byte[] value = response.getValue();
            assertNotNull(value);
            assertEquals(100, value.length);
        }
    }

    @Host("http://{hostName}")
    @ServiceInterface(name = "Service2")
    private interface Service2 {
        @Get("bytes/{numberOfBytes}")
        @ExpectedResponses({200})
        void getByteArray(@HostParam("hostName") String host,
                          @PathParam("numberOfBytes") int numberOfBytes,
                          Callback<Response<byte[]>> callback);
    }

    @Test
    public void requestWithByteArrayReturnTypeAndParameterizedHostAndPath() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<byte[]> cbResult = new CallbackResult<>();

        createService(Service2.class).getByteArray("localhost", 100,
            new Callback<Response<byte[]>>() {
                @Override
                public void onSuccess(Response<byte[]> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "requestWithByteArrayReturnTypeAndParameterizedHostAndPath");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<byte[]> response = cbResult.response;
            Assertions.assertNotNull(response);
            byte[] value = response.getValue();
            assertNotNull(value);
            assertEquals(100, value.length);
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service3")
    private interface Service3 {
        @Get("bytes/100")
        @ExpectedResponses({200})
        void getNothing(Callback<Response<Void>> callback);
    }

    @Test
    public void getRequestWithNoReturn() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();
        createService(Service3.class).getNothing(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithNoReturn");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertNotNull(response);
            Void value = response.getValue();
            assertNull(value);
        }
    }


    @Host("http://localhost")
    @ServiceInterface(name = "Service5")
    private interface Service5 {
        @Get("anything")
        @ExpectedResponses({200})
        void getAnything(Callback<Response<HttpBinJSON>> callback);

        @Get("anything/with+plus")
        @ExpectedResponses({200})
        void getAnythingWithPlus(Callback<Response<HttpBinJSON>> callback);

        @Get("anything/{path}")
        @ExpectedResponses({200})
        void getAnythingWithPathParam(@PathParam("path") String pathParam, Callback<Response<HttpBinJSON>> callback);

        @Get("anything/{path}")
        @ExpectedResponses({200})
        void getAnythingWithEncodedPathParam(@PathParam(value = "path", encoded = true) String pathParam,
                                             Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void getRequestWithAnything() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnything(new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithAnything");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything", json.url());
        }
    }

    @Test
    public void getRequestWithAnythingWithPlus() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnythingWithPlus(new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithAnythingWithPlus");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything/with+plus", json.url());
        }
    }

    @Test
    public void getRequestWithAnythingWithPathParam() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnythingWithPathParam("withpathparam",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "getRequestWithAnythingWithPathParam");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
        }
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithSpace() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnythingWithPathParam("with path param",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "getRequestWithAnythingWithPathParamWithSpace");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
        }
    }

    @Test
    public void getRequestWithAnythingWithPathParamWithPlus() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnythingWithPathParam("with+path+param",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "getRequestWithAnythingWithPathParamWithPlus");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
        }
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParam() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnythingWithEncodedPathParam("withpathparam",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "getRequestWithAnythingWithEncodedPathParam");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything/withpathparam", json.url());
        }
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPercent20() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnythingWithEncodedPathParam("with%20path%20param",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "getRequestWithAnythingWithEncodedPathParamWithPercent20");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything/with path param", json.url());
        }
    }

    @Test
    public void getRequestWithAnythingWithEncodedPathParamWithPlus() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service5.class).getAnythingWithEncodedPathParam("with+path+param",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "getRequestWithAnythingWithEncodedPathParamWithPlus");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything/with+path+param", json.url());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service6")
    private interface Service6 {
        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnything(@QueryParam("a") String a,
                                @QueryParam("b") int b,
                                Callback<Response<HttpBinJSON>> callback);

        @Get("anything")
        @ExpectedResponses({200})
        HttpBinJSON getAnythingWithEncoded(@QueryParam(value = "a", encoded = true) String a,
                                           @QueryParam("b") int b,
                                           Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void getRequestWithQueryParametersAndAnything() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service6.class).getAnything("A", 15, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithQueryParametersAndAnything");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything?a=A&b=15", json.url());
        }
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithPercent20() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service6.class).getAnything("A%20Z", 15, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithQueryParametersAndAnythingWithPercent20");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything?a=A%2520Z&b=15", json.url());
        }
    }

    @Test
    public void getRequestWithQueryParametersAndAnythingWithEncodedWithPercent20() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service6.class).getAnythingWithEncoded("x%20y", 15, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithQueryParametersAndAnythingWithEncodedWithPercent20");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything?a=x y&b=15", json.url());
        }
    }

    @Test
    public void getRequestWithNullQueryParameter() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service6.class).getAnything(null, 15, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithNullQueryParameter");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything?b=15", json.url());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service7")
    private interface Service7 {
        @Get("anything")
        @ExpectedResponses({200})
        void getAnything(@HeaderParam("a") String a, @HeaderParam("b") int b, Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void getRequestWithHeaderParametersAndAnythingReturn() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service7.class).getAnything("A", 15, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithHeaderParametersAndAnythingReturn");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything", json.url());
            assertNotNull(json.headers());
            final HttpHeaders headers = new HttpHeaders(json.headers());
            assertEquals("A", headers.getValue("A"));
            assertArrayEquals(new String[]{"A"}, headers.getValues("A"));
            assertEquals("15", headers.getValue("B"));
            assertArrayEquals(new String[]{"15"}, headers.getValues("B"));
        }
    }

    @Test
    public void getRequestWithNullHeader() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service7.class).getAnything(null, 15, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "getRequestWithNullHeader");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            final HttpHeaders headers = new HttpHeaders(json.headers());
            assertNull(headers.getValue("A"));
            assertArrayEquals(null, headers.getValues("A"));
            assertEquals("15", headers.getValue("B"));
            assertArrayEquals(new String[]{"15"}, headers.getValues("B"));
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service8")
    private interface Service8 {
        @Post("post")
        @ExpectedResponses({200})
        HttpBinJSON post(@BodyParam("application/octet-stream") String postBody,
                         Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void postRequestWithStringBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service8.class).post("I'm a post body!", new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "postRequestWithStringBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertEquals(String.class, json.data().getClass());
            assertEquals("I'm a post body!", json.data());
        }
    }

    @Test
    public void postRequestWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service8.class).post(null, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "postRequestWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            assertEquals("", json.data());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service9")
    private interface Service9 {
        @Put("put")
        @ExpectedResponses({200})
        void put(@BodyParam("application/octet-stream") int putBody, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(MyRestException.class)
        })
        HttpBinJSON putBodyAndContentLength(@BodyParam("application/octet-stream") byte[] body,
                                            @HeaderParam("Content-Length") long contentLength,
                                            Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @ExpectedResponses({201})
        HttpBinJSON putWithUnexpectedResponse(@BodyParam("application/octet-stream") String putBody,
                                              Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(MyRestException.class)
        })
        HttpBinJSON putWithUnexpectedResponseAndExceptionType(
            @BodyParam("application/octet-stream") String putBody,
            Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(code = {200}, value = MyRestException.class),
            @UnexpectedResponseExceptionType(HttpResponseException.class)
        })
        HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(
            @BodyParam("application/octet-stream") String putBody,
            Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(code = {400}, value = HttpResponseException.class),
            @UnexpectedResponseExceptionType(MyRestException.class)
        })
        HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(
            @BodyParam("application/octet-stream") String putBody,
            Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionTypes({
            @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        })
        HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(
            @BodyParam("application/octet-stream") String putBody,
            Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void putRequestWithIntBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service9.class).put(42, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "putRequestWithIntBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals(String.class, json.data().getClass());
            assertEquals("42", json.data());
        }
    }

    // Test all scenarios for the body length and content length comparison API
    @Test
    public void putRequestWithBodyAndEqualContentLength() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        final byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        createService(Service9.class).putBodyAndContentLength(body, 4L,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "putRequestWithBodyAndEqualContentLength");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("test", json.data());
            assertEquals("application/octet-stream", json.headers().get(("Content-Type")));
            assertEquals("4", json.headers().get(("Content-Length")));
        }
    }

//    @Test
//    public void putRequestWithBodyLessThanContentLength() {
//        CountDownLatch latch = new CountDownLatch(1);
//        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();
//
//        final byte[] body = "test".getBytes(StandardCharsets.UTF_8);
//        createService(Service9.class).putBodyAndContentLength(body, 5L,
//            new Callback<Response<HttpBinJSON>>() {
//                @Override
//                public void onSuccess(Response<HttpBinJSON> response) {
//                    cbResult.response = response;
//                    latch.countDown();
//                }
//
//                @Override
//                public void onFailure(Throwable error) {
//                    cbResult.error = error;
//                    latch.countDown();
//                }
//            });
//
//        awaitOnLatch(latch, "putRequestWithBodyLessThanContentLength");
//
//        if (cbResult.error == null) {
//            Assertions.fail();
//        } else {
//            final Throwable error = cbResult.error;
//            Assertions.assertNotNull(error);
//            Assertions.assertTrue(error instanceof UnexpectedLengthException);
//            assertTrue(((UnexpectedLengthException) error).getMessage().contains("less than"));
//        }
//    }
//
//    @Test
//    public void putRequestWithBodyMoreThanContentLength() {
//        CountDownLatch latch = new CountDownLatch(1);
//        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();
//
//        final byte[] body = "test".getBytes(StandardCharsets.UTF_8);
//        createService(Service9.class).putBodyAndContentLength(body, 3L,
//            new Callback<Response<HttpBinJSON>>() {
//                @Override
//                public void onSuccess(Response<HttpBinJSON> response) {
//                    cbResult.response = response;
//                    latch.countDown();
//                }
//
//                @Override
//                public void onFailure(Throwable error) {
//                    cbResult.error = error;
//                    latch.countDown();
//                }
//            });
//
//        awaitOnLatch(latch, "putRequestWithBodyLessThanContentLength");
//
//        if (cbResult.error == null) {
//            Assertions.fail();
//        } else {
//            final Throwable error = cbResult.error;
//            Assertions.assertNotNull(error);
//            Assertions.assertTrue(error instanceof UnexpectedLengthException);
//            assertTrue(((UnexpectedLengthException) error).getMessage().contains("more than"));
//        }
//    }

    @Test
    public void putRequestWithUnexpectedResponse() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service9.class).putWithUnexpectedResponse("I'm the body!",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "putRequestWithUnexpectedResponse");

        if (cbResult.response != null) {
            fail("Expected HttpResponseException would be thrown.");
        } else {
            Assertions.assertNotNull(cbResult.error);
            Assertions.assertTrue(cbResult.error instanceof HttpResponseException);
            HttpResponseException e = (HttpResponseException) cbResult.error;
            assertNotNull(e.getValue());
            assertTrue(e.getValue() instanceof LinkedHashMap);
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }


    @Test
    public void putRequestWithUnexpectedResponseAndExceptionType() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service9.class).putWithUnexpectedResponseAndExceptionType("I'm the body!",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "putRequestWithUnexpectedResponseAndExceptionType");

        if (cbResult.response != null) {
            fail("Expected HttpResponseException would be thrown.");
        } else {
            Assertions.assertNotNull(cbResult.error);
            Assertions.assertTrue(cbResult.error instanceof MyRestException,
                "Expected MyRestException would be thrown. Instead got "
                    + cbResult.error.getClass().getSimpleName());
            MyRestException e = (MyRestException) cbResult.error;
            assertNotNull(e.getValue());
            assertEquals("I'm the body!", e.getValue().data());
        }
    }

    @Test
    public void putRequestWithUnexpectedResponseAndDeterminedExceptionType() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service9.class).putWithUnexpectedResponseAndDeterminedExceptionType("I'm the body!",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "putRequestWithUnexpectedResponseAndDeterminedExceptionType");

        if (cbResult.response != null) {
            fail("Expected HttpResponseException would be thrown.");
        } else {
            Assertions.assertNotNull(cbResult.error);
            Assertions.assertTrue(cbResult.error instanceof MyRestException,
                "Expected MyRestException would be thrown. Instead got "
                    + cbResult.error.getClass().getSimpleName());
            MyRestException e = (MyRestException) cbResult.error;
            assertNotNull(e.getValue());
            assertEquals("I'm the body!", e.getValue().data());
        }
    }

    @Test
    public void putRequestWithUnexpectedResponseAndFallthroughExceptionType() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service9.class).putWithUnexpectedResponseAndFallthroughExceptionType("I'm the body!",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "putRequestWithUnexpectedResponseAndFallthroughExceptionType");

        if (cbResult.response != null) {
            fail("Expected HttpResponseException would be thrown.");
        } else {
            Assertions.assertNotNull(cbResult.error);
            Assertions.assertTrue(cbResult.error instanceof MyRestException,
                "Expected MyRestException would be thrown. Instead got "
                    + cbResult.error.getClass().getSimpleName());
            MyRestException e = (MyRestException) cbResult.error;
            assertNotNull(e.getValue());
            assertEquals("I'm the body!", e.getValue().data());
        }
    }

    @Test
    public void putRequestWithUnexpectedResponseAndNoFallthroughExceptionType() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service9.class).putWithUnexpectedResponseAndNoFallthroughExceptionType("I'm the body!",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "putRequestWithUnexpectedResponseAndNoFallthroughExceptionType");

        if (cbResult.response != null) {
            fail("Expected HttpResponseException would be thrown.");
        } else {
            Assertions.assertNotNull(cbResult.error);
            Assertions.assertTrue(cbResult.error instanceof HttpResponseException,
                "Expected HttpResponseException would be thrown. Instead got "
                    + cbResult.error.getClass().getSimpleName());
            HttpResponseException e = (HttpResponseException) cbResult.error;
            assertNotNull(e.getValue());
            assertTrue(e.getValue() instanceof LinkedHashMap);

            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, String> expectedBody = (LinkedHashMap<String, String>) e.getValue();
            assertEquals("I'm the body!", expectedBody.get("data"));
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service10")
    private interface Service10 {
        @Head("anything")
        @ExpectedResponses({200})
        void head(Callback<Response<Void>> callback);

        @Head("anything")
        @ExpectedResponses({200})
        void headBoolean(Callback<Response<Boolean>> callback);

        @Head("anything")
        @ExpectedResponses({200})
        void voidHead(Callback<Response<Void>> callback);
    }

    @Test
    public void headRequest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service10.class).head(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "headRequest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertNull(response.getValue());
        }
    }

    @Test
    public void headBooleanRequest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Boolean> cbResult = new CallbackResult<>();

        createService(Service10.class).headBoolean(new Callback<Response<Boolean>>() {
            @Override
            public void onSuccess(Response<Boolean> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "headBooleanRequest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Boolean> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertTrue(response.getValue());
        }
    }

    @Test
    public void syncVoidHeadRequest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service10.class)
            .voidHead(new Callback<Response<Void>>() {
                @Override
                public void onSuccess(Response<Void> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "syncVoidHeadRequest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertNull(response.getValue());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service11")
    private interface Service11 {
        @Delete("delete")
        @ExpectedResponses({200})
        void delete(@BodyParam("application/octet-stream") boolean bodyBoolean, Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void deleteRequest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service11.class).delete(false, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "deleteRequest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getValue());
            HttpBinJSON json = response.getValue();
            assertEquals(String.class, json.data().getClass());
            assertEquals("false", json.data());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service12")
    private interface Service12 {
        @Patch("patch")
        @ExpectedResponses({200})
        void patch(@BodyParam("application/octet-stream") String bodyString, Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void patchRequest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service12.class).patch("body-contents", new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "patchRequest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getValue());
            HttpBinJSON json = response.getValue();
            assertEquals(String.class, json.data().getClass());
            assertEquals("body-contents", json.data());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service13")
    private interface Service13 {
        @Get("anything")
        @ExpectedResponses({200})
        @Headers({"MyHeader:MyHeaderValue", "MyOtherHeader:My,Header,Value"})
        void get(Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void headersRequest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service13.class).get(new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "headersRequest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getValue());
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/anything", json.url());
            assertNotNull(json.headers());
            final HttpHeaders headers = new HttpHeaders(json.headers());
            assertEquals("MyHeaderValue", headers.getValue("MyHeader"));
            assertArrayEquals(new String[]{"MyHeaderValue"}, headers.getValues("MyHeader"));
            assertEquals("My,Header,Value", headers.getValue("MyOtherHeader"));
            assertArrayEquals(new String[]{"My", "Header", "Value"}, headers.getValues("MyOtherHeader"));
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service16")
    private interface Service16 {
        @Put("put")
        @ExpectedResponses({200})
        void putByteArray(@BodyParam("application/octet-stream") byte[] bytes, Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void service16Put() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        final Service16 service16 = createService(Service16.class);
        final byte[] expectedBytes = new byte[]{1, 2, 3, 4};
        service16.putByteArray(expectedBytes, new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service16Put");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getValue());
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertTrue(json.data() instanceof String);

            final String base64String = (String) json.data();
            final byte[] actualBytes = base64String.getBytes();
            assertArrayEquals(expectedBytes, actualBytes);
            // httpbin sends the data back as a string like "\u0001\u0002\u0003\u0004"
        }
    }

    @Host("http://{hostPart1}{hostPart2}")
    @ServiceInterface(name = "Service17")
    private interface Service17 {
        @Get("get")
        @ExpectedResponses({200})
        void get(@HostParam("hostPart1") String hostPart1, @HostParam("hostPart2") String hostPart2,
                 Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void requestWithMultipleHostParams() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service17.class).get("local", "host", new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "requestWithMultipleHostParams");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            Assertions.assertNotNull(response);
            Assertions.assertNotNull(response.getValue());
            HttpBinJSON json = response.getValue();
            assertNotNull(json);
            assertMatchWithHttpOrHttps("localhost/get", json.url());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service18")
    private interface Service18 {
        @Get("status/200")
        void getStatus200(Callback<Response<Void>> callback);

        @Get("status/200")
        @ExpectedResponses({200})
        void getStatus200WithExpectedResponse200(Callback<Response<Void>> callback);

        @Get("status/300")
        void getStatus300(Callback<Response<Void>> callback);

        @Get("status/300")
        @ExpectedResponses({300})
        void getStatus300WithExpectedResponse300(Callback<Response<Void>> callback);

        @Get("status/400")
        void getStatus400(Callback<Response<Void>> callback);

        @Get("status/400")
        @ExpectedResponses({400})
        void getStatus400WithExpectedResponse400(Callback<Response<Void>> callback);

        @Get("status/500")
        void getStatus500(Callback<Response<Void>> callback);

        @Get("status/500")
        @ExpectedResponses({500})
        void getStatus500WithExpectedResponse500(Callback<Response<Void>> callback);
    }

    @Test
    public void service18GetStatus200() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus200(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus200");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void service18GetStatus200WithExpectedResponse200() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus200WithExpectedResponse200(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus200WithExpectedResponse200");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void service18GetStatus300() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus300(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus300");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertEquals(300, response.getStatusCode());
        }
    }

    @Test
    public void service18GetStatus300WithExpectedResponse300() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus300WithExpectedResponse300(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus300WithExpectedResponse300");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertEquals(300, response.getStatusCode());
        }
    }

    @Test
    public void service18GetStatus400() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus400(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus400");

        if (cbResult.response != null) {
            fail("Expected HttpResponseException would be thrown.");
        } else {
            Assertions.assertNotNull(cbResult.error);
            Assertions.assertTrue(cbResult.error instanceof HttpResponseException,
                "Expected HttpResponseException would be thrown. Instead got "
                    + cbResult.error.getClass().getSimpleName());
            HttpResponseException e = (HttpResponseException) cbResult.error;
            assertNull(e.getValue());
            Assertions.assertEquals(400, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void service18GetStatus400WithExpectedResponse400() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus400WithExpectedResponse400(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus400WithExpectedResponse400");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertEquals(400, response.getStatusCode());
        }
    }

    @Test
    public void service18GetStatus500() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus500(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus400");

        if (cbResult.response != null) {
            fail("Expected HttpResponseException would be thrown.");
        } else {
            Assertions.assertNotNull(cbResult.error);
            Assertions.assertTrue(cbResult.error instanceof HttpResponseException,
                "Expected HttpResponseException would be thrown. Instead got "
                    + cbResult.error.getClass().getSimpleName());
            HttpResponseException e = (HttpResponseException) cbResult.error;
            assertNull(e.getValue());
            Assertions.assertEquals(500, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void service18GetStatus500WithExpectedResponse500() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service18.class).getStatus500WithExpectedResponse500(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service18GetStatus500WithExpectedResponse500");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            Assertions.assertEquals(500, response.getStatusCode());
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service19")
    private interface Service19 {
        @Put("put")
        void putWithNoContentTypeAndStringBody(@BodyParam("application/octet-stream") String body,
                                               Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        void putWithNoContentTypeAndByteArrayBody(@BodyParam("application/octet-stream") byte[] body,
                                                  Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        void putWithHeaderApplicationJsonContentTypeAndStringBody(@BodyParam("application/json") String body,
                                                                  Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @Headers({"Content-Type: application/json"})
        void putWithHeaderApplicationJsonContentTypeAndByteArrayBody(
            @BodyParam("application/json") byte[] body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @Headers({"Content-Type: application/json; charset=utf-8"})
        void putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(
            @BodyParam("application/octet-stream") String body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        void putWithHeaderApplicationOctetStreamContentTypeAndStringBody(
            @BodyParam("application/octet-stream") String body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        @Headers({"Content-Type: application/octet-stream"})
        void putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(
            @BodyParam("application/octet-stream") byte[] body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        void putWithBodyParamApplicationJsonContentTypeAndStringBody(
            @BodyParam("application/json") String body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        void putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(
            @BodyParam("application/json" + "; charset=utf-8") String body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        void putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(
            @BodyParam("application/json") byte[] body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        void putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(
            @BodyParam("application/octet-stream") String body, Callback<Response<HttpBinJSON>> callback);

        @Put("put")
        void putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(
            @BodyParam("application/octet-stream") byte[] body, Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithNoContentTypeAndStringBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithNoContentTypeAndStringBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithNoContentTypeAndStringBody("",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithNoContentTypeAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithNoContentTypeAndStringBody("hello",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithNoContentTypeAndStringBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("hello", json.data());
        }
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithNoContentTypeAndByteArrayBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithNoContentTypeAndByteArrayBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithNoContentTypeAndByteArrayBody(new byte[0],
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithNoContentTypeAndByteArrayBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithNoContentTypeAndByteArrayBody(new byte[]{0, 1, 2, 3, 4},
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithNoContentTypeAndByteArrayBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndStringBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndStringBody("",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"\"", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndStringBody("soups and stuff",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"soups and stuff\"", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndByteArrayBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndByteArrayBody(new byte[0],
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"\"", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndByteArrayBody(
            new byte[]{0, 1, 2, 3, 4},
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"AAECAwQ=\"", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody("",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(
            "soups and stuff",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("soups and stuff", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationOctetStreamContentTypeAndStringBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationOctetStreamContentTypeAndStringBody("",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationOctetStreamContentTypeAndStringBody("penguins",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("penguins", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(new byte[0],
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(
            new byte[]{0, 1, 2, 3, 4}, new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithHeaderApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndStringBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndStringBody("",
                new Callback<Response<HttpBinJSON>>() {
                    @Override
                    public void onSuccess(Response<HttpBinJSON> response) {
                        cbResult.response = response;
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        cbResult.error = error;
                        latch.countDown();
                    }
                });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"\"", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndStringBody(
            "soups and stuff", new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndStringBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"soups and stuff\"", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class)
            .putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody("",
                new Callback<Response<HttpBinJSON>>() {
                    @Override
                    public void onSuccess(Response<HttpBinJSON> response) {
                        cbResult.response = response;
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        cbResult.error = error;
                        latch.countDown();
                    }
                });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"\"", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(
            "soups and stuff", new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"soups and stuff\"", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(new byte[0],
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"\"", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(
            new byte[]{0, 1, 2, 3, 4}, new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationJsonContentTypeAndByteArrayBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("\"AAECAwQ=\"", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationOctetStreamContentTypeAndStringBody("",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(
            "penguins",
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationOctetStreamContentTypeAndStringBodyWithNonEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("penguins", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(null,
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNullBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(new byte[0],
            new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals("", json.data());
        }
    }

    @Test
    public void service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithNonEmptyBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service19.class).putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(
            new byte[]{0, 1, 2, 3, 4}, new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service19PutWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBodyWithEmptyBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            HttpBinJSON json = response.getValue();
            Assertions.assertNotNull(json);
            assertEquals(new String(new byte[]{0, 1, 2, 3, 4}), json.data());
        }
    }


    @Host("http://localhost")
    @ServiceInterface(name = "Service20")
    private interface Service20 {
        @Get("bytes/100")
        void getBytes100OnlyHeaders(Callback<ResponseBase<HttpBinHeaders, Void>> callback);

        @Get("bytes/100")
        void getBytes100OnlyRawHeaders(Callback<ResponseBase<HttpBinHeaders, Void>> callback);

        @Get("bytes/100")
        void getBytes100BodyAndHeaders(Callback<ResponseBase<HttpBinHeaders, byte[]>> callback);

        @Put("put")
        void putOnlyHeaders(@BodyParam("application/octet-stream") String body,
                            Callback<ResponseBase<HttpBinHeaders, Void>> callback);

        @Put("put")
        void putBodyAndHeaders(@BodyParam("application/octet-stream") String body,
            Callback<ResponseBase<HttpBinHeaders, HttpBinJSON>> callback);

        @Get("bytes/100")
        void getBytesOnlyStatus(Callback<ResponseBase<Void, Void>> callback);

        @Get("bytes/100")
        void getVoidResponse(Callback<Response<Void>> callback);

        @Put("put")
        void putBody(@BodyParam("application/octet-stream") String body,
                                      Callback<Response<HttpBinJSON>> callback);
    }

    private static final class CallbackResultBase<Hdr, T> {
        ResponseBase<Hdr, T> response;
        Throwable error;
    }

    @Test
    public void service20GetBytes100OnlyHeaders() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResultBase<HttpBinHeaders, Void> cbResult = new CallbackResultBase<>();

        createService(Service20.class).getBytes100OnlyHeaders(new Callback<ResponseBase<HttpBinHeaders, Void>>() {
            @Override
            public void onSuccess(ResponseBase<HttpBinHeaders, Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service20GetBytes100OnlyHeaders");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final ResponseBase<HttpBinHeaders, Void> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            final HttpBinHeaders headers = response.getDeserializedHeaders();
            assertNotNull(headers);
            assertTrue(headers.accessControlAllowCredentials());
            assertNotNull(headers.date());
            assertNotEquals(0, (Object) headers.xProcessedTime());
        }
    }

    @Test
    public void service20GetBytes100BodyAndHeaders() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResultBase<HttpBinHeaders, byte[]> cbResult = new CallbackResultBase<>();

        createService(Service20.class).getBytes100BodyAndHeaders(new Callback<ResponseBase<HttpBinHeaders, byte[]>>() {
            @Override
            public void onSuccess(ResponseBase<HttpBinHeaders, byte[]> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service20GetBytes100BodyAndHeaders");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final ResponseBase<HttpBinHeaders, byte[]> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            final byte[] body = response.getValue();
            assertNotNull(body);
            assertEquals(100, body.length);

            final HttpBinHeaders headers = response.getDeserializedHeaders();
            assertNotNull(headers);
            assertTrue(headers.accessControlAllowCredentials());
            assertNotNull(headers.date());
            assertNotEquals(0, (Object) headers.xProcessedTime());
        }
    }

    @Test
    public void service20GetBytesOnlyStatus() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResultBase<Void, Void> cbResult = new CallbackResultBase<>();

        createService(Service20.class).getBytesOnlyStatus(new Callback<ResponseBase<Void, Void>>() {
            @Override
            public void onSuccess(ResponseBase<Void, Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service20GetBytesOnlyStatus");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final ResponseBase<Void, Void> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void service20GetBytesOnlyHeaders() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResultBase<HttpBinHeaders, Void> cbResult = new CallbackResultBase<>();

        createService(Service20.class).getBytes100OnlyRawHeaders(new Callback<ResponseBase<HttpBinHeaders, Void>>() {
            @Override
            public void onSuccess(ResponseBase<HttpBinHeaders, Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service20GetBytesOnlyHeaders");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final ResponseBase<HttpBinHeaders, Void> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getHeaders());
            assertNotEquals(0, response.getHeaders().getSize());
        }
    }

    @Test
    public void service20PutOnlyHeaders() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResultBase<HttpBinHeaders, Void> cbResult = new CallbackResultBase<>();

        createService(Service20.class).putOnlyHeaders("body string",
            new Callback<ResponseBase<HttpBinHeaders, Void>>() {
                @Override
                public void onSuccess(ResponseBase<HttpBinHeaders, Void> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service20PutOnlyHeaders");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final ResponseBase<HttpBinHeaders, Void> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            final HttpBinHeaders headers = response.getDeserializedHeaders();
            assertNotNull(headers);
            assertTrue(headers.accessControlAllowCredentials());
            assertNotNull(headers.date());
            assertNotEquals(0, (Object) headers.xProcessedTime());
        }
    }

    @Test
    public void service20PutBodyAndHeaders() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResultBase<HttpBinHeaders, HttpBinJSON> cbResult = new CallbackResultBase<>();

        createService(Service20.class).putBodyAndHeaders("body string",
            new Callback<ResponseBase<HttpBinHeaders, HttpBinJSON>>() {
                @Override
                public void onSuccess(ResponseBase<HttpBinHeaders, HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "service20PutBodyAndHeaders");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final ResponseBase<HttpBinHeaders, HttpBinJSON> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            final HttpBinJSON body = response.getValue();
            assertNotNull(body);
            assertMatchWithHttpOrHttps("localhost/put", body.url());
            assertEquals("body string", body.data());

            final HttpBinHeaders headers = response.getDeserializedHeaders();
            assertNotNull(headers);
            assertTrue(headers.accessControlAllowCredentials());
            assertNotNull(headers.date());
            assertNotEquals(0, (Object) headers.xProcessedTime());
        }
    }

    @Test
    public void service20GetVoidResponse() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<Void> cbResult = new CallbackResult<>();

        createService(Service20.class).getVoidResponse(new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service20PutBodyAndHeaders");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<Void> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }

    }

    @Test
    public void service20GetResponseBody() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        createService(Service20.class).putBody("body string", new Callback<Response<HttpBinJSON>>() {
            @Override
            public void onSuccess(Response<HttpBinJSON> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service20GetResponseBody");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            final HttpBinJSON body = response.getValue();
            assertNotNull(body);
            assertMatchWithHttpOrHttps("localhost/put", body.url());
            assertEquals("body string", body.data());
            final HttpHeaders headers = response.getHeaders();
            assertNotNull(headers);
        }
    }


    @Host("http://localhost")
    @ServiceInterface(name = "UnexpectedOKService")
    interface UnexpectedOKService {
        @Get("/bytes/1024")
        @ExpectedResponses({400})
        void getBytes(Callback<StreamResponse> callback);
    }

    @Test
    public void unexpectedHTTPOK() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<InputStream> cbResult = new CallbackResult<>();

        createService(UnexpectedOKService.class).getBytes(new Callback<StreamResponse>() {
            @Override
            public void onSuccess(StreamResponse response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "unexpectedHTTPOK");

        if (cbResult.error == null) {
            Assertions.fail();
        } else {
            assertNotNull(cbResult.error);
            assertTrue(cbResult.error instanceof HttpResponseException);
            assertEquals("Status code 200, (1024-byte body)", cbResult.error.getMessage());
        }
    }

    @Host("https://www.example.com")
    @ServiceInterface(name = "Service21")
    private interface Service21 {
        @Get("http://localhost/bytes/100")
        @ExpectedResponses({200})
        void getBytes100(Callback<Response<byte[]>> callback);
    }

    @Test
    public void service21GetBytes100() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<byte[]> cbResult = new CallbackResult<>();

        createService(Service21.class).getBytes100(new Callback<Response<byte[]>>() {
            @Override
            public void onSuccess(Response<byte[]> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service21GetBytes100");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<byte[]> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            byte[] bytes = response.getValue();
            assertNotNull(bytes);
            assertEquals(100, bytes.length);
        }
    }


    @Host("http://localhost")
    @ServiceInterface(name = "DownloadService")
    interface DownloadService {
        @Get("/bytes/30720")
        void getBytes(Callback<StreamResponse> callback);

        @Get("/bytes/30720")
        void getBytesStream(Callback<Response<InputStream>> callback);
    }

    @Test
    public void simpleDownloadStreamResponseTest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<InputStream> cbResult = new CallbackResult<>();

        createService(DownloadService.class).getBytes(new Callback<StreamResponse>() {
            @Override
            public void onSuccess(StreamResponse response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "simpleDownloadStreamResponseTest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<InputStream> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            InputStream stream = response.getValue();
            assertNotNull(stream);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = stream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (IOException e) {
                fail(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    fail(e);
                }
            }
            byte[] byteArray = buffer.toByteArray();
            assertEquals(30720, byteArray.length);
        }
    }

    @Test
    public void simpleDownloadResponseTest() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<InputStream> cbResult = new CallbackResult<>();

        createService(DownloadService.class).getBytesStream(new Callback<Response<InputStream>>() {
            @Override
            public void onSuccess(Response<InputStream> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "simpleDownloadResponseTest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<InputStream> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            InputStream stream = response.getValue();
            assertNotNull(stream);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = stream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (IOException e) {
                fail(e);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    fail(e);
                }
            }
            byte[] byteArray = buffer.toByteArray();
            assertEquals(30720, byteArray.length);
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "FluxUploadService")
    interface BytesUploadService {
        @Put("/put")
        void put(@BodyParam("text/plain") byte[] content,
                 @HeaderParam("Content-Length") long contentLength,
                 Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void bytesUploadTest() throws Exception {
        byte[] reqContent = "The quick brown fox jumps over the lazy dog".getBytes();

        final HttpClient httpClient = createHttpClient();
        // Scenario: Log the body so that body buffering/replay behavior is exercised.
        //
        // Order in which policies applied will be the order in which they added to builder
        //
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new PortPolicy(getWireMockPort(), true),
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)))
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        RestProxy.create(BytesUploadService.class, httpPipeline, SERIALIZER)
            .put(reqContent, reqContent.length, new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "bytesUploadTest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            assertEquals("The quick brown fox jumps over the lazy dog", response.getValue().data());
        }
    }

    @Host("{url}")
    @ServiceInterface(name = "Service22")
    interface Service22 {
        @Get("/")
        void getBytes(@HostParam("url") String url, Callback<Response<byte[]>> callback);
    }

    @Test
    public void service22GetBytes() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<byte[]> cbResult = new CallbackResult<>();

        createService(Service22.class).getBytes("http://localhost/bytes/27", new Callback<Response<byte[]>>() {
            @Override
            public void onSuccess(Response<byte[]> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "simpleDownloadResponseTest");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<byte[]> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            byte[] bytes = response.getValue();
            assertNotNull(bytes);
            assertEquals(27, bytes.length);
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service23")
    interface Service23 {
        @Get("bytes/28")
        void getBytes(Callback<Response<byte[]>> callback);
    }

    @Test
    public void service23GetBytes() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<byte[]> cbResult = new CallbackResult<>();

        createService(Service23.class).getBytes(new Callback<Response<byte[]>>() {
            @Override
            public void onSuccess(Response<byte[]> response) {
                cbResult.response = response;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                cbResult.error = error;
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "service23GetBytes");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<byte[]> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            byte[] bytes = response.getValue();
            assertNotNull(bytes);
            assertEquals(28, bytes.length);
        }
    }


    @Host("http://localhost")
    @ServiceInterface(name = "Service24")
    interface Service24 {
        @Put("put")
        void put(@HeaderParam("ABC") Map<String, String> headerCollection, Callback<Response<HttpBinJSON>> callback);
    }

    @Test
    public void service24Put() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinJSON> cbResult = new CallbackResult<>();

        final Map<String, String> headerCollection = new HashMap<>();
        headerCollection.put("DEF", "GHIJ");
        headerCollection.put("123", "45");

        createService(Service24.class)
            .put(headerCollection, new Callback<Response<HttpBinJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });


        awaitOnLatch(latch, "service24Put");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinJSON> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            HttpBinJSON result = response.getValue();
            assertNotNull(result.headers());
            final HttpHeaders resultHeaders = new HttpHeaders(result.headers());
            assertEquals("GHIJ", resultHeaders.getValue("ABCDEF"));
            assertEquals("45", resultHeaders.getValue("ABC123"));
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "Service26")
    interface Service26 {
        @Post("post")
        void postForm(@FormParam("custname") String name,
                      @FormParam("custtel") String telephone,
                      @FormParam("custemail") String email,
                      @FormParam("size") HttpBinFormDataJSON.PizzaSize size,
                      @FormParam("toppings") List<String> toppings,
                      Callback<Response<HttpBinFormDataJSON>> callback);

        @Post("post")
        void postEncodedForm(@FormParam("custname") String name,
                             @FormParam("custtel") String telephone,
                             @FormParam(value = "custemail", encoded = true) String email,
                             @FormParam("size") HttpBinFormDataJSON.PizzaSize size,
                             @FormParam("toppings") List<String> toppings,
                             Callback<Response<HttpBinFormDataJSON>> callback);
    }


    @Test
    public void postUrlForm() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinFormDataJSON> cbResult = new CallbackResult<>();

        Service26 service = createService(Service26.class);
        service.postForm("Foo",
            "123",
            "foo@bar.com",
            HttpBinFormDataJSON.PizzaSize.LARGE,
            Arrays.asList("Bacon", "Onion"),
            new Callback<Response<HttpBinFormDataJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinFormDataJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });


        awaitOnLatch(latch, "postUrlForm");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinFormDataJSON> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            HttpBinFormDataJSON result = response.getValue();
            assertNotNull(result);
            assertNotNull(result.form());
            assertEquals("Foo", result.form().customerName());
            assertEquals("123", result.form().customerTelephone());
            assertEquals("foo%40bar.com", result.form().customerEmail());
            assertEquals(HttpBinFormDataJSON.PizzaSize.LARGE, result.form().pizzaSize());
            assertEquals(2, result.form().toppings().size());
            assertEquals("Bacon", result.form().toppings().get(0));
            assertEquals("Onion", result.form().toppings().get(1));
        }
    }

    @Test
    public void postUrlFormEncoded() {
        CountDownLatch latch = new CountDownLatch(1);
        CallbackResult<HttpBinFormDataJSON> cbResult = new CallbackResult<>();

        Service26 service = createService(Service26.class);
        service.postEncodedForm("Foo",
            "123",
            "foo@bar.com",
            HttpBinFormDataJSON.PizzaSize.LARGE,
            Arrays.asList("Bacon", "Onion"), new Callback<Response<HttpBinFormDataJSON>>() {
                @Override
                public void onSuccess(Response<HttpBinFormDataJSON> response) {
                    cbResult.response = response;
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable error) {
                    cbResult.error = error;
                    latch.countDown();
                }
            });

        awaitOnLatch(latch, "postUrlFormEncoded");

        if (cbResult.error != null) {
            Assertions.fail(cbResult.error);
        } else {
            final Response<HttpBinFormDataJSON> response = cbResult.response;
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            HttpBinFormDataJSON result = response.getValue();
            assertNotNull(result);
            assertNotNull(result.form());
            assertEquals("Foo", result.form().customerName());
            assertEquals("123", result.form().customerTelephone());
            assertEquals("foo@bar.com", result.form().customerEmail());
            assertEquals(HttpBinFormDataJSON.PizzaSize.LARGE, result.form().pizzaSize());

            assertEquals(2, result.form().toppings().size());
            assertEquals("Bacon", result.form().toppings().get(0));
            assertEquals("Onion", result.form().toppings().get(1));
        }
    }

    // Helpers
    protected <T> T createService(Class<T> serviceClass) {
        final HttpClient httpClient = createHttpClient();
        return createService(serviceClass, httpClient);
    }

    protected <T> T createService(Class<T> serviceClass, HttpClient httpClient) {
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(new PortPolicy(getWireMockPort(), true))
            .httpClient(httpClient)
            .build();

        return RestProxy.create(serviceClass, httpPipeline, SERIALIZER);
    }

    private static void assertMatchWithHttpOrHttps(String url1, String url2) {
        final String s1 = "http://" + url1;
        if (s1.equalsIgnoreCase(url2)) {
            return;
        }
        final String s2 = "https://" + url1;
        if (s2.equalsIgnoreCase(url2)) {
            return;
        }
        fail("'" + url2 + "' does not match with '" + s1 + "' or '" + s2 + "'.");
    }

    private static final SerdeAdapter SERIALIZER = new JacksonSerderAdapter();
}
