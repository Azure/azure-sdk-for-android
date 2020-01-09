## storage-test-service

A light weight locally executable REST service that can be used to generate Storage SaS token to test mobile TransferManager implementations.

### Run the service locally:

1. From terminal switch to `storage-test-service` directory
2. Run `gradle clean build` to produce `az-mobile-storage-service-0.1.0.jar` under `.\build\lib`
3. create `settings.xml` in the current directory (i.e. in `storage-test-service`)

>> settings.xml
```xml
<?xml version = "1.0" encoding = "UTF-8"?>

<beans xmlns = "http://www.springframework.org/schema/beans"
       xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation = "http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
    <bean id = "settings" class = "azandroid.storage.applicationcontext.Settings">
        <property name = "connectionString"
                  value = "DefaultEndpointsProtocol=https;AccountName=<storage-account-name>;AccountKey=<account-key>;EndpointSuffix=core.windows.net"/>
    </bean>
</beans>
```

Update value of `connectionString::value` attribute with storage connection string.

4. Start the service

```
java  -jar build/libs/az-mobile-storage-service-0.1.0.jar
```

### Consume service from mobile emulators:

From Android emulator, `http://10.0.2.2:8080` can be used to reach the locally running service. To access from iOS emulator, use `http://localhost:8080`.

1. To create a container in the storage account configured in `settings.xml`, send request like:

```
POST http://10.0.2.2:8080/create-container
Content-Type: application/json
{
	"containerName": "mobilecontainer"
}
```

2. To generate a Sas token for a blob in the container, send request like:

```
POST http://10.0.2.2:8080/generate-sas
Content-Type: application/json
{
	"containerName": "mobilecontainer",
    "blobName":"image1.png"
    "accessDurationInMinutes": 60
}
```

service will return:

```json
{
    "containerName": "mobilecontainer",
    "blobName": "image1.png",
    "sasToken": "<access-token>",
    "sasUri": "https://<account-name>.blob.core.windows.net/mobilecontainer/image1.png?<access-token>"
}
```
