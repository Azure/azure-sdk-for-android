# What is this folder

This folder contains tests for autorest.android.

Code in \app\src\main folder contains generated code with autorest.android for different swagger files in TestServer.

Code in app\src\androidTest folder contains test code that runs in an Android emulator for testing different cases.

# Regenerate Fixtures

generate-android.bat in parent folder can be used to regenerate code in \app\src\main. Modify it to add or remove swagger files you are interested in testing.

After generating the client code for the swagger, write your tests in a file inside \app\src\androidTest\java\com\azure\autoresttest folder.

# Run Android Tests

## Start TestServer
Clone https://github.com/Azure/autorest.testserver.git to a local folder. Open a command line window and navigate to the folder. Run this command to start the server.
```ps
npm run start
```

By default a webserver will be started listening on "localhost:3000"

## Run Tests

Modify network_security_config.xml in \app\src\main\res\xml to add your machine id to the domain list. For example
```xml
<domain includeSubdomains="true">IPV4 Address Of Your Machine Running TestServer</domain>
```

Modify \app\src\androidTest\java\com\azure\autoresttest\TestConstants.java to set TestServerRootUrl to the correct value.
```java
public static String TestServerRootUrl = "http://<IPV4 Address Of Your Machine Running TestServer>:3000";
```

In Android Studio, open this folder as a project. Open up any of the test files in \app\src\androidTest\java\com\azure\autoresttest  to run or debug the tests in the test file.
