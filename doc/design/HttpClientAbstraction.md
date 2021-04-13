# HTTP Abstraction:

> Note: that this is not reference documentation intended for the SDK consumers, but the content is focused on SDK developers. 

This section covers the design of HTTP abstractions and it's internals, defined in the module `azure-core-http`,  this abstraction enables a pluggable architecture that accepts multiple HTTP client libraries or custom implementations. The section also covers some of the decisions that lead to this design.

## HttpClient API:

```java
HttpClient httpClient = HttpClient.createDefault();

CancellationToken cancellationToken = new CancellationToken();

// request_1
HttpRequest getRequest = new HttpRequest(HttpMethod.GET, "https://httpbin.org/get");

httpClient.send(getRequest, cancellationToken, new HttpCallback() {
    @Override
    public void onSuccess(HttpResponse response) { }

    @Override
    public void onError(Throwable error) { }
});

// request_2
HttpRequest postRequest = new HttpRequest(HttpMethod.POST, "https://httpbin.org/post", new byte[0]);

httpClient.send(postRequest, cancellationToken, new HttpCallback() {
    @Override
    public void onSuccess(HttpResponse response) { }

    @Override
    public void onError(Throwable error) { }
});
```

The HTTP abstractions - `HttpClient`, `HttpRequest` and `HttpResponse` are intended to be implemented by the pluggable components. The Azure Android SDK provides two such pluggable components -  `azure-core-http-okhttp` (uses `okhttp` HTTP library) and `azure-core-http-httpurlconnection` (uses native `java.net.HttpUrlConnection` HTTP library), the pluggability enables the users to bring their implementation if a different HTTP library is desired. 

The SDK enables pluggability by using The Java Service Provider Interface (SPI).

The `HttpClient.createDefault()` uses SPI to locate the first pluggable component in the classpath and creates an instance of that component. 

## HttpPipeline API:

The HTTP pipeline is one of the key components in achieving consistency and diagnosability in the Azure Android client libraries. The pipeline consists of an `HttpClient` and HTTP pipeline policies. Each `HttpRequest` flows through the pipeline's policies before the `HttpClient` sends it to the service; once the `HttpClient` receives the `HttpResponse,` the response will also flow through the policies.

The policies enable monitoring (e.g., logging), rewrite (e.g., setting Authorization header), and retry the HTTP calls.

```java
HttpClient httpClient = HttpClient.createDefault();

HttpPipeline httpPipeline = new HttpPipelineBuilder()
    .policies(new AttachDatePolicy(), AttachAuthPolicy(authClient), new LogPolicy())
    .httpClient(httpClient)
    .build();

CancellationToken cancellationToken = new CancellationToken();
HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "https://httpbin.org/get");

httpPipeline.send(httpRequest, Context.NONE, cancellationToken, new HttpCallback() {
    @Override
    public void onSuccess(HttpResponse response) { }

    @Override
    public void onError(Throwable error) { }
});
```

## The send method in HttpClient and HttpPipeline:

The signature of `send` API in `HttpClient` and `HttpPipeline` is almost identical, except the pipeline `send` API takes `azure.core.util.Context.` The context is an immutable-threadsafe bag to mainly used by SDK developers to pass data to specific policies.

```java
void HttpClient::send(HttpRequest httpRequest,
                      CancellationToken cancellationToken,
                      HttpCallback httpCallback);


void HttpPipeline::send(HttpRequest httpRequest,
                        Context context,
                        CancellationToken cancellationToken,
                        HttpCallback httpCallback)
```

## Accessing the send method parameters in policy:

The `send` API accepts few parameters, which are accessible in each policy for interception.

```java
void HttpPipeline::send(HttpRequest httpRequest,
                        Context context,
                        CancellationToken cancellationToken,
                        HttpCallback httpCallback)
```

The contract `HttpPipelinePolicy` represents a policy, a functional interface with one method, 

`void process(HttpPipelinePolicyChain chain)`.

