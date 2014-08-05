#Microsoft Azure SDK for Android

This project provides a client library that makes it easy to consume Microsoft Azure services from the Android platform. For documentation please see the [AndroidDocs](http://dl.windowsazure.com/androiddocs/).

#Features

* Service Management
    * Compute Management
    * Web Site Management
    * Virtual Network Management
    * Storage Management
    * Sql Database Management

#Getting Started

##Download
###Option 1: Via Git

To get the source code of the SDK via git just type:

    git clone git://github.com/Azure/azure-sdk-for-android.git
    cd ./azure-sdk-for-android/
    mvn compile

###Option 2: Via Maven

To get the binaries of this library as distributed by Microsoft, ready for use within your project, you can use Maven.

```xml
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>azure-android</artifactId>
  <version>0.1.0</version>
</dependency>
```

###Option 3: aar via Gradle
To get the binaries of this library as distributed by Microsoft, ready for use within your project, you can use Gradle.

First, add mavenCentral to your repositories by adding the following to your gradle build file:


    repositories {
        mavenCentral()
    }

Then, add a dependency by adding the following to your gradle build file:

    dependencies {
        compile 'com.microsoft.azure.android:azure-android:0.1.0@aar'
    }

##Minimum Requirements

* Jackson-Core is used for JSON parsing.
* Android 4.0/15+
* (Optional) Gradle or Maven
This library is currently tested to work on Android versions 4.0+. Compatibility with older versions is not guaranteed.

##Usage

To use this SDK to call Microsoft Azure services, you need to first create an
account.

Make sure the client library is added as a project dependency. If using source and in Eclipse, right click on the project, select "Properties", navigate to the Android tab, and under "Library" click "Add.." and select the project. To do this, the client library should already be imported into Eclipse as an Android project.

If using Maven or Gradle, Jackson-Core should be automatically added to the build path. Otherwise, please download the jar and add it to your build path. Also, please make sure that the jar will be added to your project's apk. To do this in Eclipse, right click your project, select "Build Path->Configure Build Path", navigate to the "Order and Export" tab and check the box next to the jackson-core jar.

#Need Help?

Be sure to check out the Microsoft Azure [Developer Forums on Stack Overflow](http://go.microsoft.com/fwlink/?LinkId=234489) if you have trouble with the provided code.

#Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.com/guidelines.html).

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/Azure/azure-sdk-for-android/issues) section of the project.

#Learn More

* [AndroidDocs](http://dl.windowsazure.com/androiddocs/)

