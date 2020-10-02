# Azure Communication Chat Service client library for Android
This package contains the Android SDK for Azure Communication Services for Chat.
Read more about Azure Communication Services [here](https://docs.microsoft.com/azure/communication-services/overview).

# Getting started

## Prerequisites

* An Azure Communication Resource, learn how to create one from [Create an Azure Communication Resource](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource)
  higher to use this library.
* The library is written in Java 8. Your application must be built with Android Gradle Plugin 3.0.0 or later, and must
  be configured to
  [enable Java 8 language desugaring](https://developer.android.com/studio/write/java8-support.html#supported_features)
  to use this library. Java 8 language features that require a target API level >21 are not used, nor are any Java 8+
  APIs that would require the Java 8+ API desugaring provided by Android Gradle plugin 4.0.0.
* You must have an [Azure subscription](https://azure.microsoft.com/free/) to use this library.

### Versions available
The current version of this library is **1.0.0-beta.1**.

> Note: The SDK is currently in **beta**. The API surface and feature sets are subject to change at any time before **GA**. We do not currently recommend them for production use.

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
    implementation "com.azure.android:azure-communication-chat:1.0.0-beta.1"
}

// build.gradle.kts
dependencies {
    ...
    implementation("com.azure.android:azure-communication-chat:1.0.0-beta.1")
}
```

#### Add a dependency with Maven
To import the library into your project using the [Maven](https://maven.apache.org/) build system, add it to the `dependencies` section of your app's `pom.xml` file, specifying its artifact ID and the version you wish to use:

```xml
<dependency>
  <groupId>com.azure.android</groupId>
  <artifactId>azure-communication-chat</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```

## User and User Access Tokens

User access tokens enable you to build client applications that directly authenticate to Azure Communication Services. Refer [here](https://docs.microsoft.com/azure/communication-services/quickstarts/access-tokens) to learn how to create a user and issue a User Access Token.

The id for the user created above will be necessary later to add said user as a member of a new chat thread. The initiator of the create request must be in the list of members of the chat thread.

## Create the AzureCommunicationChatClient

```java
import com.azure.android.communication.chat.AzureCommunicationChatServiceAsyncClient;
import com.azure.android.core.http.HttpHeader;

final String endpoint = "https://<resource>.communication.azure.com";
final String userAccessToken = "<user_access_token>";

AzureCommunicationChatServiceAsyncClient client
    = new AzureCommunicationChatServiceAsyncClient.Builder()
    .endpoint(endpoint)
    .credentialInterceptor(chain -> chain.proceed(chain.request()
        .newBuilder()
        .header(HttpHeader.AUTHORIZATION, userAccessToken)
        .build());
```

# Key concepts

A chat conversation is represented by a chat thread. Each user in the thread is called a thread member. Thread members can chat with one another privately in a 1:1 chat or huddle up in a 1:N group chat. Users also get near real-time updates for when others are typing and when they have read the messages.

Once you initialize an `AzureCommunicationChatClient` class, you can perform the following chat operations:

## Thread Operations

- [Create a thread](#create-a-thread)
- [Get a thread](#get-a-thread)
- [List threads](#list-threads)
- [Update a thread](#update-a-thread)
- [Delete a thread](#delete-a-thread)

## Message Operations

- [Send a message](#send-a-message)
- [Get a message](#get-a-message)
- [List messages](#list-messages)
- [Update a message](#update-a-message)
- [Delete a message](#delete-a-message)

## Thread Member Operations

- [Get thread members](#get-thread-members)
- [Add thread members](#add-thread-members)
- [Remove a thread member](#remove-a-thread-member)

## Events Operations

- [Send a typing notification](#send-a-typing-notification)
- [Send read receipt](#send-read-receipt)
- [Get read receipts](#get-read-receipts)

# Examples

## Thread Operations

### Create a thread

Use the `create` method to create a thread.

```java
//  The list of ChatThreadMember to be added to the thread.
List<ChatThreadMember> members = new ArrayList<>();
// The CommunicationUser.identifier you created before, required.
final String id = "<user_id>";
// The display name for the thread member.
final String displayName = "initial member";
members.add(new ChatThreadMember()
    .setId(id)
    .setDisplayName(displayName));

// The topic for the thread.
final String topic = "General";
// The model to pass to create method.
CreateChatThreadRequest thread = new CreateChatThreadRequest()
    .setTopic(topic)
    .setMembers(members);

client.createChatThread(thread, new Callback<MultiStatusResponse>() {
    public void onSuccess(MultiStatusResponse result, okhttp3.Response response) {
        // MultiStatusResponse is the result returned from creating a thread.
        // It has a 'multipleStatus' property which represents a list of IndividualStatusResponse.
        String threadId;
        List<IndividualStatusResponse> statusList = result.getMultipleStatus();
        for (IndividualStatusResponse  status : statusList) {
            if (status.getId().endsWith("@thread.v2")
                && status.getType().contentEquals("Thread")) {
                threadId = status.getId();
                break;
            }
        }
        // TODO: Take further action.
    }

    public void onFailure(Throwable throwable, okhttp3.Response response) {
        // TODO: Display error message.
    }
});
```

### Get a thread

Use the `getChatThread` method to retrieve a thread.
```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.getChatThread(threadId, new Callback<ChatThread>() {
    @Override
    public void onSuccess(ChatThread thread, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### List threads

Use the `listChatThreads` method to retrieve a list of threads.

```java
// The maximum number of messages to be returned per page, optional.
final int maxPageSize = 10;
// The thread start time to consider in the query, optional
final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-08T00:00:00Z");
client.listChatThreadsPages(maxPageSize, startTime,
    new Callback<AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>>>() {
    @Override
    public void onSuccess(AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>> pageCollection,
                          Response response) {
     // pageCollection enables enumerating list of threads.                       
     pageCollection.getFirstPage(new Callback<Page<ChatThreadInfo>>() {
            @Override
            public void onSuccess(Page<ChatThreadInfo> firstPage, Response response) {
                for (ChatThreadInfo thread : firstPage.getItems()) {
                    //TODO: Take further action
                }
                retrieveNextThreadPages(firstPage.getPageId(), pageCollection);
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                // TODO: Display error message.
            }
        });
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});

void retrieveNextThreadPages(String nextPageId,
                       AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ChatThreadInfo>>() {
        @Override
        public void onSuccess(Page<ChatThreadInfo> nextPage, Response response) {
            for (ChatThreadInfo thread : nextPage.getItems()) {
                //TODO: Take further action
            }
            if (nextPage.getPageId() != null) {
                retrieveNextThreadPages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // TODO: Display error message.
        }
    });
}
```

### Update a thread

Use the `update` method to update a thread's properties.

```java
// The new topic for the thread.
final String topic = "updated topic";
// The model to pass to update method.
UpdateChatThreadRequest thread = new UpdateChatThreadRequest()
    .setTopic(topic);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.updateChatThread(threadId, thread, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### Delete a thread

Use `deleteChatThread` method to delete a thread.


```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.deleteChatThread(threadId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

## Message Operations

### Send a message

Use the `send` method to send a message to a thread.
```java
// The chat message content, required.
final String content = "Test message 1";
//  The display name of the sender, if null i.e. not specified, an empty name will be set.
final String senderDisplayName = "An important person";
// The message priority level, such as 'normal' or 'high', 
// if null i.e. not specified, 'normal' will be set.
final ChatMessagePriority priority = ChatMessagePriority.HIGH;
SendChatMessageRequest message = new SendChatMessageRequest()
    .setPriority(priority)
    .setContent(content)
    .setSenderDisplayName(senderDisplayName);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.sendChatMessage(threadId, message, new Callback<SendChatMessageResult>() {
    @Override
    public void onSuccess(SendChatMessageResult result, Response response) {
        // SendChatMessageResult is the response returned from sending a message, it contains an id, 
        // which is the unique ID of the message.
        final String chatMessageId = result.getId();
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### Get a message

Use the `getChatMessage` method to retrieve a message in a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the message.
final String chatMessageId = "<message_id>";

client.getChatMessage(threadId,
    chatMessageId,
    new Callback<ChatMessage>() {
    @Override
    public void onSuccess(ChatMessage result, Response response) {
        // `ChatMessage` is the response returned from getting a message.
        final String content = result.getContent();
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### List messages

Use the `listChatMessages` method to retrieve messages in a thread.
```java
// The maximum number of messages to be returned per page, optional.
final int maxPageSize = 10;
// The thread start time to consider in the query, optional
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
                        //TODO: Take further action
                    }
                    retrieveNextMessagePages(firstPage.getPageId(), pageCollection);
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    // TODO: Display error message.
                }
            });
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // TODO: Display error message.
        }
});

