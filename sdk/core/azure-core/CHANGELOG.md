# Release History

## 1.0.0-beta.6 (Unreleased)

### New Features

- Added `AsyncStream` and `AsyncStreamHandler` to support enumeration of a stream of data asynchronously
- Added `Page` and `PagedAsyncStreamCore` that defines the paging contract.

### Breaking Changes

- Renamed `azure.core.util.Context` to `azure.core.util.RequestContext`
- Removed `ContinuablePage`

## 1.0.0-beta.5 (2021-03-26)

### Breaking Changes

- Removed the `azure-core.properties` file.

## 1.0.0-beta.4 (2021-03-18)

### Breaking Changes

- Split Azure Core into smaller modules:
    - azure-core
    - azure-core-credential
    - azure-core-http
    - azure-core-http-httpurlconnection
    - azure-core-http-okhttp
    - azure-core-jackson
    - azure-core-logging
    - azure-core-rest
    - azure-core-test

## 1.0.0-beta.3 (2021-01-15)

### New Features

- Added `ClientOptions`, `RequestOptions` and `TransportOptions`.

## 1.0.0-beta.2 (2020-10-05)

### New Features

- Added `PagedDataCollection`, `PagedDataResponseCollection`, `AsyncPagedDataCollection` and associated types to support pagination APIs.

## 1.0.0-beta.1 (2020-09-17)

- Initial release. Please see the README for information.
