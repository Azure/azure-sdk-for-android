https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/core/azure-core/Test.md
# Azure core client library for Android

This is the core library for the Azure SDK for Android, containing the HTTP pipeline, as well as a shared set of
components that are used across all client libraries, including pipeline policies, error types, serialization classes,
and a logging system. As an end user, you don't need to manually install azure-core because it will be installed
automatically when you install other SDK libraries. If you are a client library developer, please reference the
[azure-storage-blob](https://github.com/Azure/azure-sdk-for-android/tree/master/sdk/storage/azure-storage-blob) library
as an example of how to use the shared azure-core components in your client library.

[Source code](https://github.com/Azure/azure-sdk-for-android/tree/master/sdk/core/azure-core)
| [API reference documentation](https://azure.github.io/azure-sdk-for-android/com.azure.android/azure-core/1.0.0-beta.1)

## Getting started

### Prerequisites
* The client library natively target Android API level 21. Your application's `minSdkVersion` must be set to 21 or
  higher to use this library.
* The library is written in Java 8. Your application must be built with Android Gradle Plugin 3.0.0 or later, and must
  be configured to
  [enable Java 8 language desugaring](https://developer.android.com/studio/write/java8-support.html#supported_features)
  to use this library. Java 8 language features that require a target API level >21 are not used, nor are any Java 8+
  APIs that would require the Java 8+ API desugaring provided by Android Gradle plugin 4.0.0.
* You must have an [Azure subscription](https://azure.microsoft.com/free/) to use this library.

### Install the library
At the present time, to install the Azure core client library for Android you must download the latest
[release](https://github.com/Azure/azure-sdk-for-android/releases) and integrate it into your project manually:

#### Manually integrate the library into your project

To manually integrate this library into your project, first download the latest releases of the following libraries from
the repository's [Releases](https://github.com/Azure/azure-sdk-for-android/releases) page:

* `azure-core`

Place the libraries' .aar files in your application module's `libs` directory, and modify your application module's
`build.gradle` file, updating (or adding) the `fileTree` dependency targeting the `libs` directory to include .aar
files.

If you plan to use the [Microsoft Authentication Library (MSAL) for Android](http://aka.ms/aadv2) in your project, add
it by following the library's
[installation instructions](https://github.com/AzureAD/microsoft-authentication-library-for-android#using-msal).

## Key concepts

The main shared concepts of azure-core (and thus, Azure SDK libraries using azure-core) include:

- Configuring service clients, e.g. policies, logging (`RequestIdInterceptor` et al., `ClientLogger`).
- Accessing HTTP response details (`Response`, `ServiceCall`).
- Exceptions for reporting errors from service requests in a consistent fashion. (`AzureException`).
- Classes for serializing and deserializing common HTTP bodies used by Azure services (`DateTimeSerializer` et al.)

## Examples

TODO

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
