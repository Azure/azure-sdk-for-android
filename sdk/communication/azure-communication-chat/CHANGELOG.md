# Release History
## 2.1.0-beta.1 (Unreleased)


## 2.0.0 (2022-08-29)
### Features Added
- Create new `startPushNotifications` API without errorHandler as a parameter.

### Bugs Fixed
- Push notification registration renewal is now guaranteed to happen when an app is idle or closed.

### Breaking Changes
- Introduced the use of WorkManager, which now requires additional configuration in apps that were already using the push notification feature. Check the [push notifications documentation](https://docs.microsoft.com/azure/communication-services/tutorials/chat-android-push-notification) for more information.
- Increase minSDKVersion from 21 to 23.

### Other Changes
- Implement internal key management library to securely persist all the registration secret keys which are not expired.
- Reduce the expiration time of each push notification registration from 25 hours to 45 minutes.
- Add `@Deprecated` annotation to `startPushNotifications` API with `errorHandler` as a parameter.

## 1.1.0 (2022-04-11)
### Features Added
- Added support for metadata in messages.
- Added options class `TypingNotificationOptions` for setting `SenderDisplayName` of the notification sender.
- Added `SenderDisplayName` to `TypingIndicatorReceivedEvent`.
- Added support FCM push notifications:
    - Added the following methods to `ChatAsyncClient` and `ChatClient`:
        - `startPushNotifications`
        - `stopPushNotifications`
        - `handlePushNotification`
        - `addPushNotificationHandler`
        - `removePushNotificationHandler`
     - Added `ChatPushNotification` as the type for passing a push notification payload.
     - Added instructions in the README.md of our sample app on how to setup FCM push notifications.
     - Added code in sample app for FCM push notification end to end scenario.
- Added `startRealtimeNotifications(Context context, Consumer<Throwable> errorHandler)` to `ChatAsyncClient` and `ChatClient`.

### Bugs Fixed
- Fixed a dependency issue with `azure-communication-common`.
- Fixed a BUG in real-time notification payload for GCCH.

### Other Changes
- Deprecated `startRealtimeNotifications(String skypeUserToken, Context context)` in `ChatAsyncClient` and `ChatClient`. Users should utilize `startRealtimeNotifications(Context context, Consumer<Throwable> errorHandler)` instead.

## 1.0.2 (2022-03-14)

### Other Changes

#### Dependency updates
- Updated `azure-core` dependency to `1.0.0-beta.10`
- Updated `azure-core-credential` dependency to `1.0.0-beta.10`
- Updated `azure-core-http` dependency to `1.0.0-beta.10`
- Updated `azure-core-http-okhttp` dependency to `1.0.0-beta.10`
- Updated `azure-core-logging` dependency to `1.0.0-beta.10`
- Updated `azure-core-rest` dependency to `1.0.0-beta.10`
- Updated `azure-communication-common` dependency to `1.0.2`
- Updated `android-retrofuture` dependency to `1.7.4`

## 1.1.0-beta.4 (2021-11-16)
### Features Added
- Added support FCM push notifications:
    - Added methods to `ChatAsyncClient`:
        - `startPushNotifications`
        - `stopPushNotifications`
        - `handlePushNotification`
        - `addPushNotificationHandler`
        - `removePushNotificationHandler`
    - Added methods to `ChatClient`:
        - `startPushNotifications`
        - `stopPushNotifications`
        - `handlePushNotification`
        - `addPushNotificationHandler`
        - `removePushNotificationHandler`
     - Added `ChatPushNotification` as the type for passing a push notification payload.
     - Added instructions in README.md in sample app for steps to setup FCM push notifications.
     - Added code in sample app for FCM push notification end to end scenario.
- Added `startRealtimeNotifications(Context context, Consumer<Throwable> errorHandler)` to `ChatAsyncClient`.
- Added `startRealtimeNotifications(Context context, Consumer<Throwable> errorHandler)` to `ChatClient`.

### Other Changes
- Deprecate `startRealtimeNotifications(String skypeUserToken, Context context)` in `ChatAsyncClient`.
- Deprecate `startRealtimeNotifications(String skypeUserToken, Context context)` in `ChatClient`.

#### Dependency Updates
- Updated `com.azure.android.core` from `1.0.0-beta.6` to `1.0.0-beta.9`.

## 1.1.0-beta.3 (2021-09-15)
- Version update.

## 1.0.1 (2021-09-15)
- Version update.

## 1.1.0-beta.2 (2021-08-10)
- Version update.
- Fix `azure-communication-common` dependency issue.

## 1.1.0-beta.1 (2021-08-10)
- Added support for metadata in messages.
- Added options class `TypingNotificationOptions` for setting `SenderDisplayName` of the notification sender.
- Added `SenderDisplayName` to `TypingIndicatorReceivedEvent`.

## 1.0.0 (2021-06-15)
### New Features
- Added `ChatServiceVersion` and the ability to set it on `ChatClientBuilder` and `ChatThreadClientBuilder`.

### Breaking Changes
- ChatClientBuilder:
    - Added `retryPolicy`.
    - Removed `credentialPolicy`.
    - Removed `realtimeNotificationParams`.
- ChatThreadClientBuilder:
    - Added `retryPolicy`.
    - Removed `credentialPolicy`.
    - Removed `realtimeNotificationParams`.
- ChatClient:
    - Added `listChatThreads`.
    - Changed `startRealtimeNotifications` with adding parameter `String skypeUserToken` and `Context context`.
    - Removed `getChatThreadsFirstPage`.
    - Removed `getChatThreadsFirstPageWithResponse`.
    - Removed `getChatThreadsNextPage`.
    - Removed `getChatThreadsNextPageWithResponse`.
    - Replaced `azure.core.util.Context` in the APIs with `azure.core.util.RequestContext`.
    - Replaced `on` with `addEventHandler`.
    - Replaced `off` with `removeEventHandler`.
- ChatAsyncClient:
    - Added `listChatThreads`.
    - Changed `startRealtimeNotifications` with adding parameter `String skypeUserToken` and `Context context`.
    - Removed `getChatThreadsFirstPage`.
    - Removed `getChatThreadsFirstPageWithResponse`.
    - Removed `getChatThreadsNextPage`.
    - Removed `getChatThreadsNextPageWithResponse`.
    - Replaced `azure.core.util.Context` in the APIs with `azure.core.util.RequestContext`.
    - Replaced `on` with `addEventHandler`.
    - Replaced `off` with `removeEventHandler`.
- ChatThreadClient:
    - Added `listParticipants`.
    - Added `listMessages`.
    - Added `listReadReceipts`.
    - Changed returning `AddChatParticipantsResult` instead of `void` for `addParticipants`.
    - Changed taking parameter `Iterable<ChatParticipant> participants` instead of `AddChatParticipantsOptions options` for `addParticipants` and `addParticipantsWithResponse`.
    - Removed `getParticipantsFirstPage`.
    - Removed `getParticipantsFirstPageWithResponse`.
    - Removed `getParticipantsNextPage`.
    - Removed `getParticipantsNextPageWithResponse`.
    - Removed `getMessagesFirstPage`.
    - Removed `getMessagesFirstPageWithResponse`.
    - Removed `getMessagesNextPage`.
    - Removed `getMessagesNextPageWithResponse`.
    - Removed `getReadReceiptsFirstPage`.
    - Removed `getReadReceiptsFirstPageWithResponse`.
    - Removed `getReadReceiptsNextPage`.
    - Removed `getReadReceiptsNextPageWithResponse`.
    - Replaced `azure.core.util.Context` in the APIs with `azure.core.util.RequestContext`.
- ChatThreadAsyncClient:
    - Added `listParticipants`.
    - Added `listMessages`.
    - Added `listReadReceipts`.
    - Changed returning `CompletableFuture<AddChatParticipantsResult>` instead of `CompletableFuture<Void>` for `addParticipants`.
    - Changed taking parameter `Iterable<ChatParticipant> participants` instead of `AddChatParticipantsOptions options` for `addParticipants` and `addParticipantsWithResponse`.
    - Removed `getParticipantsFirstPage`.
    - Removed `getParticipantsFirstPageWithResponse`.
    - Removed `getParticipantsNextPage`.
    - Removed `getParticipantsNextPageWithResponse`.
    - Removed `getMessagesFirstPage`.
    - Removed `getMessagesFirstPageWithResponse`.
    - Removed `getMessagesNextPage`.
    - Removed `getMessagesNextPageWithResponse`.
    - Removed `getReadReceiptsFirstPage`.
    - Removed `getReadReceiptsFirstPageWithResponse`.
    - Removed `getReadReceiptsNextPage`.
    - Removed `getReadReceiptsNextPageWithResponse`.
    - Replaced `azure.core.util.Context` in the APIs with `azure.core.util.RequestContext`.
- Removed `com.azure.android.communication.chat.signaling.chatevents.ChatParticipant`.
- Removed `com.azure.android.communication.chat.signaling.chatevents.ChatThreadProperties`.
- Removed `com.azure.android.communication.chat.models.AddChatParticipantsOptions`.
- Removed setters for `AddChatParticipantsResult`.
- Removed setters for `ChatError`.
- Changed `com.azure.android.communication.chat.signaling.chatevents.*` to `com.azure.android.communication.chat.models.*`.
- Changed `httpClient` to an optional component for `ChatClientBuilder`/`ChatThreadClientBuilder` to create corresponding client.
- Changed `ChatEventId` to `ChatEventType`.
- Changed `BaseEvent` to `ChatEvent`.
- Changed `ChatMessageReceivedEvent` createdOn type from `String` to `OffsetDateTime`.
- Changed `ChatMessageEditedEvent` createdOn and editedOn type from `String` to `OffsetDateTime`.
- Changed `ChatMessageDeletedEvent` createdOn and deletedOn type from `String` to `OffsetDateTime`.
- Changed `ReadReceiptReceivedEvent` readOn type from `String` to `OffsetDateTime`.
- Changed `TypingIndicatorReceivedEvent` receivedOn type from `String` to `OffsetDateTime`.
- Changed `ChatThreadCreatedEvent` createdOn type from `String` to `OffsetDateTime`.
- Changed `ChatThreadDeletedEvent` deletedOn type from `String` to `OffsetDateTime`.
- Changed `ChatThreadPropertiesUpdatedEvent` updatedOn type from `String` to `OffsetDateTime`.
- Changed `ParticipantsAddedEvent` addedOn type from `String` to `OffsetDateTime`.
- Changed `ParticipantsRemovedEvent` removedOn type from `String` to `OffsetDateTime`.

### Dependency Updates
- Updated `com.azure.android.core` from `1.0.0-beta.5` to `1.0.0-beta.6`.


## 1.0.0-beta.8 (2021-03-29)
### Breaking Changes
- ChatThreadAsyncClient:
    - Renamed `getChatThreadProperties` to `getProperties`.
    - Renamed `getChatThreadPropertiesWithResponse` to `getPropertiesWithResponse`.
    - Changed `addParticipant` and `addParticipantWithResponse` to throw `InvalidParticipantException` for failure instead of returning `AddChatParticipantsResult`.
    - Changed `sendMessage` and `sendMessageWithResponse` to return `SendChatMessageResult`.
- ChatThreadClient:
    - Renamed `getChatThreadProperties` to `getProperties`.
    - Renamed `getChatThreadPropertiesWithResponse` to `getPropertiesWithResponse`.
    - Changed `addParticipant` and `addParticipantWithResponse` to throw `InvalidParticipantException` for failure instead of returning `AddChatParticipantsResult`.
    - Changed `sendMessage` and `sendMessageWithResponse` to return `SendChatMessageResult`.
- Renamed `ChatThread` to `ChatThreadProperties`.
- Renamed `CommunicationError` to `ChatError`.
- Removed `CommunicationErrorResponse`.
- Renamed `CommunicationErrorResponseException` to `ChatErrorResponseException`.
- Renamed `repeatabilityRequestId` renamed to `idempotencyToken` in `CreateChatThreadOptions`.
- Renamed `chatThread` to `chatThreadProperties` in `CreateChatThreadResult`.
- Removed the `azure-communication-chat.properties` file.

### New Features
- Added ChatThreadClientBuilder
- Added InvalidParticipantException
- Added chat basic operations in test app

### Bug Fixes
- Fixed realtime notification connection URL.

## 1.0.0-beta.7 (2021-03-09)
### Added
- Support realtime notifications with new methods in ChatClient/ChatAsyncClient:
    - startRealtimeNotifications
    - stopRealtimeNotifications
    - on(chatEventId, listenerId, listener)
    - off(chatEventId, listenerId)
- Add a sample chat app under folder samples for testing and playing around chat functionality purpose.

### Breaking Changes
- Change remove participant API to /chat/threads/{chatThreadId}/participants/:remove
- user id in following classes changed from type CommunicationUserIdentifier to type CommunicationIdentifierModel.
- property initiator in ChatMessageContent renamed to initiatorCommunicationIdentifier.
- property senderId in ChatMessage and ChatMessageReadReceipt renamed to senderCommunicationIdentifier.
- property identifier in ChatParticipant renamed to communicationIdentifier.
- property createdBy in ChatThread renamed to createdByCommunicationIdentifier.
- repeatability-Request-ID in header renamed to repeatability-Request-Id.

## 1.0.0-beta.6 (2021-02-26)
### Breaking Changes
- Change ChatParticipant id type from CommunicationUserIdentifier to String.
- Change ChatThread createdBy type from CommunicationUserIdentifier to String.
- Change ChatMessage senderId type from CommunicationUserIdentifier to String.
- Change ChatMessageContent initiator type from CommunicationUserIdentifier to String.
- Change ChangeMessageReadReceipt senderId type from CommunicationUserIdentifier to String.


## 1.0.0-beta.5 (2021-02-08)
### Added
- Support for three more types of chat message content
- Support adding user agent http header in chat client
- New classes
    - CommunicationError
    - CommunicationErrorResponse
    - CommunicationErrorResponseException

### Breaking Changes
- Chat client is split into chatClient and chatThreadClient
- Add participants API changed with a /:add at the end
- Remove priority in message
- Change request and response types to more specific types
- listChatParticipantsPages takes two more parameters: maxPageSize and skip
- listChatReadReceiptsPages takes two more parameters: maxPageSize and skip

### Renamed
- Rename retrieveNextThreadPages to listChatThreadsNext
- Rename retrieveNextThreadPages to listChatThreadsNext
- Rename retrieveNextMessagePages to listChatMessagesNext
- Rename retrieveNextParticipantsPages to listChatParticipantsNext
- Rename retrieveNextReceiptsPages to listChatReadReceiptsNext



## 1.0.0-beta.4 (Skipped)
### Added
- Support for Rich Text Chat message content
- New classes
    - ChatMessageContent
    - ChatMessageType
    - AddChatParticipantsErrors
    - AddChatParticipantsResult
    - ChatMessageType

### Breaking Changes
- ChatMessage properties are now all required
- ChatMessage type is no longer a String type but an extendable Enum type, ChatMessageType
- ChatMessage content is no longer a String type but an object of ChatMessageContent
- All OffsetDateTime properties are now in RFC3339 format instead of ISO8601 format

## 1.0.0-beta.3 (Skipped)
### Breaking Changes
- Return messageId instead of SendChatMessageResult
- Rename ReadReceipt to ChatMessageReadReceipt
- Rename updateChatThread to updateTopic
- Rename UpdateChatThreadRequest to UpdateTopicRequest
- Rename member and threadMember to participant

## 1.0.0-beta.2 (2020-10-06)
This is the initial release of Azure Communication Services for chat. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-android/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-android/blob/main/sdk/communication/azure-communication-chat/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
