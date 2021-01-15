# Azure SDK for Android

This repository is for active development of the Azure SDK for Android. For consumers of the SDK we recommend visiting our versioned [developer docs](https://azure.github.io/azure-sdk-for-android).

> Note: The Azure SDK for Android replaces a previous offering, known as Azure.Android. Source code and documentation for Azure.Android is available in the [Azure.Android](https://github.com/Azure/azure-sdk-for-android/tree/Azure.Android) branch.

## Getting started

For your convenience, each service has a separate set of libraries that you can choose to use instead of one, large Azure package. To get started with a specific library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk` directory.

### Prerequisites

* The client libraries natively target Android API level 21. Your application's `minSdkVersion` must be set to 21 or higher to use these libraries.
* The libraries are written in Java 8. Your application must be built with Android Gradle Plugin 3.0.0 or later, and must be configured to [enable Java 8 language desugaring](https://developer.android.com/studio/write/java8-support.html#supported_features) to use these libraries. Java 8 language features that require a target API level >21 are not used, nor are any Java 8+ APIs that would require the Java 8+ API desugaring provided by Android Gradle plugin 4.0.0.
* You must have an [Azure subscription](https://azure.microsoft.com/free/) to use these libraries.

### Libraries available

Currently, the client libraries are in **beta**. These libraries follow the [Azure SDK Design Guidelines for Android](https://azure.github.io/azure-sdk/android_introduction.html) and share a number of core features such as HTTP retries, logging, transport protocols, authentication protocols, etc., so that once you learn how to use these features in one client library, you will know how to use them in other client libraries. You can learn about these shared features in [azure-core](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/core/azure-core/README.md).

The following libraries are currently in **beta**:

#### Core
- [azure-core](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/core/azure-core): 1.0.0-beta.3

#### Azure Communication Services
- [azure-communication-calling](https://search.maven.org/artifact/com.azure.android/azure-communication-calling): 1.0.0-beta.2
- [azure-communication-chat](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/communication/azure-communication-chat): 1.0.0-beta.2
- [azure-communication-common](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/communication/azure-communication-common): 1.0.0-beta.1

> Note: The SDK is currently in **beta**. The API surface and feature sets are subject to change at any time before they become generally available. We do not currently recommend them for production use.

### Install the libraries
To install the Azure client libraries for Android, add them as dependencies within your
[Gradle](#add-a-dependency-with-gradle) or
[Maven](#add-a-dependency-with-maven) build scripts.

#### Add a dependency with Gradle
To import one or more client libraries into your project using the [Gradle](https://gradle.org/) build system, follow the instructions in [Add build dependencies](https://developer.android.com/studio/build/dependencies):

For each library you wish to use, add an `implementation` configuration to the `dependencies` block of your app's `build.gradle` or `build.gradle.kts` file, specifying the library's name and version:

```gradle
// build.gradle
dependencies {
    ...
    implementation "com.azure.android:azure-communication-chat:1.0.0-beta.2"
}

// build.gradle.kts
dependencies {
    ...
    implementation("com.azure.android:azure-communication-chat:1.0.0-beta.2")
}
```

#### Add a dependency with Maven
To import one or more client libraries into your project using the [Maven](https://maven.apache.org/) build system, add them to the `dependencies` section of your app's `pom.xml` file, specifying the artifact ID and version of each library you wish to use:

```xml
<dependency>
  <groupId>com.azure.android</groupId>
  <artifactId>azure-communication-chat</artifactId>
  <version>1.0.0-beta.2</version>
</dependency>
```

## Need help?

* File an issue via [Github Issues](https://github.com/Azure/azure-sdk-for-android/issues/new/choose).
* Check [previous questions](https://stackoverflow.com/questions/tagged/azure-android-sdk) or ask new ones on StackOverflow using `azure-android-sdk` tag.

### Reporting security issues and security bugs

Security issues and bugs should be reported privately, via email, to the Microsoft Security Response Center (MSRC) <secure@microsoft.com>. You should receive a response within 24 hours. If for some reason you do not, please follow up via email to ensure we received your original message. Further information, including the MSRC PGP key, can be found in the [Security TechCenter](https://www.microsoft.com/msrc/faqs-report-an-issue).

## Contributing
For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-android/blob/master/CONTRIBUTING.md).

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit
https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-android%2FREADME.png)