The chain provides access to all the parameters passed to the pipeline send method. 

```java
final class MyPolicy implements HttpPipelinePolicy {

    @Override
    public void process(HttpPipelinePolicyChain chain) {
        final HttpRequest request = chain.getRequest();
        final Context context = chain.getContext();
        final CancellationToken cancellationToken = chain.getCancellationToken();

        .....
    }
}

```

## intercepting request-response in policy:

### intercept-only-request:

Most of the policies intercept and mutate the request; very few require response interception. The following policy is an example that intercepts only the request.

```java
final class AttachDatePolicy implements HttpPipelinePolicy {
    @Override
    public void process(HttpPipelinePolicyChain chain) {
        HttpRequest httpRequest = chain.getRequest();
        httpRequest.getHeaders().put("Date", OffsetDateTime.now().toString());

        chain.processNextPolicy(httpRequest);
    }
}
```

The `HttpPipelinePolicyChain.processNextPolicy(HttpRequest)` hand over the request to the next policy in the pipeline.

### intercept-[request]-response:

The above section provided a simple policy example. This section covers the policy design in detail, including its comparison with policy in Java SDK and interceptors in OkHttp.

When the pipeline executes, that execution has two flows - forward flow and reverse flow. Each policy is a hook in this flow.

<img width="1474" alt="PipelineFlow" src="https://user-images.githubusercontent.com/1471612/108263452-26909f80-711b-11eb-964c-c2ffdad6a8cb.png">

At a high level, each policy has two roles:

1. Inspect the "data" delivered to the policy hook.
2. After the "data" inspection, tell the pipeline to continue the "flow" (forward or reverse).

We can elaborate these two roles further

1.  Intercept request (Inspect the "data")
2. Call into next-policy (Continue the "flow")
3. Intercept response (Inspect the "data")
4. [Policy completed] Return to previous-policy (Continue the "flow")
            
Below is shown how these four steps look like in typical "OKHttp" and "Java SDK Reactor-based" policy implementation.

### Sync policy (Native OkHttp interceptor)

Even though the native OkHttp client can be used in synchronous and asynchronous mode, the policy/interceptor is synchronous.

```java
public okhttp3.Response intercept(okhttp3.Interceptor.Chain chain) {
    // 1. Intercept request
    okhttp3.Request request = chain.request();

    try {
        // 2. Call into next-policy
        okhttp3.Response response = chain.proceed(request);
        // 3. Intercept response
        log(response.code());
        // 4. [Policy completed] Return to previous-policy
        return response;
    } catch (Throwable error) {
        log(error);
        throw error;
    }
}
```

#### Response content interception:

```java
try {
    // 2. Call into next-policy
    okhttp3.Response response = chain.proceed(request);
     // 3. Intercept response
    okhttp3.Response bufferedResponse = buffer(response);
    inspectContent(bufferedResponse);
     // 4. [Policy completed] Return to previous-policy
    return bufferedResponse;
} catch (Throwable error) {
    log(error);
    throw error;
}
```

#### API call during response interception:

Some policy requires making additional API calls after receiving the response; this is straight forward in case of OkHttp due to the synchronous nature of the policy/interceptor.

```java
try {
    // 2. Call into next-policy
    okhttp3.Response response = chain.proceed(request);
     // 3. Intercept response
    okhttp3.Response fooResponse = client.apiFoo(response);
     // 4. [Policy completed] Return to previous-policy
    return fooResponse;
} catch (Throwable error) {
    log(error);
    throw error;
}
```

</details>

### Async policy (Reactor: Azure Java SDK)

The Java SDK uses the Reactor library to enable async APIs. The pipeline is also async and powered by Reactor.

```java
public Mono<Response> process(PiplelineCallContext context, NextPolicy next) {
    // 1. Intercept request
    HttpRequest request = context.getRequest();

    // 2. Call into next-policy
    Mono<HttpResponse> responseMono = next.process();
    // 3. Intercept response
    responseMono = responseMono
        .doOnNext(response -> log(response.code()))
        .doOnError(error -> log(error));
    // 4. [Policy completed] Return to previous-policy
    return responseMono;
}
```

