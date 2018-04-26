package com.azure.data.constants

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

// https://docs.microsoft.com/en-us/rest/api/documentdb/common-documentdb-rest-request-headers
// https://docs.microsoft.com/en-us/rest/api/documentdb/common-documentdb-rest-response-headers
enum class MSHttpHeader(val value: String, val description: String = "") {

    AIM("A-IM"),
    MSActivityId("x-ms-activity-id", "x-ms-activity-id: Represents a unique identifier for the operation. This echoes the value of the x-ms-activity-id request header, and commonly used for troubleshooting purposes."),
    MSAltContentPath("x-ms-alt-content-path", "x-ms-alt-content-path: The alternate path to the resource. Resources can be addressed in REST via system generated IDs or user supplied IDs. x-ms-alt-content-path represents the path constructed using user supplied IDs."),
    MSConsistencyLevel("x-ms-consistency-level"),
    MSContentPath("x-ms-content-path"),
    MSContinuation("x-ms-continuation", "x-ms-continuation: This header represents the intermediate state of query (or read-feed) execution, and is returned when there are additional results aside from what was returned in the response. Clients can resubmitted the request with a request header containingthe value of x-ms-continuation."),
    MSDate("x-ms-date"),
    MSDocumentDBIsQuery("x-ms-documentdb-isquery"),
    MSDocumentDBIsUpsert("x-ms-documentdb-is-upsert"),
    MSDocumentDBPartitionKey("x-ms-documentdb-partitionkey"),
    MSDocumentDBPartitionKeyRangeId("x-ms-documentdb-partitionkeyrangeid"),
    MSItemCount("x-ms-item-count", "x-ms-item-count: The number of items returned for a query or read-feed request."),
    MSLastStateChange("x-ms-last-state-change-utc"),
    MSMaxItemCount("x-ms-max-item-count"),
    MSRequestCharge("x-ms-request-charge", "x-ms-request-charge: This is the number of normalized requests a.k.a. request units (RU) for the operation. For more information, see Request units in Azure Cosmos DB."),
    MSResourceQuota("x-ms-resource-quota", "x-ms-resource-quota: Shows the allotted quota for a resource in an account."),
    MSResourceUsage("x-ms-resource-usage", "x-ms-resource-usage: Shows the current usage cout of a resource in an account. When deleting a resource, this shows the number of resources after the deletion."),
    MSRetryAfterMs("x-ms-retry-after-ms", "x-ms-retry-after-ms: The number of milliseconds to wait to retry the operation after an initial operation received HTTP status code 429 and was throttled."),
    MSSchemaVersion("x-ms-schemaversion", "x-ms-schemaversion: Shows the resource schema version number."),
    MSServiceVersion("x-ms-serviceversion", "x-ms-serviceversion: Shows the service version number."),
    MSSessionToken("x-ms-session-token", "x-ms-session-token: The session token of the request. For session consistency, clients must echo this request via the x-ms-session-token request header for subsequent operations made to the corresponding collection."),
    MSVersion("x-ms-version")
}