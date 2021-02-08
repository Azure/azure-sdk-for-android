# Release History

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
- Rename member and threadMember to participant
- Add participants API changed with a /:add at the end
- Remove priority in message


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

## 1.0.0-beta.2 (2020-10-06)
This is the initial release of Azure Communication Services for chat. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-android/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/communication/azure-communication-chat/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
