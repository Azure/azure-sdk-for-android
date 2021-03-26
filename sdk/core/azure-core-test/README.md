# Azure Core Test shared library for Android

Library containing core classes used to test Azure SDK client libraries.

* Create live or playback JUnit tests using [TestBase][TestBase.java] and
  leveraging [InterceptorManager][InterceptorManager.java] to keep track of
  network calls.
* Record network calls using using pipeline policy,
  [RecordNetworkCallPolicy][RecordNetworkCallPolicy.java].
* Playback test session records with [PlaybackClient][PlaybackClient.java].

## Getting started

To use this package, add the following to your _pom.xml_.

[//]: # ({x-version-update-start;com.azure:azure-core-test;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-test</artifactId>
    <version>1.0.0-beta.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

* [TestBase][TestBase.java]: Base test class that creates an `InterceptorManager` for each unit test and either plays
  back test session data or records the test session. Each session's file name is determined using the name of the test.
* [InterceptorManager][InterceptorManager.java]: Main class used to record and playback `NetworkCallRecords`.

## Examples

### Create a test class to run live or playback tests

Use [TestBase][TestBase.java] to easily create live and playback test cases. Extending from `TestBase` provides an
`interceptorManager` that keeps track of all network calls.

```java
/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class SessionTests extends TestBase {

    /**
     * Use JUnit or TestNG annotation here for your testcase
     */
    public void fooTest() {
        // Do some network calls.
    }
}
```

### Record network calls

Record network calls using [RecordNetworkCallPolicy][RecordNetworkCallPolicy.java]. Each HTTP request sent from the test
client, is persisted to [RecordedData][RecordedData.java].

```java
/**
 * Sample code for recording network calls.
 */
public class Foo {
    public void recordNetworkCalls() {
        // All network calls are kept in the recordedData variable.
        RecordedData recordedData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RecordNetworkCallPolicy(recordedData))
            .build();

        // Send requests through the HttpPipeline.
        pipeline.send(new HttpRequest(HttpMethod.GET, "http://bing.com"));

        // Get a record that was sent through the pipeline.
        NetworkCallRecord networkCall = recordedData.findFirstAndRemoveNetworkCall(record -> {
            return record.getUri().equals("http://bing.com");
        });
    }
}
```

### Playback session records

Playback test session records by creating a [RecordedData][RecordedData.java].

```java
/**
 * Sample code for using playback to test.
 */
public class FooBar {
    public void playbackNetworkCalls() {
        RecordedData recordedData = new RecordedData();

        // Add some network calls to be replayed by playbackClient

        // Creates a HTTP client that plays back responses in recordedData.
        HttpClient playbackClient = new PlaybackClient(recordedData, null);

        // Send an HTTP GET request to http://bing.com. If recordedData contains a NetworkCallRecord with a matching HTTP
        // method and matching URL, it is returned as a response.
        Mono<HttpResponse> response = playbackClient.send(new HttpRequest(HttpMethod.GET, "http://bing.com"));
    }
}
```

## Troubleshooting

If you encounter any bugs, please file issues via [GitHub Issues](https://github.com/Azure/azure-sdk-for-android/issues/new/choose)
or checkout [StackOverflow for Azure Android SDK](https://stackoverflow.com/questions/tagged/azure-android-sdk).
## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in
[Microsoft Azure Projects Contribution Guidelines](https://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- links -->
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core%2FREADME.png)
