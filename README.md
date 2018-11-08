# Azure.Android [![Build Status](https://travis-ci.org/Azure/Azure.Android.svg?branch=master)](https://travis-ci.org/Azure/Azure.Android) [![Build Status](https://dev.azure.com/MobileAzure/Azure.Android/_apis/build/status/Azure.Android)](https://dev.azure.com/MobileAzure/Azure.Android/_build/latest?definitionId=1)

Azure.Android is a collection of SDKs for rapidly creating Android apps with modern, highly-scalable backends on Azure.


# SDKs

## [AzureData](azuredata) ![Current State: Preview Release](https://img.shields.io/badge/Current_State-Preview_Release-brightgreen.svg)[ ![Download](https://api.bintray.com/packages/azure/Azure.Android/azuredata/images/download.svg) ](https://bintray.com/azure/Azure.Android/azuredata/_latestVersion)

AzureData API Reference and samples can be found on the [AzureData readme](azuredata).

AzureData is an SDK for interfacing with [Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-introduction) - A schema-less JSON database engine with rich SQL querying capabilities. It currently supports the full SQL (DocumentDB) API, and offline persistence (including read/write).


## [AzureCore](azurecore) ![Current State: Preview Release](https://img.shields.io/badge/Current_State-Preview_Release-brightgreen.svg)[ ![Download](https://api.bintray.com/packages/azure/Azure.Android/azurecore/images/download.svg) ](https://bintray.com/azure/Azure.Android/azurecore/_latestVersion)

AzureCore API Reference and samples can be found on the [AzureCore readme](azurecore).

AzureCore is a shared dependency of the other four SDKs. It will include the following functionality:
- Secure Storage
- Preference Storage
- Reachability
- Logging
- Encoding
- User Agent Generation
- Base Errors

More information on the features to be included in AzureCore can be found the [Requirements](https://github.com/Azure/Azure.Android/wiki/Requirements-AzureCore) wiki document.


## [AzureMobile](azuremobile) ![Current State: Development](https://img.shields.io/badge/Current_State-Development-blue.svg)[ ![Download](https://api.bintray.com/packages/azure/Azure.Android/azuremobile/images/download.svg) ](https://bintray.com/azure/Azure.Android/azuremobile/_latestVersion)

AzureMobile is an SDK that connects to services deployed using [Azure.Mobile](https://aka.ms/mobile).


## [AzureAuth](azureauth) ![Current State: Development](https://img.shields.io/badge/Current_State-Development-blue.svg)[ ![Download](https://api.bintray.com/packages/azure/Azure.Android/azureauth/images/download.svg) ](https://bintray.com/azure/Azure.Android/azureauth/_latestVersion)

AzureAuth API Reference and samples can be found on the [AzureAuth readme](azureauth).

AzureAuth is an SDK that enables authentication with popular identity providers SDKs to be used to securely access backend services on [Azure App Service](https://docs.microsoft.com/en-us/azure/app-service/app-service-authentication-overview). AzureAuth will support five identity providers out of the box: Azure Active Directory, Facebook, Google, Microsoft Account, and Twitter. Your app can use any number of these identity providers to provide your users with options for how they sign in.

Azure App Service uses federated identity, in which a third-party identity provider stores accounts and authenticates users. The application relies on the provider's identity information so that the app doesn't have to store that information itself. 

More information on the features to be included in AzureAuth can be found the [Requirements](https://github.com/Azure/Azure.Android/wiki/Requirements-AzureAuth) wiki document.


## [AzurePush](azurepush) ![Current State: Development](https://img.shields.io/badge/Current_State-Development-blue.svg)[ ![Download](https://api.bintray.com/packages/azure/Azure.Android/azurepush/images/download.svg) ](https://bintray.com/azure/Azure.Android/azurepush/_latestVersion)

AzurePush API Reference and samples can be found on the [AzurePush readme](azurepush).

AzurePush will provide push notification functionality.  The current SDK for Azure Notification Hubs can be found [here](https://github.com/Azure/azure-notificationhubs/tree/master/Android/notification-hubs-sdk). The intent is to migrate that SDK to this repository, update it, and refactor the API to ensure it works seamlessly with the other SDKs in this project to provide the best possible developer experience.


More information on the features to be included in AzureData can be found the [Requirements](https://github.com/Azure/Azure.Android/wiki/Requirements-AzurePush) wiki document.


## [AzureStorage](azurestorage) ![Current State: Requirements](https://img.shields.io/badge/Current_State-Requirements-red.svg)

AzureStorage API Reference and samples can be found on the [AzureStorage readme](azurestorage).

AzureStorage will provide cloud storage functionality.  The current SDK for Azure Storage can be found [here](https://github.com/Azure/azure-storage-android). The intent is to migrate that SDK to this repository, update it, and refactor the API to ensure it works seamlessly with the other SDKs in this project to provide the best possible developer experience.

More information on the features to be included in AzureStorage can be found the [Requirements](https://github.com/Azure/Azure.Android/wiki/Requirements-AzureStorage) wiki document.


# Installation

## Gradle

The Azure.Android SDKs are packaged and available via [BinTray](https://bintray.com/azure/Azure.Android) and [JCenter](https://bintray.com/bintray/jcenter), so it's easy to add the SDKs to your Android project via Gradle.

To integrate any of the Azure.Android packages into your project, specify it in your [Gradle file](https://developer.android.com/studio/build/dependencies), e.g.:

```
compile 'com.azure.android:azuredata:0.1.0'
```

The correct Gradle package specifier can be found by [navigating to the package](https://bintray.com/azure/Azure.Android) or clicking the above "Download" button for the SDK.  Then view the Maven/Gradle information and copy the Gradle snippet into your Gradle file.

# Getting Started

Once you [add the SDKs to your project](#installation)...

// coming soon


# About
This project is in active development and will change. As the SDKs become ready for use, they will be versioned and released.

We will do our best to conduct all development openly by posting detailed [requirements](https://github.com/Azure/Azure.Android/wiki/Requirements) and managing the project using [issues](https://github.com/Azure/Azure.Android/issues), [milestones](https://github.com/Azure/Azure.Android/milestones), and [projects](https://github.com/Azure/Azure.Android/projects).

## Contributing
This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).  
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Reporting Security Issues
Security issues and bugs should be reported privately, via email, to the Microsoft Security Response Center (MSRC) at [secure@microsoft.com](mailto:secure@microsoft.com). You should receive a response within 24 hours. If for some reason you do not, please follow up via email to ensure we received your original message. Further information, including the [MSRC PGP](https://technet.microsoft.com/en-us/security/dn606155) key, can be found in the [Security TechCenter](https://technet.microsoft.com/en-us/security/default).

## License
Copyright (c) Microsoft Corporation. All rights reserved.  
Licensed under the MIT License.  See [LICENSE](License) for details.
