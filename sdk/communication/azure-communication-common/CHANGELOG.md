# Release History

## 1.0.0-beta.5 (2021-02-08)
### Breaking Changes
- CallingApplicationIdentifier is removed.
- The getId function in CommunicationIdentifier class is removed.

### Added
- New MicrosoftTeamsUserIdentifier constructor that takes a non-null CommunicationCloudEnvironment parameter.
- Added class CommunicationCloudEnvironment.
- Added class CommunicationCloudEnvironmentModel.
- Added class CommunicationIdentifierSerializer.
- Added class CommunicationIdentifierModel.
- Added class MicrosoftTeamsUserIdentifierModel.
- Added class PhoneNumberIdentifierModel.
- Added class CommunicationUserIdentifierModel.

## 1.0.0-beta.4 (2021-01-28)
### Breaking Changes
- Renamed `CommunicationUserCredential` to `CommunicationTokenCredential`
- Renamed `PhoneNumber` to `PhoneNumberIdentifier`
- Renamed `CommunicationUser` to `CommunicationUserIdentifier `
- Renamed `CallingApplication` to `CallingApplicationIdentifier`

### Added
- Added class `MicrosoftTeamsUserIdentifier`

## 1.0.0-beta.1 (2020-09-22)
This package contains common code for Azure Communication Service libraries. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-android/blob/master/sdk/communication/azure-communication-common/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
