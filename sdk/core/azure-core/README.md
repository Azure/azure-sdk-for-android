# Azure core client library for Android

This is the core library for the Azure SDK for Android, containing the HTTP pipeline, as well as a shared set of
components that are used across all client libraries, including pipeline policies, error types, serialization classes,
and a logging system. As an end user, you don't need to manually install azure-core because it will be installed
automatically when you install other SDK libraries. If you are a client library developer, please reference the
[azure-communication-chat](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/communication/azure-communication-chat)
library as an example of how to use the shared azure-core components in your client library.

[Source code](https://github.com/Azure/azure-sdk-for-android/tree/master/sdk/core/azure-core)
| [API reference documentation](https://azure.github.io/azure-sdk-for-android/sdk/core/azure-core/azure-core/index.html)

## Getting started

### Prerequisites
* The client libraries natively target Android API level 21. Your application's `minSdkVersion` must be set to 21 or
  higher to use this library.
* The library is written in Java 8. Your application must be built with Android Gradle Plugin 3.0.0 or later, and must
  be configured to
  [enable Java 8 language desugaring](https://developer.android.com/studio/write/java8-support.html#supported_features)
  to use this library. Java 8 language features that require a target API level >21 are not used, nor are any Java 8+
  APIs that would require the Java 8+ API desugaring provided by Android Gradle plugin 4.0.0.
* You must have an [Azure subscription](https://azure.microsoft.com/free/) to use this library.

### Versions available
The current version of this library is **1.0.0-beta.2**.

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
    implementation "com.azure.android:azure-core:1.0.0-beta.2"
}

// build.gradle.kts
dependencies {
    ...
    implementation("com.azure.android:azure-core:1.0.0-beta.2")
}
```

#### Add a dependency with Maven
To import the library into your project using the [Maven](https://maven.apache.org/) build system, add it to the `dependencies` section of your app's `pom.xml` file, specifying its artifact ID and the version you wish to use:

```xml
<dependency>
  <groupId>com.azure.android</groupId>
  <artifactId>azure-core</artifactId>
  <version>1.0.0-beta.2</version>
</dependency>
```


## Key concepts

The main shared concepts of azure-core (and thus, Azure SDK libraries using azure-core) include:

- Configuring service clients, e.g. policies, logging (`RequestIdInterceptor` et al., `ClientLogger`).
- Accessing HTTP response details (`Response`, `ServiceCall`).
- Exceptions for reporting errors from service requests in a consistent fashion. (`AzureException`).
- Classes for serializing and deserializing common HTTP bodies used by Azure services (`DateTimeSerializer` et al.)

## Examples

See [sample-app-storage](https://github.com/Azure/azure-sdk-for-android/tree/master/samples/sample-app-storage) for an example of using this library.

## Troubleshooting

If you run into issues while using this library, please feel free to
[file an issue](https://github.com/Azure/azure-sdk-for-android/issues/new).

## Next steps

Explore and install
[available Azure SDK libraries](https://github.com/Azure/azure-sdk-for-android/blob/master/README.md#libraries-available).

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For
details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-android%2Fsdk%2Fcore%2Fazure-core%2FREADME.png)
