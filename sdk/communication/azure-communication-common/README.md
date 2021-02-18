# Azure Communication Common client library for Android

This package contains common code for Azure Communication Service libraries.

[Source code](https://github.com/Azure/azure-sdk-for-android/tree/master/sdk/communication/azure-communication-common)
| [API reference documentation](https://azure.github.io/azure-sdk-for-android/sdk/communication/azure-communication-common/azure-communication-common/index.html)
| [Product documentation](https://docs.microsoft.com/azure/communication-services/overview)

## Getting started

### Prerequisites
* You must have an [Azure subscription](https://azure.microsoft.com/free/) and a
  [Communication Services resource](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource) to use this library.
* The client libraries natively target Android API level 21. Your application's `minSdkVersion` must be set to 21 or
  higher to use this library.
* The library is written in Java 8. Your application must be built with Android Gradle Plugin 3.0.0 or later, and must
  be configured to
  [enable Java 8 language desugaring](https://developer.android.com/studio/write/java8-support.html#supported_features)
  to use this library. Java 8 language features that require a target API level >21 are not used, nor are any Java 8+
  APIs that would require the Java 8+ API desugaring provided by Android Gradle plugin 4.0.0.

### Versions available
The current version of this library is **1.0.0-beta.5**.

> Note: The SDK is currently in **beta**. The API surface and feature sets are subject to change at any time before they become generally available. We do not currently recommend them for production use.

### Install the library
To install the Azure client libraries for Android, add them as dependencies within your
[Gradle](#add-a-dependency-with-gradle) or
[Maven](#add-a-dependency-with-maven) build scripts.

#### Add a dependency with Gradle
To import the library into your project using the [Gradle](https://gradle.org/) build system, follow the instructions in [Add build dependencies](https://developer.android.com/studio/build/dependencies):

Add an `implementation` configuration to the `dependencies` block of your app's `build.gradle` or `build.gradle.kts` file, specifying the library's name and the version you wish to use:

```gradle
// build.gradle
dependencies {
    ...
    implementation "com.azure.android:azure-communication-common:1.0.0-beta.5"
}

// build.gradle.kts
dependencies {
    ...
    implementation("com.azure.android:azure-communication-common:1.0.0-beta.5")
}
```

#### Add a dependency with Maven
To import the library into your project using the [Maven](https://maven.apache.org/) build system, add it to the `dependencies` section of your app's `pom.xml` file, specifying its artifact ID and the version you wish to use:

```xml
<dependency>
  <groupId>com.azure.android</groupId>
  <artifactId>azure-communication-common</artifactId>
  <version>1.0.0-beta.5</version>
</dependency>
```

### Key concepts

### CommunicationTokenCredential

A `CommunicationTokenCredential` authenticates a user with Communication Services, such as Chat or Calling. It optionally
provides an auto-refresh mechanism to ensure a continuously stable authentication state during communications. User
tokens are created by the application developer using the Communication Administration SDK - once created, they are
provided to the various Communication Services client libraries by way of a `CommunicationTokenCredential` object.

## Examples

The following sections provide several code snippets showing different ways to use a `CommunicationTokenCredential`:

* [Creating a credential with a static token](#creating-a-credential-with-a-static-token)
* [Creating a credential that refreshes with a Callable](#creating-a-credential-that-refreshes-with-a-callable)
* [Creating a credential that refreshes proactively](#creating-a-credential-that-refreshes-proactively)
* [Creating a credential with an initial value that refreshes proactively](#creating-a-credential-with-an-initial-value-that-refreshes-proactively)
* [Getting a token asynchronously](#getting-a-token-asynchronously)
* [Invalidating a credential](#invalidating-a-credential)

### Creating a credential with a static token

```java
CommunicationTokenCredential userCredential = new CommunicationTokenCredential("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjM2MDB9.adM-ddBZZlQ1WlN3pdPBOF5G4Wh9iZpxNP_fSvpF4cWs");
```

### Creating a credential that refreshes with a Callable

Here we pass an imagined `Callable<String>` that makes a network request to retrieve a token string for user Bob. It will be called on a background thread.

```java
Callable<String> tokenRefresher = () -> {
  return fetchtoken();
};

CommunicationTokenCredential userCredential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(tokenRefresher, false, null));
```

### Creating a credential that refreshes proactively

Setting `refreshProactively` to true will call your `Callable<String> tokenRefresher` when the token is close to expiry.

```java
CommunicationTokenCredential userCredential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(tokenRefresher, true, null));
```

### Creating a credential with an initial value that refreshes proactively

Passing `initialToken` is an optional optimization to skip the first call to `Callable<String> tokenRefresher`. You can use this to separate the boot from your application from subsequent token refresh cycles.

```java
CommunicationTokenCredential userCredential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(tokenRefresher, true, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjM2MDB9.adM-ddBZZlQ1WlN3pdPBOF5G4Wh9iZpxNP_fSvpF4cWs"));
```

### Getting a token asynchronously

Calling `getToken()` will return a `Future<AccessToken>`

```java
CommunicationTokenCredential userCredential = new CommunicationTokenCredential(new CommunicationTokenRefreshOptions(tokenRefresher, false, null));
Future<AccessToken> accessTokenFuture = userCredential.getToken();
```

### Invalidating a credential

Each `CommunicationTokenCredential` instance uses a background thread for refreshing the cached token. To free up resources and facilitate garbage collection, `dispose()` must be called when the `CommunicationTokenCredential` instance is no longer used.

```java
userCredential.dispose();
```

## Troubleshooting

If you run into issues while using this library, please feel free to
[file an issue](https://github.com/Azure/azure-sdk-for-android/issues/new).

## Next steps
* Read more about Communication [user access tokens](https://docs.microsoft.com/azure/communication-services/concepts/authentication).

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For
details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-android%2Fsdk%2Fcommunication%2Fazure-communication-common%2FREADME.png)
