# Release History

## 1.0.0-beta.9 (Unreleased)


## 1.0.0-beta.8 (2021-10-08)

### Bugs Fixed
- `DefaultLogger` will no longer cause the Android `Log` API to throw an exception when using an instance created with a tag longer than 23 characters for Android API levels <= 25; instead, it will shorten the tag as best as possible in such cases. For example: 
  - org.example.project.MyClass -> o*.e*.p*.MyClass
  - org.example.project.subproject.MyClass -> o*.e*.p*.s*.MyClass
  - org.example.MyQuiteLongNamedClassOfTooMuchCharacters -> o*.e*.MyQuiteLongNamed*
  - o.e.project.subproject.MyClass -> o.e.p*.s*.MyClass
  - MyQuiteLongNamedClassNotInAPackage -> MyQuiteLongNamedClassN*

## 1.0.0-beta.7 (2021-09-08)

## 1.0.0-beta.6 (2021-06-07)

## 1.0.0-beta.5 (2021-03-26)

### Breaking Changes

- Removed the `azure-core.properties` file.

## 1.0.0-beta.4 (2021-03-18)

- Initial release. Please see the README for information.
