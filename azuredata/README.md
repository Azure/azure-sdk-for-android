# AzureData

 ![Current State: Preview Release](https://img.shields.io/badge/Current_State-Preview_Release-brightgreen.svg)[ ![Download](https://api.bintray.com/packages/azure/Azure.Android/azuredata/images/download.svg) ](https://bintray.com/azure/Azure.Android/azuredata/_latestVersion)

AzureData is an Android client SDK for Microsoft's [Azure Cosmos DB](https://aka.ms/cosmosdb) [SQL API](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-introduction) (previously DocumentDB), written in Kotlin.

# Configure

Before making calls to AzureData, you'll need to call one of the `AzureData.configure` methods.  We recommend doing this in your application class or main activity's `OnCreate`, etc.:

```kotlin
override fun onCreate() {
    super.onCreate()

    AzureData.configure(applicationContext, "<Account Name>", "<Account Master Key>", PermissionMode.All /* or PermissionMode.Read */)
}
```


# Usage

## Operations

| Resource                                              | Create                                                | List                                                  | Get                                                   | Delete                                                | Replace                                               | Query                                                 | Execute                                               |
| ----------------------------------------------------- | ----------------------------------------------------- | ----------------------------------------------------- | ----------------------------------------------------- | ----------------------------------------------------- | ----------------------------------------------------- | ----------------------------------------------------- | ----------------------------------------------------- |
| **[Databases](#databases)**                           | [Create](#create)                                     | [List](#list)                                         | [Get](#get)                                           | [Delete](#delete)                                     | *                                                     | *                                                     | *                                                     |
| **[Collections](#collections)**                       | [Create](#create-1)                                   | [List](#list-1)                                       | [Get](#get-1)                                         | [Delete](#delete-1)                                   | [Replace](#replace)                                   | *                                                     | *                                                     |
| **[Documents](#documents)**                           | [Create](#create-2)                                   | [List](#list-2)                                       | [Get](#get-2)                                         | [Delete](#delete-2)                                   | [Replace](#replace-1)                                 | [Query](#query)                                       | *                                                     |
| **[Attachments](#attachments)**                       | [Create](#create-3)                                   | [List](#list-3)                                       | *                                                     | [Delete](#delete-3)                                   | [Replace](#replace-2)                                 | *                                                     | *                                                     |
| **[Stored Procedures](#stored-procedures)**           | [Create](#create-4)                                   | [List](#list-4)                                       | *                                                     | [Delete](#delete-4)                                   | [Replace](#replace-3)                                 | *                                                     | [Execute](#execute)                                   |
| **[User Defined Functions](#user-defined-functions)** | [Create](#create-5)                                   | [List](#list-5)                                       | *                                                     | [Delete](#delete-5)                                   | [Replace](#replace-4)                                 | *                                                     | *                                                     |
| **[Triggers](#triggers)**                             | [Create](#create-6)                                   | [List](#list-6)                                       | *                                                     | [Delete](#delete-6)                                   | [Replace](#replace-5)                                 | *                                                     | *                                                     |
| **[Users](#users)**                                   | [Create](#create-7)                                   | [List](#list-7)                                       | [Get](#get-3)                                         | [Delete](#delete-7)                                   | [Replace](#replace-6)                                 | *                                                     | *                                                     |
| **[Permissions](#permissions)**                       | [Create](#create-8)                                   | [List](#list-8)                                       | [Get](#get-4)                                         | [Delete](#delete-8)                                   | [Replace](#replace-7)                                 | *                                                     | *                                                     |
| **[Offers](#offers)**                                 | *                                                     | [List](#list-9)                                       | [Get](#get-5)                                         | *                                                     | [Replace](#replace-8)                                 | [Query](#query-1)                                     | *                                                     |


_* not applicable to resource type_


### Databases

#### Create

```kotlin
AzureData.createDatabase (databaseId) {
    // database = it.resource
}
```

#### List

```kotlin
AzureData.getDatabases {
    // databases = it.resource?.items
}
```

#### Get

```kotlin
AzureData.getDatabase (databaseId) {
    // database = it.resource
}
```

#### Delete

```kotlin
AzureData.deleteDatabase (databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteDatabase (database) {
    // successfully deleted == it.isSuccessful
}

database.delete {
    // successfully deleted == it.isSuccessful
}
```


### Collections

#### Create

```kotlin
AzureData.createCollection (collectionId, databaseId) {
    // collection = it.resource
}

database.create (collectionId) {
    // collection = it.resource
}
```

#### List

```kotlin
AzureData.getCollections (databaseId) {
    // collections = it.resource?.items
}

database.getCollections {
    // collections = it.resource?.items
}
```

#### Get

```kotlin
AzureData.getCollection (collectionId, databaseId) {
    // collection = it.resource
}

database.getCollection (collectionId) {
    // collection = it.resource
}
```

#### Delete

```kotlin
AzureData.deleteCollection (collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

database.deleteCollection (collection) {
    // successfully deleted == it.isSuccessful
}

database?.deleteCollection(collectionId) {
    // successfully deleted == it.isSuccessful
}

collection.delete {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

A `Replace` operation on a `DocumentCollection` allows you to replace the `IndexingPolicy` for the collection.

Given a created `IndexingPolicy`:

```kotlin
val policy = IndexingPolicy.create {
    automatic = true
    mode = IndexingMode.Lazy
    includedPaths {
        includedPath {
            path = "/*"
            indexes {
                // create indexes via factory methods
                index(Index.range(DataType.Number, -1))
                // or, by specifying each member
                index {
                    kind = IndexKind.Hash
                    dataType = DataType.String
                    precision = 3
                }
                index(Index.spatial(DataType.Point))
            }
        }
    }
    // omit if no paths should be excluded
    excludedPaths {
        excludedPath {
            path = "/test/*"
        }
    }
}
```

```kotlin
AzureData.replaceCollection(resourceId, databaseId, policy) {
    // replaced collection = it.resource
}
```


### Documents

There are two different classes you can use to interact with documents:

#### Document

The `Document` type is intended to be inherited by your custom document model types.

Here is an example of a class `CustomDocument` that inherits from `Document`:

```kotlin
class CustomDocument(id: String? = null) : Document(id) {

    var customString = "My Custom String"
    var customNumber = 123000
    var customDate: Date = Date()
    var customBool = true
    var customArray = arrayOf(1, 2, 3)
    var customObject: User? = User()
}
```

#### DictionaryDocument

The `DictionaryDocument` type behaves very much like a `<String, Any?>` Map while handling all properties required by the database.  This allows you to interact with the document directly using subscript/indexing syntax.  `DictionaryDocument` cannot be subclassed.

Here is an example of using `DictionaryDocument` to create a document with the same properties as the `CustomDocument` above:

```kotlin
val document = DictionaryDocument()

document["customString"] = "My Custom String"
document["customNumber"] = 123000
document["customDate"] = Date()
document["customBool"] = true
document["customArray"] = arrayOf(1, 2, 3)
document["customObject"] = User()
```

##### Limitations

When using `DictionaryDocument`, the data is subject to the limitations of json's lack of typing.  This means that when the above `DictionaryDocument` is deserialized, the deserializer won't know the specific types for your data.  In practice, this means the following types of data may appear differently once they've been "round tripped":

| Data Type  |   Roundtrip Data Type       | Sample Conversion |
| ---------- | --------------------------- | ----------------- |
| Number types (Int, Long, etc.) | `Number` | `(document["customNumber"] as Number).toInt()` |
| Array/List types | `ArrayList<*>`<br/>`ArrayList<Any?>` | `document["customArray"] as ArrayList<*>` |
| Object types | `Map<*,*>`<br/>`Map<String, Any?>` | `document["customObject"] as Map<*,*>` |

Due to these limitations, we recommend only using `DictionaryDocument` for simple data types and/or rapid prototyping.  Subclassing `Document`, as shown above, will yield much better results with proper typing based on the structure of your document class.

#### Create

```kotlin
// ridiculous code to get a Date using Calendar API
val cal = Calendar.getInstance()
cal.set(Calendar.YEAR, 1988)
cal.set(Calendar.MONTH, Calendar.JANUARY)
cal.set(Calendar.DAY_OF_MONTH, 1)
val customDateValue = cal.time

// Create Document

val document = CustomDocument() //optionally specify an Id here, otherwise it will be generated

document.customDate = customDateValue
document.customNumber = 1_000_000

// or

val document = DictionaryDocument() //optionally specify an Id here, otherwise it will be generated
            
document["customDate"] = customDateValue
document["customNumber"] = 1_000_000

// Document creation in CosmosDB

AzureData.createDocument (document, collectionId, databaseId) {
    // created document = it.resource
}

AzureData.createDocument (document, collection) {
    // created document = it.resource
}

collection.createDocument (document) {
    // created document = it.resource
}
```

#### List

```kotlin
AzureData.getDocuments (collectionId, databaseId, CustomDocument::class.java) {
    // documents = it.resource?.items
}

AzureData.getDocuments (collection, CustomDocument::class.java) {
    // documents = it.resource?.items
}

collection.getDocuments (CustomDocument::class.java) {
    // documents = it.resource?.items
}
```

#### Get

```kotlin
AzureData.getDocument (documentId, collectionId, databaseId, CustomDocument::class.java) {
    // document = it.resource
}

AzureData.getDocument (documentId, collection, CustomDocument::class.java) {
    // document = it.resource
}

collection.getDocument (documentId, CustomDocument::class.java) {
    // document = it.resource
}
```

#### Delete

```kotlin
AzureData.deleteDocument (document, collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteDocument (document, collection) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteDocument (documentId, collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

collection.deleteDocument (document) {
    // successfully deleted == it.isSuccessful
}

collection.deleteDocument (documentId) {
    // successfully deleted == it.isSuccessful
}

document.delete {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

```kotlin
AzureData.replaceDocument (document, collectionId, databaseId) {
    // updated document = it.resource
}

AzureData.replaceDocument (document, collection) {
    // updated document = it.resource
}

collection.replaceDocument (document) {
    // updated document = it.resource
}
```

#### Query

```kotlin
val query = Query.select()
                .from(collectionId)
                .where("firstName", "Faiçal")
                .andWhere("lastName", "Tchirou")
                .andWhereGreaterThanEqual("age", 20)
                .orderBy("_etag", true) // descending = true/false

AzureData.queryDocuments (collectionId, databaseId, query, CustomDocument::class.java) {
    // matching documents = it.resource?.items
}

AzureData.queryDocuments (collection, query, CustomDocument::class.java) {
    // matching documents = it.resource?.items
}

collection.queryDocuments (query, CustomDocument::class.java) {
    // matching documents = it.resource?.items
}
```


### Attachments

#### Create

Link to existing external media asset:

```kotlin
AzureData.createAttachment (attachmentId, "image/jpeg", mediaUrl, documentId, collectionId, databaseId) {
    // attachment = it.resource
}

document.createAttachment (attachmentId, "image/jpeg", mediaUrl) {
    // attachment = it.resource
}
```

`mediaUrl` can be of type `HttpUrl` (from OkHttp), `URL`, or a string url.

...or upload the media directly:

```kotlin
AzureData.createAttachment (attachmentId, "image/jpeg", data, documentId, collectionId, databaseId) {
    // attachment = it.resource
}

document.createAttachment (attachmentId, "image/jpeg", data) {
    // attachment = it.resource
}

```

`data` here is a `ByteArray` containing the bytes for the media/blob, and "image/jpeg" is the content type of the blob.

#### List

```kotlin
AzureData.getAttachments (documentId, collectionId, databaseId) {
    // attachments = it.resource?.items
}

document.getAttachments {
    // attachments = it.resource?.items
}
```

#### Delete

```kotlin
AzureData.deleteAttachment (attachmentId, documentId, collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteAttachment (attachment, documentId, collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

document.deleteAttachment (attachment) {
    // successfully deleted == it.isSuccessful
}

document.deleteAttachment (attachmentId) {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

Link to existing external media asset:

```kotlin
AzureData.replaceAttachment (attachmentId, "image/jpeg", mediaUrl, documentId, collectionId, databaseId) {
    // replaced attachment = it.resource
}

document.replaceAttachment (attachmentId, "image/jpeg", url) {
    // replaced attachment = it.resource
}

```

`mediaUrl` can be of type `HttpUrl` (from OkHttp), `URL`, or a string url.

...or upload the media directly:

```kotlin
AzureData.replaceAttachment (attachmentId, "image/jpeg", data, documentId, collectionId, databaseId) {
    // replaced attachment = it.resource
}

document.replaceAttachment (attachmentId, "image/jpeg", data) {
    // replaced attachment = it.resource
}

```

`data` here is a `ByteArray` containing the bytes for the media/blob, and "image/jpeg" is the content type of the blob.


### Stored Procedures

#### Create

Given a stored procedure body:

```kotlin
val storedProcedureBody = """
        function () {
            var context = getContext();
            var r = context.getResponse();

            r.setBody('Hello World!');
        }
        """
```

A Stored Procedure can be created like so:

```kotlin
AzureData.createStoredProcedure (storedProcedureId, storedProcedureBody, collectionId, databaseId) {
    // storedProcedure = it.resource
}

AzureData.createStoredProcedure (storedProcedureId, storedProcedureBody, collection) {
    // storedProcedure = it.resource
}

collection.createStoredProcedure (storedProcedureId, storedProcedureBody) {
    // storedProcedure = it.resource
}
```

#### List

```kotlin
AzureData.getStoredProcedures (collectionId, databaseId) {
    // storedProcedures = it.resource?.items
}

AzureData.getStoredProcedures (collection) {
    // storedProcedures = it.resource?.items
}

collection.getStoredProcedures {
    // storedProcedures = it.resource?.items
}
```

#### Delete

```kotlin
AzureData.deleteStoredProcedure (storedProcedureId, collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteStoredProcedure (storedProcedureId, collection) {
    // successfully deleted == it.isSuccessful
}

collection.deleteStoredProcedure (storedProcedure) {
    // successfully deleted == it.isSuccessful
}

collection.deleteStoredProcedure (storedProcedureId) {
    // successfully deleted == it.isSuccessful
}

storedProcedure.delete {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

```kotlin
AzureData.replaceStoredProcedure (storedProcedureId, storedProcedureBody, collectionId, databaseId) {
    // storedProcedure = it.resource
}

AzureData.replaceStoredProcedure (storedProcedureId, storedProcedureBody, collection) {
    // storedProcedure = it.resource
}

AzureData.replaceStoredProcedure (storedProcedure, collection) {
    // storedProcedure = it.resource
}

collection.replaceStoredProcedure (storedProcedureId, storedProcedureBody) {
    // storedProcedure = it.resource
}
```

#### Execute

```kotlin
AzureData.executeStoredProcedure (storedProcedureId, parameters, collectionId, databaseId) {
    // raw response data = it.resource
}

AzureData.executeStoredProcedure (storedProcedureId, parameters, collection) {
    // raw response data = it.resource
}

collection.executeStoredProcedure (storedProcedureId, parameters) {
    // raw response data = it.resource
}
```


### User Defined Functions

#### Create

Given a user defined function body:

```
val udfBody = """
        function (input) { 
            return input.toLowerCase() 
        }
        """
```

A user defined function can be created like so:

```kotlin
AzureData.createUserDefinedFunction (userDefinedFunctionId, udfBody, collectionId, databaseId) {
    // userDefinedFunction = it.resource
}

AzureData.createUserDefinedFunction (userDefinedFunctionId, udfBody, collection) {
    // userDefinedFunction = it.resource
}

collection.createUserDefinedFunction (userDefinedFunctionId, udfBody) {
    // userDefinedFunction = it.resource
}
```

#### List

```kotlin
AzureData.getUserDefinedFunctions (collectionId, databaseId) {
    // userDefinedFunctions = it.resource?.items
}

AzureData.getUserDefinedFunctions (collection) {
    // userDefinedFunction = it.resource?.items
}

collection.getUserDefinedFunctions {
    // userDefinedFunctions = it.resource?.items
}
```

#### Delete

```kotlin
AzureData.deleteUserDefinedFunction (userDefinedFunctionId, collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteUserDefinedFunction (userDefinedFunctionId, collection) {
    // successfully deleted == it.isSuccessful
}

collection.deleteUserDefinedFunction (userDefinedFunction) {
    // successfully deleted == it.isSuccessful
}

collection.deleteUserDefinedFunction (userDefinedFunctionId) {
    // successfully deleted == it.isSuccessful
}

userDefinedFunction.delete {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

```kotlin
AzureData.replaceUserDefinedFunction (userDefinedFunctionId, userDefinedFunctionBody, collectionId, databaseId) {
    // userDefinedFunction = it.resource
}

AzureData.replaceUserDefinedFunction (userDefinedFunctionId, userDefinedFunctionBody, collection) {
    // userDefinedFunction = it.resource
}

collection.replaceUserDefinedFunction (userDefinedFunctionId, userDefinedFunctionBody) {
    // userDefinedFunction = it.resource
}
```


### Triggers

#### Create

Given a trigger body:

```kotlin
val triggerBody = """
        function updateMetadata() {}
        """
```

A trigger can be created like so:

```kotlin
AzureData.createTrigger (triggerId, triggerOperation, triggerType, triggerBody, collectionId, databaseId) {
    // trigger = it.resource
}

AzureData.createTrigger (triggerId, triggerOperation, triggerType, triggerBody, collection) {
    // trigger = it.resource
}

collection.createTrigger (triggerId, triggerOperation, triggerType, triggerBody) {
    // trigger = it.resource
}
```

#### List

```kotlin
AzureData.getTriggers (collectionId, databaseId) {
    // triggers = it.resource?.items
}

AzureData.getTriggers (collection) {
    // triggers = it.resource?.items
}

collection.getTriggers {
    // triggers = it.resource?.items
}
```

#### Delete

```kotlin
AzureData.deleteTrigger (triggerId, collectionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteTrigger (triggerId, collection) {
    // successfully deleted == it.isSuccessful
}

collection.deleteTrigger (trigger) {
    // successfully deleted == it.isSuccessful
}

collection.deleteTrigger (triggerId) {
    // successfully deleted == it.isSuccessful
}

trigger.delete {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

```kotlin
AzureData.replaceTrigger (triggerId, triggerOperation, triggerType, triggerBody, collectionId, databaseId) {
    // trigger = it.resource
}

AzureData.replaceTrigger (triggerId, triggerOperation, triggerType, triggerBody, collection) {
    // trigger = it.resource
}

collection.replaceTrigger (triggerId, triggerOperation, triggerType, triggerBody) {
    // trigger = it.resource
}
```


### Users    

#### Create

```kotlin
AzureData.createUser (userId, databaseId) {
    // user = it.resource
}

database.createUser (userId) {
    // user = it.resource
}

```

#### List

```kotlin
AzureData.getUsers (databaseId) {
    // users = it.resource?.items
}

database.getUsers {
    // users = it.resource?.items
}
```

#### Get

```kotlin
AzureData.getUser (userId, databaseId) {
    // user = it.resource
}

database.getUser (userId) {
    // user = it.resource
}
```

#### Delete

```kotlin
AzureData.deleteUser (userId, databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deleteUser (userId, database) {
    // successfully deleted == it.isSuccessful
}

database.deleteUser (userId) {
    // successfully deleted == it.isSuccessful
}

database.deleteUser (user) {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

```kotlin
AzureData.replaceUser (userId, newUserId, databaseId) {
    // user = it.resource
}

AzureData.replaceUser (userId, newUserId, database) {
    // user = it.resource
}

database.replaceUser (userId, newUserId) {
    // user = it.resource
}
```


### Permissions  

#### Create

```kotlin
AzureData.createPermission (permissionId, permissionMode, resource, userId, databaseId) {
    // permission = it.resource
}

AzureData.createPermission (permissionId, permissionMode, resource, user) {
    // permission = it.resource
}

<resource>.createPermission (permissionId, permissionMode, user) {
    // permission = it.resource
}

user.createPermission (permissionId, permissionMode, resource) {
    // permission = it.resource
}
```

#### List

```kotlin
AzureData.getPermissions (userId, databaseId) {
    // permissions = it.resource?.items
}

AzureData.getPermissions (user) {
    // permissions = it.resource?.items
}

user.getPermissions {
    // permissions = it.resource?.items
}
```

#### Get

```kotlin
AzureData.getPermission (permissionId, userId, databaseId) {
    // permission = it.resource
}

AzureData.getPermission (permissionId, user) {
    // permission = it.resource
}

user.getPermission (permissionId) {
    // permission = it.resource
}
```

#### Delete

```kotlin
AzureData.deletePermission (permissionId, userId, databaseId) {
    // successfully deleted == it.isSuccessful
}

AzureData.deletePermission (permissionId, user) {
    // successfully deleted == it.isSuccessful
}

user.deletePermission (permissionId, databaseId) {
    // successfully deleted == it.isSuccessful
}

user.deletePermission (permission) {
    // successfully deleted == it.isSuccessful
}

user.deletePermission (permissionId) {
    // successfully deleted == it.isSuccessful
}

permission.delete {
    // successfully deleted == it.isSuccessful
}
```

#### Replace

```kotlin
AzureData.replacePermission (permissionId, permissionMode, resource, userId, databaseId) {
    // replaced permission = it.resource
}

AzureData.replacePermission (permissionId, permissionMode, resource, user) {
    // replaced permission = it.resource
}

AzureData.replacePermission (permission, user) {
    // replaced permission = it.resource
}

user.replacePermission (permissionId, permissionMode, resource) {
    // replaced permission = it.resource
}
```


### Offers

#### List

```kotlin
AzureData.getOffers {
    // offers = it.resource?.items
}
```

#### Get

```kotlin
AzureData.getOffer (offerId) {
    // offer = it.resource
}
```

#### Replace

```kotlin
// TODO...
```

#### Query

```kotlin
// TODO...
```
   

## General Information

### Responses

All operations defined above will return a response that has the following properties:

| Property      |                    Value                 |
| ------------- | -----------------------------------------|
| `isSuccessful` | Returns `true` if the result is a success, `false` otherwise. |
| `isErrored` | Returns `true` if the result is an error, `false` otherwise. |
| `error` | Returns the associated error value if the result if it is a failure, null otherwise. |
| `jsonData` | The json data returned by the server (if applicable) |
| `request` | The (OkHttp) request object sent to the server. (If available) |
| `response` | The (OkHttp) response object returned from the server. (If available) |
| `resource` | For operations that return a resource or list of resources, this will contain that (typed) result. |

### Pagination of results

All `list` and `get` functions support pagination through the optional `maxPerPage` parameter and the `next` function on the response object. The `maxPerPage`  parameter specifies the maximum number of items that can be returned by the function. This can be between 1 and 1000. If omitted, Azure CosmosDB uses a default of 100.

```kotlin
AzureData.getDocuments(collection, CustomDocument::class.java, maxPerPage = 20) { 
    // it.resource?.items.count() <= 20

    if (it.hasMoreResults) {
        it.next {
            // next items = it.resource?.items
        }
    }
}
```


### Controlling Serialization

AzureData constructs and maintains its own [Gson](https://github.com/google/gson) instance that handles serializing and deserializing all Cosmos DB data.  Some projects may need to provide custom type adapters or otherwise hook into the serialization process.  This can be accomplished at configure-time, by passing a callback to the `configure` method:

```kotlin
AzureData.configure(this, <Cosmos DB account name>, <Cosmos DB Key>, PermissionMode.All) { gsonBuilder ->
    gsonBuilder.registerTypeAdapter(MyClass::class.java, MyTypeAdapter())
    ...
}
```

Any modifications made to the `GsonBuilder` during configuration will be used to construct the `Gson` instance.

### Using from Java

As noted, this library is written in and optimized for Kotlin.  If your app is written in Java, it's still possible to use this library (assuming [your app targets JDK 1.8](https://developer.android.com/studio/write/java8-support.html)), with a few syntactical differences to the sample code found above:

* Callbacks in Java will be in lambda form and passed as an argument to the method.
* Due to [some compiler intricacies](https://stackoverflow.com/questions/37828790/why-do-i-have-to-return-unit-instance-when-implementing-in-java-a-kotlin-functio) with the way lambdas returning `Unit` (void in Java) are interpreted in Java, the callbacks from the [operations](#operations) either need to return `Unit.INSTANCE` or be wrapped in something that handles that for you.

Example: To get the [list of databases](#list), the call would look like:

```java
AzureData.getDatabases(response -> {
    if (response.isSuccessful()) {
        Database[] dbs = response.getResource().getItems();
    }
    ...
    return Unit.INSTANCE;
});
```

For an improved development experience, a functional wrapper has been added to make this a bit cleaner:

```java
AzureData.getDatabases(onCallback(response -> {
    if (response.isSuccessful()) {
        Database[] dbs = response.getResource().getItems();
    }
    ...
}));
```

`onCallback()` is found in the `com.microsoft.azureandroid.data.util` package, and will in essence 'inject' the return statement for you and remove the need to end your callback with a returned `Unit.INSTANCE`.

**Note:** `OnCallback` requires API level 24+

[See here](https://github.com/NateRickard/cosmos_db_example) for a complete example using this library from Java.


# Offline Capabilities

When offline capabilities are enabled, data returned by any `list` or `get` requests is cached locally. When the device is offline, `list` and `get` requests return the data cached locally.

## Enabling Offline Capabilities

The offline capabilities of Azure are enabled by setting the property `offlineDataEnabled` of `AzureData` to `true`.

```swift
AzureData.offlineDataEnabled = true
```

## Security

If, for security reasons you need to encrypt the data returned by `AzureData` before it is cached locally, you can do so by setting a custom `ResourceEncryptor` to `AzureData`.

```kotlin
class AES128Encryptor: ResourceEncryptor {
    /// Encrypts the data before it's cached locally.
    fun encrypt(data: String) -> String {
        // ...
    }

    /// Decrypts the cached encrypted data
    /// when it's being read.
    func decrypt(data: String) -> String {
        // ...
    }
}
```

```swift
/// Data will be encrypted using AES128ResourceEncrytpor before it's cached locally.
AzureData.resourceEncryptor = AES128ResourceEncryptor()
```