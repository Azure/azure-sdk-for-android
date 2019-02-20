package com.azure.data.integration.common

/*
This file contains keys and database ids that MUST not be pushed to the git repository
Once you've downloaded the repository to your local machine, execute  following git
command to ensure that any changes to this file will be ignored by git:

git update-index --assume-unchanged azuredata/src/androidTest/java/com/azure/data/integration/common/DoNotDistribute.kt

if you need to undo this change, execute the following command:

git update-index --no-assume-unchanged azuredata/src/androidTest/java/com/azure/data/integration/common/DoNotDistribute.kt
 */

val azureCosmosDbAccount  = "" // whatever the Cosmos DB account is named in the Azure portal
val azureCosmosPrimaryKey = "" // one of the keys from the Cosmos DB account