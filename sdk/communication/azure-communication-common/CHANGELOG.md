# Release History

## 1.0.1 (2021-06-09)
### Dependency Updates
- Updated Azure Core from 1.0.0-beta.5 to 1.0.0-beta.6

## 1.0.0 (2021-04-20)
- Update version

## 1.0.0-beta.8 (2021-03-29)
### Breaking Changes
- Changed `UserCredential.getToken()` to return `CompletableFuture<CommunicationAccessToken>` instead of `Future<CommunicationAccessToken>`.
- Invoking `getToken` on a disposed `UserCredential` returns a failed `CompletableFuture` instead of cancelled `Future`.
- Removed the `fromString` method from `CommunicationCloudEnvironment` given the same result can be achieved using the existing public constructor.
- Renamed the `getToken` method in `CommunicationTokenRefreshOptions` to `getInitialToken`.

## 1.0.0-beta.7 (2021-03-09)
### Breaking Changes
- Credential `getToken` returns the newly added `CommunicationAccessToken` object instead of `AccessToken`.
- Renamed 'getRefreshProactively' to 'isRefreshProactively' in 'CommunicationTokenRefreshOptions'
- Removed constructor 'MicrosoftTeamsUserIdentifier(String userId, boolean isAnonymous, CommunicationCloudEnvironment cloudEnvironment)' in 'MicrosoftTeamsUserIdentifier'
- A few classes are made final 'CommunicationTokenCredential', 'CommunicationTokenRefreshOptions', 'CommunicationUserIdentifier', 'MicrosoftTeamsUserIdentifier', 'PhoneNumberIdentifier', 'UnknownIdentifier', 'CommunicationAccessToken'

### New Features
- Introduce new class `CommunicationAccessToken`.

## 1.0.0-beta.6 (2021-02-26)
### Breaking Changes
- Removed `CommunicationTokenCredential(Callable<String> tokenRefresher)`, ` CommunicationTokenCredential(Callable<String> tokenRefresher, String initialToken)`, `CommunicationTokenCredential(Callable<String> tokenRefresher, boolean refreshProactively)`, `CommunicationTokenCredential(Callable<String> tokenRefresher, boolean refreshProactively, String initialToken)`, and added `CommunicationTokenCredential(CommunicationTokenRefreshOptions tokenRefreshOptions)`

## 1.0.0-beta.5 (2021-02-08)
### Breaking Changes
- Removed 'CallingApplicationIdentifier'.
- Removed 'getId' method in 'CommunicationIdentifier' class.

### New Features
- Added a new 'MicrosoftTeamsUserIdentifier' constructor that takes a non-null CommunicationCloudEnvironment parameter.
- Added class 'CommunicationCloudEnvironment'.
- Added class 'CommunicationCloudEnvironmentModel'.
- Added class 'CommunicationIdentifierSerializer'.
- Added class 'CommunicationIdentifierModel'.
- Added class 'MicrosoftTeamsUserIdentifierModel'.
- Added class 'PhoneNumberIdentifierModel'.
- Added class 'CommunicationUserIdentifierModel'.

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