#### Response content interception:

```java
// 2. Call into next-policy
Mono<HttpResponse> responseMono = next.process();
// 3. Intercept response
responseMono = responseMono
    .map(response -> {
        HttpResponse bufferredResponse = buffer(response);
        inspectContent(bufferredResponse);
        return bufferredResponse;
    })
    .doOnError(error -> log(error));
// 4. [Policy completed] Return to previous-policy
return responseMono;
```

#### API call during response interception:

```java
// 2. Call into next-policy
Mono<HttpResponse> responseMono = next.process();
// 3. Intercept response
responseMono = responseMono
    .flatmap(response -> {
        Mono<HttpResponse> fooResponseMono = client.apiFoo(response);
        return fooResponseMono;
    })
    .doOnError(error -> log(error));
// 4. [Policy completed] Return to previous-policy
return responseMono;
```

### Async policy (CompletableFuture)

Now that we have seen examples for native OkHttp and Java SDK policy implementations, below shown how policy looks had we use `CompletableFuture`. 

> Note that for Android, we want the policy to be async and not to depend on API24 `CompletableFuture` or any external async framework. The below sample is just to get a feeling of structure of CF based policy.

```java
public CompletetableFuture<HttpResponse> intercept(PipelnePolicyChain chain) {
    // 1. Intercept request
    HttpRequest request = chain.request();

    // 2. Call into next-policy
    CompletetableFuture<HttpResponse> responseCF = chain.proceed(request);
    // 3. Intercept response
    responseCF = responseCF.whenComplete((response, error) -> {
        if (response != null) {
            log(response.code()));
        } else {
            log(error);
        }
    });
    // 4. [Policy compeleted] Return to previous-policy
    return responseCF;
}
```

#### Response content interception:

```java
// 2. Call into next-policy
CompletetableFuture<HttpResponse> responseCF = chain.proceed(request);
// 3. Intercept response
responseCF = responseCF.thenCompose((response) -> {
    HttpResponse bufferredResponse = buffer(response);
    inspectContent(bufferredResponse);
    return CompletetableFuture.complete(bufferredResponse);
});
// 4. [Policy compeleted] Return to previous-policy
return responseCF;
```

#### API call during response interception:

```java
// 2. Call into next-policy
CompletetableFuture<HttpResponse> responseCF = chain.proceed(request);
// 3. Intercept response
responseCF = responseCF.thenComposeAsync((response) -> {
    CompletableFuture<HttpResponse> fooResponseCF = client.apiFoo(response);
    return fooResponseCF;
});
// 4. [Policy compeleted] Return to previous-policy
return responseCF;
```

### Async policy (Callback: Azure Android SDK)

This section covers the thought process behind the structure of async policy in the "Azure Android SDK". 

```java
public void process(PipelnePolicyChain chain) {
    // 1. Intercept request
    HttpRequest request = chain.request();

    // 2. Call into next-policy
    chain.proceed(request, new HttpCallback() {
        @Override
        public void onSuccess(HttpResponse response) {
            // 3. Intercept response
            log(response.code());
        }

        @Override
        public void onError(Throwable error) {
            // 3. Intercept error response
            log(error);
        }
    };
}
```

The above sample is async and callback based. For callback, it uses `HttpCallback` type, the same type `HttpClient` and `HttpPipeline` uses, but the design is not addressing how to achieve the 4th step - "[Policy completed] Return to previous-policy".

#### [Policy completed] Return to previous-policy

 :x:

A straight forward approach is to define return type for `onSuccess` and `onError` :--- `Tuple<HttpResponse, Throwable>`

