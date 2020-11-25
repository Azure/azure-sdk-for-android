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
The current Azure Communication Chat Service Version is **2020-11-01-preview3**.

The current Azure Communication Chat SDK Version is **1.0.0-beta.3**.

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
    implementation "com.azure.android:azure-communication-chat:1.0.0-beta.2"
}

// build.gradle.kts
dependencies {
    ...
    implementation("com.azure.android:azure-communication-chat:1.0.0-beta.2")
}
```

#### Add a dependency with Maven
To import the library into your project using the [Maven](https://maven.apache.org/) build system, add it to the `dependencies` section of your app's `pom.xml` file, specifying its artifact ID and the version you wish to use:

```xml
<dependency>
  <groupId>com.azure.android</groupId>
  <artifactId>azure-communication-chat</artifactId>
  <version>1.0.0-beta.3</version>
</dependency>
```

### Create the AzureCommunicationChatClient

Use the `AzureCommunicationChatServiceAsyncClient.Builder` to configure and create an instance of `AzureCommunicationChatClient`.

```java
import com.azure.android.communication.chat.AzureCommunicationChatServiceAsyncClient;
import com.azure.android.core.http.HttpHeader;

final String endpoint = "https://<resource>.communication.azure.com";
final String userAccessToken = "<user_access_token>";

