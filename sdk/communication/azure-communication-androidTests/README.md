# Azure Communication SDK Tests

This module is intended for Android integration tests of Azure Communication Services SDK for Android.
 
## Key Concepts

Integration tests often contain site url, credentials, and other secrets that should not be checked into code repository. It is expected that these values be stored in local.properties file for each user on each run of the tests.

## Getting started

### Create a local property

For each of the secrets, create a property in local.properties, for example,

myTest.mySecret=MySecretValue

### Create a resource string for each local property

Create a resource string in build.gradle, for example,

```gradle
// build.gradle
android {
    def Properties properties = new Properties()
    properties.load(project.rootProject.file("local.properties").newDataInputStream())

    defaultConfig {
        resValue "string", "myTest_mySecret", properties.getProperty("myTest.mySecret", "")
    }
}
```

### Reference Secret from Resource

Wherever in code a secret is needed, reference it by resource string, for example,

```java
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import com.azure.myModule.R;

String mySecret = getInstrumentation().getContext().getResources().getString(R.string.myTest_mySecret);
```
