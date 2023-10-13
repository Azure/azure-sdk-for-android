# Release History

## 1.0.0-beta.13 (2023-10-12)

### Other Changes

#### Dependency updates
- Updated `azure-core` dependency version to `1.0.0-beta.13`.
- Updated `azure-core-credential` dependency version to `1.0.0-beta.13`.
- Updated `azure-core-logging` dependency version to `1.0.0-beta.13`.

## 1.0.0-beta.12 (2022-11-08)

### Other Changes

#### Dependency updates
- Updated `azure-core` dependency version to `1.0.0-beta.12`.
- Updated `azure-core-credential` dependency version to `1.0.0-beta.12`.
- Updated `azure-core-logging` dependency version to `1.0.0-beta.12`.

## 1.0.0-beta.11 (2022-08-26)

### Bugs Fixed
- Fixed issue where `RetryPolicy` would throw a `NullPointerException` when failing to receive an HTTP response from a service call. ([#1180](https://github.com/Azure/azure-sdk-for-android/pull/1180)) 

### Other Changes

#### Dependency updates
- Updated `azure-core` dependency version to `1.0.0-beta.11`.
- Updated `azure-core-credential` dependency version to `1.0.0-beta.11`.
- Updated `azure-core-logging` dependency version to `1.0.0-beta.11`.

## 1.0.0-beta.10 (2022-03-08)

### Other changes

#### Dependency updates
- Updated `azure-core` dependency version to `1.0.0-beta.10`.
- Updated `azure-core-credential` dependency version to `1.0.0-beta.10`.
- Updated `azure-core-logging` dependency version to `1.0.0-beta.10`.

## 1.0.0-beta.9 (2021-11-08)

### Other changes

#### Dependency updates
- Updated `azure-core` dependency version to `1.0.0-beta.9`.
- Updated `azure-core-credential` dependency version to `1.0.0-beta.9`.
- Updated `azure-core-logging` dependency version to `1.0.0-beta.9`.

## 1.0.0-beta.8 (2021-10-08)

### Other changes

#### Dependency updates
- Updated `azure-core` dependency version to `1.0.0-beta.8`.
- Updated `azure-core-credential` dependency version to `1.0.0-beta.8`.
- Updated `azure-core-logging` dependency version to `1.0.0-beta.8`.

## 1.0.0-beta.7 (2021-09-08)

## 1.0.0-beta.6 (2021-06-07)

### Breaking Changes

- Replaced `azure.core.util.Context` in the `HttpPipeline::send` API with `azure.core.util.RequestContext`.
- Replaced `azure.core.util.Context` in the `HttpPipelinePolicyChain` type with `azure.core.util.RequestContext`.

## 1.0.0-beta.5 (2021-03-26)

### Breaking Changes

- Removed the `azure-core.properties` file.

## 1.0.0-beta.4 (2021-03-18)

- Initial release. Please see the README for information.
