# Azure Storage Blobs client library for Android
Azure Blob storage is Microsoft's object storage solution for the cloud. Blob
storage is optimized for storing massive amounts of unstructured data, such as
text or binary data.

Blob storage is ideal for:

* Serving images or documents directly to a browser
* Storing files for distributed access
* Streaming video and audio
* Storing data for backup and restore, disaster recovery, and archiving
* Storing data for analysis by an on-premises or Azure-hosted service

[Source code](https://github.com/Azure/azure-sdk-for-android/tree/master/sdk/storage/azure-storage-blob)
| [API reference documentation](...)
| [Product documentation](https://docs.microsoft.com/azure/storage/)
| [Samples](https://github.com/Azure/azure-sdk-for-android/tree/master/samples/sample-app-storage)

## Getting started
The basic outline of tasks needed to use the client library is as follows:
1. Ensure you've met the [prerequisites](#prerequisites).
2. [Install the library](#install-the-library) into your project.
3. Create an instance of the [client](#create-the-client).
4. Call the appropriate [client methods](#examples) to list containers, and list, download, and upload blobs.

### Prerequisites
* The client library natively target Android API level 21. Your application's `minSdkVersion` must be set to 21 or
  higher to use this library.
* The library is written in Java 8. Your application must be built with Android Gradle Plugin 3.0.0 or later, and must
  be configured to
  [enable Java 8 language desugaring](https://developer.android.com/studio/write/java8-support.html#supported_features)
  to use this library. Java 8 language features that require a target API level >21 are not used, nor are any Java 8+
  APIs that would require the Java 8+ API desugaring provided by Android Gradle plugin 4.0.0.
* You must have an [Azure subscription](https://azure.microsoft.com/free/) to use this library.

#### Create a storage account
If you wish to create a new storage account, you can use the
[Azure Portal](https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal),
[Azure PowerShell](https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-powershell),
or [Azure CLI](https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-cli):

```bash
# Create a new resource group to hold the storage account -
# if using an existing resource group, skip this step
az group create --name my-resource-group --location westus2

# Create the storage account
az storage account create -n my-storage-account-name -g my-resource-group
```

### Install the library
At the present time, to install the Azure Storage Blobs client library for Android you must download the latest
[release](https://github.com/Azure/azure-sdk-for-android/releases) and integrate it into your project manually:

#### Manually integrate the library into your project

To manually integrate this library into your project, first download the latest releases of the following libraries from
the repository's [Releases](https://github.com/Azure/azure-sdk-for-android/releases) page:

* `azure-core`
* `azure-storage-blob`

Place the libraries' .aar files in your application module's `libs` directory, and modify your application module's
`build.gradle` file, updating (or adding) the `fileTree` dependency targeting the `libs` directory to include .aar
files.

If you plan to use the [Microsoft Authentication Library (MSAL) for Android](http://aka.ms/aadv2) in your project, add
it by following the library's
[installation instructions](https://github.com/AzureAD/microsoft-authentication-library-for-android#using-msal).

### Create the client
The Azure Storage Blobs client library for Android allows you to interact with blob storage containers and blobs.
Interaction with these resources starts with an instance of a [client](#client).

TODO

#### Types of clients
To interact with a storage account that permits anonymous read access, you can create an
[anonymous client](#anonymous-clients) that requires no credential.

When interacting with a storage account that doesn't permit anonymous read access, or to perform any write operations,
you'll need to create a client that is authenticated by one of the following credentials, depending on the type of
[authorization](https://docs.microsoft.com/azure/storage/common/storage-auth) you wish to use:

* [Shared Access Signature](#shared-access-signature)
* [Microsoft Authentication Library (MSAL) for Android)](#microsoft-authentication-library-msal-for-android)
* [Storage account shared access key](#storage-account-shared-access-key)

##### Anonymous clients
TODO

##### Shared Access Signature
TODO

##### Microsoft Authentication Library (MSAL) for Android
TODO

##### Storage account shared access key
TODO

#### Looking up the endpoint URL
If you're creating an [anonymous client](#anonymous-clients) or a client that uses the
[Microsoft Authentication Library (MSAL) for Android](#microsoft-authentication-library-msal-for-android), you'll need to
provide the storage account's blob storage endpoint URL. You can find the blob storage endpoint URL using the
[Azure Portal](https://docs.microsoft.com/azure/storage/common/storage-account-overview#storage-account-endpoints),
[Azure PowerShell](https://docs.microsoft.com/powershell/module/az.storage/get-azstorageaccount),
or [Azure CLI](https://docs.microsoft.com/cli/azure/storage/account?view=azure-cli-latest#az-storage-account-show):

```bash
# Get the blob service account url for the storage account
az storage account show -n my-storage-account-name -g my-resource-group --query "primaryEndpoints.blob"
```

TODO

#### Choosing a Restoration ID
TODO

#### Customizing the client
TODO

### Managed transfers
When you use a client's [upload](#downloading-a-blob) or [download](#downloading-a-blob) methods to transfer a blob to
or from the device, the client will perform the operation as a *managed transfer*. During a managed transfer, the
library's transfer management engine will ensure that the transfer is performed reliably in the face of changing network
conditions, pausing transfers when network connectivity is lost and resuming them when connectivity is restored. Managed
transfers can also be paused, resumed, or canceled by the developer at any time. Any managed transfers which have not
been completed when the hosting application terminates are persisted to disk and can be restarted on a subsequent
application launch.

#### Tracking progress of managed transfers
TODO

#### Looking up and manipulating transfers
TODO

#### Parallel execution of transfers
TODO

## Key concepts
The following components make up the Azure Blob Service:
* The storage account itself
* A container within the storage account
* A blob within a container

The Azure Storage Blobs client library for Android allows you to interact with some of these components through the
`StorageBlobClient` client object.

### Client
A single client is provided to interact with the various components of the Blob Service:
1. [StorageBlobClient](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/storage/azure-storage-blob/src/main/java/com/azure/android/storage/blob/StorageBlobClient.java) -
   this client represents interaction with a specific blob container. It provides operations to upload, download,
   delete, and list blobs.

### Blob Types
The `StorageBlobClient` only works with block blobs:
* [Block blobs](https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs#about-block-blobs)
  store text and binary data, up to approximately 4.75 TiB. Block blobs are made up of blocks of data that can be
  managed individually

## Examples
TODO

## Troubleshooting
### General
Storage Blob clients raise exceptions defined in
[Azure Core](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/core/azure-core/src/main/java/com/azure/android/core/exception/AzureException.java).

### Logging
This library uses the
[ClientLogger](https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/core/azure-core/src/main/java/com/azure/android/core/util/logging/ClientLogger.java)
interface for logging. The desired style of logger can be set when initializing the client:

TODO

## Next steps

### More sample code

Get started with our [samples](https://github.com/Azure/azure-sdk-for-android/tree/master/samples/).

Storage Blobs client library for Android samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Storage Blobs:

* [AzureSDKSwiftDemo](https://github.com/Azure/azure-sdk-for-android/tree/master/samples/sample-app-storage) - Example for common Storage Blob tasks:
    * List blobs in a container
    * Upload blobs
    * Download blobs

### Additional documentation
For more extensive documentation on Azure Blob storage, see the
[Azure Blob storage documentation](https://docs.microsoft.com/azure/storage/blobs/) on docs.microsoft.com.

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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-android%2Fsdk%2Fstorage%2Fazure-storage-blob%2FREADME.png)