```java
// 2. Call into next-policy
chain.processNextPolicy(request, new NextPolicyCallback() {

    @Override
    public Tuple<HttpResponse, Throwable> onSuccess(HttpResponse response) {
        // 3. Intercept response

        // 4.  [Policy completed] Return to previous-policy
        if (isGood(response)) {
            return new Tuple<>(response, null);
        } else {
            return new Tuple<>(null, new Throwable("boom!"));
        }
    }

    @Override
    public Tuple<HttpResponse, Throwable> onError(Throwable error) {
        // 3. Intercept error response

        // 4.  [Policy completed] Return to previous-policy
        if (shouldUseCache) {
            HttpResponse cachedResponse = cacheStore.get(..);
            return new Tuple<>(cachedResponse, null);
        } else {
            return new Tuple<>(null, error);
        }
    }
});
```

But the design has the following drawbacks;

1. Tuple not great dev experience.
2. Enforces `onSucess | onError` to be synchronous.


#### Limitation of synchronous onSucess | onError

The following code shows the limitation we hit if we force `onSuccess` and `onError` to be synchronous, i.e., we can't make an async call from these methods.

```java
// 2. Call into next-policy
chain.processNextPolicy(request, new NextPolicyCallback() {

    @Override
    public Tuple<HttpResponse, Throwable> onSuccess(HttpResponse response) {
        // async-call
        client.apiFoo(response, new FooCallback() {
            void onFooComplete(BarResponse barResponse) {
                HttpResponse coreResponse = toCoreResponse(barResponse);
            }

            void onFooFailed(Throwable t) {

            }
        });
        // not completed yet!, so can't build Tuple and return.
    }

    @Override
    public Tuple<HttpResponse, Throwable> onError(Throwable error) { 
        return new Tuple<>(null, error); 
    }
}

```

#### PolicyCompleter

So, ideally, the design for the 4th step - "[Policy completed] Return to previous-policy" should cover both sync and async work inside `onSuccess` | `onError`.

#### [Policy completed] Return to previous-policy {sync-completion}

```java
chain.processNextPolicy(request, new NextPolicyCallback() {

    @Override
    public CompletionState onSuccess(HttpResponse response, PolicyCompleter completer) {
        if (isGood(response)) {
            return completer.completed(updatedResponse);
        } else {
            return completer.completedError(new Throwable("boom!"));
        }
    }

    @Override
    public CompletionState onError(Throwable error, PolicyCompleter completer) {
        if (shouldUseCache) {
            Response cachedResponse = cacheStore.get(..);
            return completer.completed(cachedResponse);
        } else {
            return completer.completedError(error);
        }
    }
}
```

#### [Policy completed] Return to previous-policy {async-completion}

For android, we'll offer async methods in azure clients; additionally, many of the external android libraries are async.

Suppose the callback methods "onSuccess" or "onError" implementation calls into another async method say `apiFooAsync`. In this case, it's important to note that only after the async method `apiFooAsync` produces results, then only we should mark the completion of policy. Only after the completion of policy should we resume the previous policy in the pipeline.

This means we need a way to `defer` the policy completion until `apiFooAsync` produces some result.

The `PolicyCompleter` offer a method `defer` for this, which indicate that the completion of policy should be deferred until one of the completion method is called.


```java
chain.processNextPolicy(request, new NextPolicyCallback() {

    @Override
    public CompletionState onSuccess(HttpResponse response, PolicyCompleter completer) {

        client.apiFooAsync(response, new FooCallback() {
            void onFooComplete(BarResponse barResponse) {
                HttpResponse coreResponse = toCoreResponse(barResponse);
                completer.complete(coreResponse);
            }

            void onFooFailed(Throwable t) {
                completer.completeError(t);
            }
        });
        
        return completer.defer(); // completion is deferred
    }

    @Override
    public CompletionState onError(Throwable error, PolicyCompleter completer) {
        if (shouldUseCache) {
            Response cachedResponse = cacheStore.get(..);
            return completer.completed(cachedResponse);
        } else {
            return completer.completedError(error);
        }
    }
}
```