void retrieveNextMessagePages(String nextPageId,
                              AsyncPagedDataCollection<ChatMessage, Page<ChatMessage>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ChatMessage>>() {
        @Override
        public void onSuccess(Page<ChatMessage> nextPage, Response response) {
            for (ChatMessage thread : nextPage.getItems()) {
                //TODO: Take further action
            }
            if (nextPage.getPageId() != null) {
                retrieveNextMessagePages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // TODO: Display error message.
        }
    });
}

```

### Update a message

Use the `update` method to update a message in a thread.

```java
// The message content to be updated.
final String content = "updated message";
// The message priority level, such as 'normal' or 'high', if null i.e. not specified, 
// 'normal' will be set.
final ChatMessagePriority priority = ChatMessagePriority.HIGH;
//  The model to pass to update method.
UpdateChatMessageRequest message = new UpdateChatMessageRequest()
    .setContent(content)
    .setPriority(priority);

// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the message.
final String messageId = "message_id";
client.updateChatMessage(threadId,
    messageId,
    message, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### Delete a message

Use the `deleteChatMessage` method to delete a message in a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the message.
final String messageId = "message_id";
client.deleteChatMessage(threadId, messageId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

## Thread Member Operations

### Get thread members

Use the `listChatThreadMembers` method to retrieve the members participating in a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.listChatThreadMembersPages(threadId,
    new Callback<AsyncPagedDataCollection<ChatThreadMember, Page<ChatThreadMember>>>() {
    @Override
    public void onSuccess(AsyncPagedDataCollection<ChatThreadMember,
        Page<ChatThreadMember>> firstPage,
        Response response) {
         // pageCollection enables enumerating list of chat members.
         pageCollection.getFirstPage(new Callback<Page<ChatThreadMember>>() {
            @Override
            public void onSuccess(Page<ChatThreadMember> firstPage, Response response) {
                for (ChatThreadMember member : firstPage.getItems()) {
                    //TODO: Take further action
                }
                retrieveNextMembersPages(firstPage.getPageId(), pageCollection);
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                // TODO: Display error message.
            }
         }
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});

void retrieveNextMembersPages(String nextPageId,
                              AsyncPagedDataCollection<ChatThreadMember, Page<ChatThreadMember>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ChatThreadMember>>() {
        @Override
        public void onSuccess(Page<ChatThreadMember> nextPage, Response response) {
            for (ChatThreadMember member : nextPage.getItems()) {
                //TODO: Take further action
            }
            if (nextPage.getPageId() != null) {
                retrieveNextMembersPages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // TODO: Display error message.
        }
    });
}
```

### Add thread members

Use the `add` method to add members to a thread.

```java
//  The list of ChatThreadMember to be added to the thread.
List<ChatThreadMember> members = new ArrayList<>();
// The CommunicationUser.identifier you created before, required.
final String id = "<user_id>";
// The display name for the thread member.
final String displayName = "a new member";
members.add(new ChatThreadMember().setId(id).setDisplayName(displayName));
// The model to pass to add method.
AddChatThreadMembersRequest threadMembers = new AddChatThreadMembersRequest()
    .setMembers(members);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.addChatThreadMembers(threadId, threadMembers, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### Remove a thread member

Use the `removeChatThreadMember` method to remove a member from a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
// The unique ID of the member.
final String memberId = "<member_id>";
client.removeChatThreadMember(threadId, memberId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

## Events Operations

### Send a typing notification

Use the `sendTypingNotification` method to post a typing notification event to a thread, on behalf of a user.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.sendTypingNotification(threadId, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### Send read receipt

Use the `send` method to post a read receipt event to a thread, on behalf of a user.

```java
// The unique ID of the member.
final String messageId = "<message_id>";
// The model to be passed to send method.
SendReadReceiptRequest readReceipt = new SendReadReceiptRequest()
    .setChatMessageId(messageId);

// The unique ID of the thread.
final String threadId = "<thread_id>";
client.sendChatReadReceipt(threadId, readReceipt, new Callback<Void>() {
    @Override
    public void onSuccess(Void result, Response response) {
        //TODO: Take further action
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});
```

### Get read receipts

Use the `listChatReadReceipts` method to retrieve read receipts for a thread.

```java
// The unique ID of the thread.
final String threadId = "<thread_id>";
client.listChatReadReceiptsPages(threadId,
    new Callback<AsyncPagedDataCollection<ReadReceipt, Page<ReadReceipt>>>() {
    @Override
    public void onSuccess(AsyncPagedDataCollection<ReadReceipt,
        Page<ReadReceipt>> result,
        Response response) {
         // pageCollection enables enumerating list of chat members.
         pageCollection.getFirstPage(new Callback<Page<ReadReceipt>>() {
            @Override
            public void onSuccess(Page<ReadReceipt> firstPage, Response response) {
                for (ReadReceipt receipt : firstPage.getItems()) {
                    //TODO: Take further action
                }
                retrieveNextReceiptsPages(firstPage.getPageId(), pageCollection);
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                // TODO: Display error message.
            }
         }
    }

    @Override
    public void onFailure(Throwable throwable, Response response) {
        // TODO: Display error message.
    }
});

void retrieveNextReceiptsPages(String nextPageId,
                              AsyncPagedDataCollection<ReadReceipt, Page<ReadReceipt>> pageCollection) {
    pageCollection.getPage(nextPageId, new Callback<Page<ReadReceipt>>() {
        @Override
        public void onSuccess(Page<ReadReceipt> nextPage, Response response) {
            for (ReadReceipt receipt : nextPage.getItems()) {
                //TODO: Take further action
            }
            if (nextPage.getPageId() != null) {
                retrieveNextReceiptsPages(nextPage.getPageId(), pageCollection);
            }
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
            // TODO: Display error message.
        }
    });
}
```

# Troubleshooting

## General

The client raises via Callback.onFailure.

```java
client.createChatThread(thread, new Callback<MultiStatusResponse>() {
    public void onFailure(Throwable throwable, okhttp3.Response response) {
        // TODO: Display error message.
    }
});
```

# Next steps

More sample code should go here, along with links out to the appropriate example tests.

# Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](TODO: Find impressions URL)