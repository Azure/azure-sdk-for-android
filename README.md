# Azure.Android [![Build Status](https://travis-ci.org/Azure/Azure.Android.svg?branch=master)](https://travis-ci.org/Azure/Azure.Android)

Azure.Android is a collection of SDKs for rapidly creating Android apps with modern, highly-scalable backends on Azure.

**This project is in active development and will change.**

# SDKs
**Usage and API references for all SDKs can be found on our [wiki](https://github.com/Azure/Azure.Android/wiki)**.


### [AzureData](https://github.com/Azure/Azure.Android/tree/master/azuredata)
![Current State: Development](https://img.shields.io/badge/Current_State-Development-blue.svg)

AzureData is an SDK for interfacing with [Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-introduction) - A schema-less JSON database engine with rich SQL querying capabilities. It currently supports the full SQL (DocumentDB) API, and offline persistence (inluding read/write).


### [AzureCore](https://github.com/Azure/Azure.Android/tree/master/azurecore)
![Current State: Development](https://img.shields.io/badge/Current_State-Development-blue.svg)

AzureCore is a shared dependency of the other four SDKs. It includes functionality like secure storage, reachablility, logging, etc.


### [AzureMobile](https://github.com/Azure/Azure.Android/tree/master/azuremobile)
![Current State: Development](https://img.shields.io/badge/Current_State-Development-blue.svg)

AzureMobile is an SDK that connects to services deployed using [Azure.Mobile](https://aka.ms/mobile).


### [AzureAuth](https://github.com/Azure/Azure.Android/tree/master/azureauth)
![Current State: Development](https://img.shields.io/badge/Current_State-Development-blue.svg)

AzureAuth is an SDK that enables authentication with popular identity providers' SDKs to be used to securely access backend services on [Azure App Service](https://docs.microsoft.com/en-us/azure/app-service/app-service-authentication-overview). It supports five identity providers out of the box: Azure Active Directory, Facebook, Google, Microsoft Account, and Twitter.

### [AzurePush](https://github.com/Azure/Azure.Android/tree/master/azurepush)
![Current State: Requirements](https://img.shields.io/badge/Current_State-Requirements-red.svg)

AzurePush will provide push notification functionality.  The current SDK for Azure Notification Hubs can be found [here](https://github.com/Azure/azure-notificationhubs/tree/master/Android/notification-hubs-sdk). The intent is to migrate that SDK to this repository, update it, and refactor the API to ensure it works seamlessly with the other SDKs in this project to provide the best possible developer experience.


More information on the features to be included in AzureData can be found the [Requirements](https://github.com/Azure/Azure.Android/wiki/Requirements-AzurePush) wiki document.


### [AzureStorage](https://github.com/Azure/Azure.Android/tree/master/azurestorage)
![Current State: Requirements](https://img.shields.io/badge/Current_State-Requirements-red.svg)

AzureStorage will provide cloud storage functionality.  The current SDK for Azure Storage can be found [here](https://github.com/Azure/azure-storage-android). The intent is to migrate that SDK to this repository, update it, and refactor the API to ensure it works seamlessly with the other SDKs in this project to provide the best possible developer experience.


# Installation

Coming soon...


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
