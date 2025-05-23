# Release History

## 1.3.1 (2025-06-22)

### Features Added
- Added support for a new communication identifier `TeamsExtensionUserIdentifier` which maps rawIds with format `8:acs:{resourceId}_{tenantId}_{userId}`.
- Added `isAnonymous` and `getAssertedId` methods to `PhoneNumberIdentifier`.

## 1.2.1 (2024-02-23)

### Other Changes

#### Dependency Updates
- Updated `android-core` version to `1.0.0-beta.14`.
- Updated `android-core-logging` version to `1.0.0-beta.14`.

## 1.2.0 (2024-02-20)

### Features Added
- Added support for a new communication identifier `MicrosoftTeamsAppIdentifier`.
- Added a new constructor with required param `tokenRefresher` for `CommunicationTokenRefreshOptions`.
- Added fluent setters for optional properties:
    - Added the `setRefreshProactively(boolean refreshProactively)` fluent setter that allows setting whether the token should be proactively renewed prior to its expiry or on demand.
    - Added the `setInitialToken(String initialToken)` fluent setter that allows setting the optional serialized JWT token.
- Optimization added: When the proactive refreshing is enabled and the token refresher fails to provide a token that's not about to expire soon, the subsequent refresh attempts will be scheduled for when the token reaches half of its remaining lifetime until a token with long enough validity (>10 minutes) is obtained.
- Added a default `CommunicationCloudEnvironment` constructor set to use Azure Public Cloud.
- Overrode the `equals`, `toString`, `hashCode` methods of `CommunicationCloudEnvironment` to make it consistent with Azure Core's `ExpandableStringEnum` APIs.

### Other Changes
- Introduction of `MicrosoftTeamsAppIdentifier` is a breaking change. It will impact any code that previously depended on the use of UnknownIdentifier with rawIDs starting with `28:orgid:`, `28:dod:`, or `28:gcch:`.
- Deprecated constructors that take 2 or more arguments in `CommunicationTokenRefreshOptions`. Users should now use the `CommunicationTokenRefreshOptions(Callable tokenRefresher)` constructor and chain fluent setters.
- Updated `targetSdkVersion` and `compileSdkVersion` from `30` to `34`.

## 2.0.0-beta.2 (2023-05-16)

### Features Added
- Added new constructor with required param `tokenRefresher` for `CommunicationTokenRefreshOptions`
- Deprecated old constructor overloads in `CommunicationTokenRefreshOptions` and replaced by fluent setters
- Added fluent setters for optional properties:
    - Added `setRefreshProactively(boolean refreshProactively)` setter that allows setting whether the token should be proactively renewed prior to its expiry or on demand.
    - Added `setInitialToken(String initialToken)` setter that allows setting the optional serialized JWT token
- Optimization added: When the proactive refreshing is enabled and the token refresher fails to provide a token that's not about to expire soon, the subsequent refresh attempts will be scheduled for when the token reaches half of its remaining lifetime until a token with long enough validity (>10 minutes) is obtained.
- The default `CommunicationCloudEnvironment` constructor will create Azure public cloud.
- Overrode the `equals`, `toString`, `hashCode` methods of `CommunicationCloudEnvironment` to make it consistent with Java API.

### Breaking Changes
- Introduced non-nullability check for the argument of `CommunicationCloudEnvironment.fromString(String name)`. It will throw `NullPointerException` if the passed argument is null.

## 2.0.0-beta.1 (2023-04-04)

### Features Added
- Added support for a new communication identifier `MicrosoftBotIdentifier`.

### Breaking Changes
- Introduction of `MicrosoftBotIdentifier` is a breaking change. It will affect code that relied on using `UnknownIdentifier` with a rawID starting with `28:`

## 1.1.1 (2023-10-23)

### Other Changes

#### Dependency updates
- Updated `android-core` version to `1.0.0-beta.13`.
- Updated `android-core-logging` version to `1.0.0-beta.13`.

## 1.1.0 (2022-11-11)

### Bugs Fixed
- Fixed the logic of `PhoneNumberIdentifier` to always maintain the original phone number string whether it included the leading + sign or not.

#### Dependency updates
- Updated `android-core` version to `1.0.0-beta.12`.
- Updated `android-core-logging` version to `1.0.0-beta.12`.
- Updated `jackson-databind` dependency to `2.12.7.1`.

## 1.1.0-beta.1 (2022-08-29)

### Features Added
- Added `String getRawId()`, and `static CommunicationIdentifier fromRawId(String rawId)` to `CommunicationIdentifier` to translate between a `CommunicationIdentifier` and its underlying canonical rawId representation. Developers can now use the rawId as an encoded format for identifiers to store in their databases or as stable keys in general.

## 1.0.2 (2022-03-11)

### Other updates

#### Dependency updates
- Updated `azure-core` dependency to `1.0.0-beta.10`
- Updated `azure-core-logging` dependency to `1.0.0-beta.10`
- Updated `android-retrofuture` dependency to `1.7.4`
- Updated `jackson-databind` dependency to `2.12.6`
- Updated `threetenabp` dependency to `1.3.1`

## 1.0.1 (2021-06-15)
### Dependency Updates
- Updated `com.azure.android.core` from `1.0.0-beta.5` to `1.0.0-beta.6`

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
[read_me]: https://github.com/Azure/azure-sdk-for-android/blob/main/sdk/communication/azure-communication-common/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
