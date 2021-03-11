# Release History
## 1.0.0-beta.8 (2021-03-11)
### Added
- Add chat basic operations in test app 

### Breaking Changes
- Fix real time notification connection url

## 1.0.0-beta.7 (2021-03-09)
### Added
- Support real time notifications with new methods in ChatClient/ChatAsyncClient: 
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
[read_me]: https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/communication/azure-communication-chat/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