### Out of the box support for retry 

The retry is a mainline scenario; the pipeline policies are split into two groups - the policies before retry and policies after retry.

Android pipeline offers an overload of `processNextPolicy`, which can delay the next policy's execution for a given time. Having the pipeline itself providing this method simplifies the authoring of the retry policy.

Below is a very simplified version of retry that retries only once if the status code is 500. We can also see that completer's `defer` play nicely here i.e. defer the completion of policy until the completion of "scheduled" next policy.

```java
class RetryOncePolicy implements HttpPipelinePolicy {
    @Override
    public void process(HttpPipelinePolicyChain chain) {
        final HttpRequest httpRequest = CopyWithMD5(chain.getRequest());

        chain.processNextPolicy(httpRequest, new HttpCallback() {
            @Override
            public CompletionState onSuccess(HttpResponse response, PolicyCompleter completer) {
                if (response.getStatusCode() == 500) {
                    retryOnceAfter5Sec(chain);
                    //
                    return completer.defer();
                } else {
                    return completer.complete(response);
                }
            }

            @Override
            public CompletionState onError(Throwable error, PolicyCompleter completer) {
                return completer.completedError(error);
            }
        });
    }

    // Do retry once asynchronously.
    private void retryOnceAfter5Sec(HttpPipelinePolicyChain chain) {
        final HttpRequest httpRequest = CopyWithMD5(chain.getRequest());

        chain.processNextPolicy(httpRequest, new HttpCallback() {
            @Override
            public CompletionState onSuccess(HttpResponse response, PolicyCompleter completer) {
                return completer.completed(response);
            }

            @Override
            public CompletionState onError(Throwable error, PolicyCompleter completer) {
                return completer.completedError(error);
            }
        }, 
        5,  // delay
        TimeUnit.SECONDS);
    }

    // Create a copy of given request with MD5 header set.
    private HttpRequest CopyWithMD5(HttpRequest httpRequest) {
        return HttpRequest
          .ciopy()
          .getHeaders()
          .put("Content-MD5", computeMD5(httpRequest));
    }
}
```

## HttpCallDispatcher:

### One HttpClient and multiple HttpPipelines:

<img width="1097" alt="PipelineShareHttpClient" src="https://user-images.githubusercontent.com/1471612/103817024-c6d2af00-501a-11eb-86a9-676f93a59397.png">

### Async HttpPipeline run:

<img width="1440" alt="PipelineHttpDispatcher" src="https://user-images.githubusercontent.com/1471612/103817228-1ca75700-501b-11eb-91ca-9f29194f7047.png">


```java
public interface HttpClient {

    HttpCallDispatcher getHttpCallDispatcher();

    void send(HttpRequest httpRequest,
                      CancellationToken cancellationToken,
                      HttpCallback httpCallback);

    static HttpClient createDefault() {
        return HttpClientProviders.createInstance();
    }
}
```

```java
public final class HttpCallDispatcher {
    public HttpCallDispatcher();
    public HttpCallDispatcher(ExecutorService executorService);
    public HttpCallDispatcher(ExecutorService executorService,
                              ScheduledExecutorService scheduledExecutorService);

    
    public void enqueue(HttpCallFunction httpCallFunction,
                        HttpRequest httpRequest,
                        CancellationToken cancellationToken,
                        HttpCallback httpCallback);
}

```

#### HttpClient(OkHttpClient) and HttpPipeline

<img width="943" alt="OkHttp_Dispatcher_ShareES" src="https://user-images.githubusercontent.com/1471612/103819345-161ade80-501f-11eb-9e0c-ed638b50dab6.png">


#### HttpClient(HttpUrlConnection) and HttpPipeline

<img width="1063" alt="HttpUrlCon_Use_Dispatcher" src="https://user-images.githubusercontent.com/1471612/103819764-eb7d5580-501f-11eb-9c2c-4bb1b4c6547a.png">