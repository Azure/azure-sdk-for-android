# Azure Communication Chat Service client library for Android

This package contains the Chat client library for Azure Communication Services.

[Source code](https://github.com/Azure/azure-sdk-for-android/tree/master/sdk/communication/azure-communication-chat)
| [API reference documentation](https://azure.github.io/azure-sdk-for-android/sdk/communication/azure-communication-chat/azure-communication-chat/index.html)
| [Product documentation](https://docs.microsoft.com/azure/communication-services/overview)

# Getting started

## Prerequisites

* You must have an [Azure subscription](https://azure.microsoft.com/free/) and a [Communication Services resource](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource) to use this library.
* The client libraries natively target Android API level 21. Your application's minSdkVersion must be set to 21 or higher to use this library.
* The library is written in Java 8. Your application must be built with Android Gradle plugin 3.0.0 or later, and must be configured to [enable Java 8 language desugaring](https://developer.android.com/studio/write/java8-support.html#supported_features) to use this library. Java 8 language features that require a target API level > 21 are not used, nor are any Java 8+ APIs that would require the Java 8+ API desugaring provided by Android Gradle plugin 4.0.0.

### Versions available
The current Azure Communication Chat Service Version is **2021-01-27-preview4**.

The current Azure Communication Chat SDK Version is **1.0.0-beta.6**.

> Note: The SDK is currently in **beta**. The API surface and feature sets are subject to change at any time before they become generally available. We do not currently recommend them for production use.

### Install the library
To install the Azure client libraries for Android, add them as dependencies within your
[Gradle](#add-a-dependency-with-gradle) or
[Maven](#add-a-dependency-with-maven) build files.

#### Add a dependency with Gradle
To import the library into your project using the [Gradle](https://gradle.org/) build system, follow the instructions in [Add build dependencies](https://developer.android.com/studio/build/dependencies):

Add an `implementation` configuration to the `dependencies` block of your app's `build.gradle` or `build.gradle.kts` file, specifying the library's name and the version you wish to use:

```gradle
// build.gradle
dependencies {
    ...
    implementation "com.azure.android:azure-communication-chat:1.0.0-beta.6"
}

// build.gradle.kts
dependencies {
    ...
    implementation("com.azure.android:azure-communication-chat:1.0.0-beta.6")
}
```

#### Add a dependency with Maven
To import the library into your project using the [Maven](https://maven.apache.org/) build system, add it to the `dependencies` section of your app's `pom.xml` file, specifying its artifact ID and the version you wish to use:

```xml
<dependency>
  <groupId>com.azure.android</groupId>
  <artifactId>azure-communication-chat</artifactId>
  <version>1.0.0-beta.6</version>
</dependency>
```

### Create the ChatClient

Use the `ChatAsyncClient.Builder` to configure and create an instance of `ChatAsyncClient`.

```java
import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.core.http.HttpHeader;

final String endpoint = "https://<resource>.communication.azure.com";
final String userAccessToken = "<user_access_token>";

ChatAsyncClient client = new ChatAsyncClient.Builder()
        .endpoint(endpoint)
        .credentialInterceptor(chain -> chain.proceed(chain.request()
                .newBuilder()
                .header(HttpHeader.AUTHORIZATION, "Bearer " + userAccessToken)
                .build()))
        .build();
```

### Create the threadClient

Now that we've created a Chat thread we'll obtain a `threadClient` to perform operations within the thread.

```java
ChatThreadAsyncClient threadClient =
        new ChatThreadAsyncClient.Builder()
                .endpoint(endpoint)
                .credentialInterceptor(chain -> chain.proceed(chain.request()
                        .newBuilder()
                        .header(HttpHeader.AUTHORIZATION, "Bearer " + userAccessToken)
                        .build()))
                .build();
```
Replace `<endpoint>` with your Communication Services endpoint.

## Key concepts

### Users and User Access Tokens

User access tokens enable you to build client applications that directly authenticate to Azure Communication Services. Refer [here](https://docs.microsoft.com/azure/communication-services/quickstarts/access-tokens) to learn how to create a user and issue a User Access Token.

The id for the user created above will be necessary later to add said user as a participant of a new chat thread. 

### Chat Thread

A chat conversation is represented by a chat thread. Each user in the thread is called a thread participant. Thread participants can chat with one another privately in a 1:1 chat or huddle up in a 1:N group chat. 

### Chat operations

Once you initialize an `ChatClient` class, you can perform the following chat operations:

#### Thread Operations

- [Create a thread](#create-a-thread)
- [Get a thread](#get-a-thread)
- [List threads](#list-threads)
- [Update a thread](#update-a-thread)
- [Delete a thread](#delete-a-thread)

#### Message Operations

- [Send a message](#send-a-message)
- [Get a message](#get-a-message)
- [List messages](#list-messages)
- [Update a message](#update-a-message)
- [Delete a message](#delete-a-message)

#### Thread Participant Operations

- [Get thread participants](#get-thread-participants)
- [Add thread participants](#add-thread-participants)
- [Remove a thread participant](#remove-a-thread-participant)

#### Events Operations

- [Send a typing notification](#send-a-typing-notification)
- [Send a read receipt](#send-read-receipt)
- [Get read receipts](#get-read-receipts)

## Examples

#### Create a thread

Use the `create` method to create a thread.

```java
//  The list of ChatParticipant to be added to the thread.
List<ChatParticipant> participants = new ArrayList<>();
// The display name for the thread participant.
String displayName = "initial participant";
participants.add(new ChatParticipant()
        .setId(id)
        .setDisplayName(displayName)
);


// The topic for the thread.
final String topic = "General";
// The model to pass to the create method.
CreateChatThreadRequest thread = new CreateChatThreadRequest()
        .setTopic(topic)
        .setParticipants(participants);

// optional, set a repeat request ID
final String repeatabilityRequestID = "";

client.createChatThread(thread, repeatabilityRequestID, new Callback<CreateChatThreadResult>() {
    public void onSuccess(CreateChatThreadResult result, okhttp3.Response response) {
        ChatThread chatThread = result.getChatThread();
        threadId = chatThread.getId();
        // take further action
        Log.i(TAG, "threadId: " + threadId);
    }

    public void onFailure(Throwable throwable, okhttp3.Response response) {
        // Handle error.
        Log.e(TAG, throwable.getMessage());
    }
});
```

For more examples, please go to [Quickstart](https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-android) doc.

## Troubleshooting

When an error occurs, the client calls the callback's `onFailure` method. You can use the provided `Throwable` to act upon the failure.

```java
client.createChatThread(thread, new Callback<CreateChatThreadResult>() {
    public void onFailure(Throwable throwable, okhttp3.Response response) {
        // Handle error.
    }
});
```

## Next steps

Check the code examples in quickstart doc, create an Android app to test it out.

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-android%2Fsdk%2Fcommunication%2Fazure-communication-chat%2FREADME.png)