AzureCommunicationChatServiceAsyncClient client = new AzureCommunicationChatServiceAsyncClient.Builder()
    .endpoint(endpoint)
    .credentialInterceptor(chain -> chain.proceed(chain.request()
        .newBuilder()
        .header(HttpHeader.AUTHORIZATION, userAccessToken)
        .build());
```

## Key concepts

### Users and User Access Tokens

User access tokens enable you to build client applications that directly authenticate to Azure Communication Services. Refer [here](https://docs.microsoft.com/azure/communication-services/quickstarts/access-tokens) to learn how to create a user and issue a User Access Token.

The id for the user created above will be necessary later to add said user as a participant of a new chat thread. 

### Chat Thread

A chat conversation is represented by a chat thread. Each user in the thread is called a thread participant. Thread participants can chat with one another privately in a 1:1 chat or huddle up in a 1:N group chat. 

### Chat operations

Once you initialize an `AzureCommunicationChatClient` class, you can perform the following chat operations:

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

### Thread Operations

#### Create a thread

Use the `create` method to create a thread.

```java
//  The list of ChatParticipant to be added to the thread.
List<ChatParticipant> participants = new ArrayList<>();
// The communication user ID you created before, required.
final String id = "<user_id>";
// The display name for the thread participant.
final String displayName = "initial participant";
participants.add(new ChatParticipant()
    .setId(id)
    .setDisplayName(displayName));

// The topic for the thread.
final String topic = "General";
// The model to pass to the create method.
CreateChatThreadRequest thread = new CreateChatThreadRequest()
    .setTopic(topic)
    .setParticipants(participants);

client.createChatThread(thread, new Callback<MultiStatusResponse>() {
    public void onSuccess(MultiStatusResponse result, okhttp3.Response response) {
        // MultiStatusResponse is the result returned from creating a thread.
        // It has a 'multipleStatus' property which represents a list of IndividualStatusResponse.
        String threadId;
        List<IndividualStatusResponse> statusList = result.getMultipleStatus();
        for (IndividualStatusResponse status : statusList) {
            if (status.getId().endsWith("@thread.v2")
                && status.getType().contentEquals("Thread")) {
                threadId = status.getId();
                break;
            }
        }
        // Take further action.
    }

    public void onFailure(Throwable throwable, okhttp3.Response response) {
        // Handle error.
    }
});
```

#### Get a thread

Use the `getChatThread` method to retrieve a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.getChatThread(threadId, new Callback<ChatThread>() {
    @Override
    public void onSuccess(ChatThread thread, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### List threads

Use the `listChatThreads` method to retrieve a list of threads.

```java
// The maximum number of messages to be returned per page, optional.
final int maxPageSize = 10;
// The thread start time to consider in the query, optional.
final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-08T00:00:00Z");
client.listChatThreadsPages(maxPageSize, startTime,
    new Callback<AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>>>() {
    @Override
    public void onSuccess(AsyncPagedDataCollection<ChatThreadInfo,
        Page<ChatThreadInfo>> pageCollection,
        Response response) {
        // pageCollection enables enumerating a list of threads.
        pageCollection.getFirstPage(new Callback<Page<ChatThreadInfo>>() {
            @Override
            public void onSuccess(Page<ChatThreadInfo> firstPage, Response response) {
                for (ChatThreadInfo thread : firstPage.getItems()) {
                    // Take further action.
                }
                retrieveNextThreadPages(firstPage.getPageId(), pageCollection);
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                // Handle error.
            }
        });
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});

void retrieveNextThreadPages(String nextPageId,
    AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ChatThreadInfo>>() {
        @Override
        public void onSuccess(Page<ChatThreadInfo> nextPage, Response response) {
            for (ChatThreadInfo thread : nextPage.getItems()) {
                // Take further action.
            }
            if (nextPage.getPageId() != null) {
                retrieveNextThreadPages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // Handle error.
        }
    });
}
```

#### Update a thread

Use the `update` method to update a thread's properties.

```java
// The new topic for the thread.
final String topic = "updated topic";
// The model to pass to the update method.
UpdateTopicRequest thread = new UpdateTopicRequest()
    .setTopic(topic);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.updateTopic(threadId, thread, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### Delete a thread

Use the `deleteChatThread` method to delete a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.deleteChatThread(threadId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

### Message Operations

#### Send a message

Use the `send` method to send a message to a thread.
```java
// The chat message content, required.
final String content = "Test message 1";
// The display name of the sender, if null (i.e. not specified), an empty name will be set.
final String senderDisplayName = "An important person";
// The message priority level, such as 'NORMAL' or 'HIGH', 
// if null (i.e. not specified), 'NORMAL' will be set.
final ChatMessagePriority priority = ChatMessagePriority.HIGH;
SendChatMessageRequest message = new SendChatMessageRequest()
    .setPriority(priority)
    .setContent(content)
    .setSenderDisplayName(senderDisplayName);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.sendChatMessage(threadId, message, new Callback<SendChatMessageResult>() {
    @Override
    public void onSuccess(String result, Response response) {
        // A string is the response returned from sending a message, it is an id, 
        // which is the unique ID of the message.
        final String chatMessageId = result;
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### Get a message

Use the `getChatMessage` method to retrieve a message in a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the message.
final String chatMessageId = "<message_id>";

client.getChatMessage(threadId, chatMessageId, new Callback<ChatMessage>() {
    @Override
    public void onSuccess(ChatMessage result, Response response) {
        // `ChatMessage` is the response returned from getting a message.
        final String content = result.getContent();
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### List messages

Use the `listChatMessages` method to retrieve messages in a thread.

```java
// The maximum number of messages to be returned per page, optional.
final int maxPageSize = 10;
// The thread start time to consider in the query, optional.
final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-08T00:00:00Z");
// The unique ID of the thread.
final String threadId = "<thread_id>";
        
client.listChatMessagesPages(threadId,
    maxPageSize,
    startTime,
    new Callback<AsyncPagedDataCollection<ChatMessage, Page<ChatMessage>>>() {
        @Override
        public void onSuccess(AsyncPagedDataCollection<ChatMessage, Page<ChatMessage>> pageCollection,
            Response response) {
            // pageCollection enables enumerating list of messages.
            pageCollection.getFirstPage(new Callback<Page<ChatMessage>>() {
                @Override
                public void onSuccess(Page<ChatMessage> firstPage, Response response) {
                    for (ChatMessage message : firstPage.getItems()) {
                        // Take further action.
                    }
                    retrieveNextMessagePages(firstPage.getPageId(), pageCollection);
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    // Handle error.
                }
            });
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // Handle error.
        }
});

void retrieveNextMessagePages(String nextPageId,
    AsyncPagedDataCollection<ChatMessage, Page<ChatMessage>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ChatMessage>>() {
        @Override
        public void onSuccess(Page<ChatMessage> nextPage, Response response) {
            for (ChatMessage thread : nextPage.getItems()) {
                // Take further action.
            }
            if (nextPage.getPageId() != null) {
                retrieveNextMessagePages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // Handle error.
        }
    });
}
```

#### Update a message

Use the `update` method to update a message in a thread.

```java
// The message content to be updated.
final String content = "updated message";
// The message priority level, such as 'NORMAL' or 'HIGH', if null (i.e. not specified), 
// 'NORMAL' will be set.
final ChatMessagePriority priority = ChatMessagePriority.HIGH;
//  The model to pass to the update method.
UpdateChatMessageRequest message = new UpdateChatMessageRequest()
    .setContent(content)
    .setPriority(priority);

// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the message.
final String messageId = "<message_id>";
client.updateChatMessage(threadId, messageId, message, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### Delete a message

Use the `deleteChatMessage` method to delete a message in a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the message.
final String messageId = "<message_id>";
client.deleteChatMessage(threadId, messageId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

### Thread Participant Operations

#### Get thread participants

Use the `listChatParticipants` method to retrieve the participants participating in a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.listChatParticipantsPages(threadId,
    new Callback<AsyncPagedDataCollection<ChatParticipant, Page<ChatParticipant>>>() {
    @Override
    public void onSuccess(AsyncPagedDataCollection<ChatParticipant, Page<ChatParticipant>> firstPage,
        Response response) {
        // pageCollection enables enumerating list of chat participants.
        pageCollection.getFirstPage(new Callback<Page<ChatParticipant>>() {
            @Override
            public void onSuccess(Page<ChatParticipant> firstPage, Response response) {
                for (ChatParticipant participant : firstPage.getItems()) {
                    // Take further action.
                }
                retrieveNextParticipantsPages(firstPage.getPageId(), pageCollection);
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                // Handle error.
            }
         }
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});

void retrieveNextParticipantsPages(String nextPageId,
    AsyncPagedDataCollection<ChatParticipant, Page<ChatParticipant>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ChatParticipant>>() {
        @Override
        public void onSuccess(Page<ChatParticipant> nextPage, Response response) {
            for (ChatParticipant participant : nextPage.getItems()) {
                // Take further action.
            }
            if (nextPage.getPageId() != null) {
                retrieveNextParticipantsPages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // Handle error.
        }
    });
}
```

#### Add thread participants

Use the `add` method to add participants to a thread.

```java
//  The list of ChatParticipant to be added to the thread.
List<ChatParticipant> participants = new ArrayList<>();
// The CommunicationUser.identifier you created before, required.
final String id = "<user_id>";
// The display name for the thread participant.
final String displayName = "a new participant";
participants.add(new ChatParticipant().setId(id).setDisplayName(displayName));
// The model to pass to the add method.
AddChatParticipantsRequest participants = new AddChatParticipantsRequest()
    .setParticipants(participants);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.addChatParticipants(threadId, participants, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### Remove a thread participant

Use the `removeChatParticipant` method to remove a participant from a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the participant.
final String participantId = "<participant_id>";
client.removeChatParticipant(threadId, participantId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

### Events Operations

#### Send a typing notification

Use the `sendTypingNotification` method to post a typing notification event to a thread, on behalf of a user.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.sendTypingNotification(threadId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### Send read receipt

Use the `send` method to post a read receipt event to a thread, on behalf of a user.

```java
// The unique ID of the participant.
final String messageId = "<message_id>";
// The model to be passed to the send method.
SendReadReceiptRequest readReceipt = new SendReadReceiptRequest()
    .setChatMessageId(messageId);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.sendChatReadReceipt(threadId, readReceipt, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        // Take further action.
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});
```

#### Get read receipts

Use the `listChatReadReceipts` method to retrieve read receipts for a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.listChatReadReceiptsPages(threadId,
    new Callback<AsyncPagedDataCollection<ReadReceipt, Page<ReadReceipt>>>() {
    @Override
    public void onSuccess(AsyncPagedDataCollection<ReadReceipt, Page<ReadReceipt>> result,
        Response response) {
        // pageCollection enables enumerating list of chat participants.
        pageCollection.getFirstPage(new Callback<Page<ReadReceipt>>() {
            @Override
            public void onSuccess(Page<ReadReceipt> firstPage, Response response) {
                for (ReadReceipt receipt : firstPage.getItems()) {
                    // Take further action.
                }
                retrieveNextReceiptsPages(firstPage.getPageId(), pageCollection);
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                // Handle error.
            }
         }
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // Handle error.
    }
});

void retrieveNextReceiptsPages(String nextPageId,
    AsyncPagedDataCollection<ReadReceipt, Page<ReadReceipt>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ReadReceipt>>() {
        @Override
        public void onSuccess(Page<ReadReceipt> nextPage, Response response) {
            for (ReadReceipt receipt : nextPage.getItems()) {
                // Take further action.
            }
            if (nextPage.getPageId() != null) {
                retrieveNextReceiptsPages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // Handle error.
        }
    });
}
```

## Troubleshooting

When an error occurs, the client calls the callback's `onFailure` method. You can use the provided `Throwable` to act upon the failure.

```java
client.createChatThread(thread, new Callback<MultiStatusResponse>() {
    public void onFailure(Throwable throwable, okhttp3.Response response) {
        // Handle error.
    }
});
```

## Next steps

More sample code should go here, along with links out to the appropriate code samples.

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-android%2Fsdk%2Fcommunication%2Fazure-communication-chat%2FREADME.png)
