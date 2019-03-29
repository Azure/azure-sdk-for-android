package com.azure.data.constants

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

// https://docs.microsoft.com/en-us/rest/api/documentdb/common-documentdb-rest-request-headers
// https://docs.microsoft.com/en-us/rest/api/documentdb/common-documentdb-rest-response-headers
enum class MSHttpHeader(val value: String, val description: String = "") {

    /**
     * Indicates a change feed request. Must be set to "Incremental feed", or omitted otherwise.
     */
    AIM("A-IM"),
    /**
     * A client supplied identifier for the operation, which is echoed in the server response. The recommended value is a unique identifier.
     */
    MSActivityId("x-ms-activity-id", "x-ms-activity-id: Represents a unique identifier for the operation. This echoes the value of the x-ms-activity-id request header, and commonly used for troubleshooting purposes."),
    /**
     * The alternate path to the resource. Resources can be addressed in REST via system generated IDs or user supplied IDs. x-ms-alt-content-path represents the path constructed using user supplied IDs.
     */
    MSAltContentPath("x-ms-alt-content-path", "x-ms-alt-content-path: The alternate path to the resource. Resources can be addressed in REST via system generated IDs or user supplied IDs. x-ms-alt-content-path represents the path constructed using user supplied IDs."),
    /**
     * The consistency level override for read options against documents and attachments. The valid values are: Strong, Bounded, Session, or Eventual (in order of strongest to weakest). The override must be the same or weaker than the account's configured consistency level.
     */
    MSConsistencyLevel("x-ms-consistency-level"),
    MSContentPath("x-ms-content-path"),
    /**
     * A string token returned for queries and read-feed operations if there are more results to be read. Clients can retrieve the next page of results by resubmitting the request with the x-ms-continuation request header set to this value.
     */
    MSContinuation("x-ms-continuation", "x-ms-continuation: This header represents the intermediate state of query (or read-feed) execution, and is returned when there are additional results aside from what was returned in the response. Clients can resubmitted the request with a request header containingthe value of x-ms-continuation."),
    /**
     * The date of the request per RFC 1123 date format expressed in Coordinated Universal Time, for example, Fri, 08 Apr 2015 03:52:31 GMT.
     */
    MSDate("x-ms-date"),
    MSDocumentDBIsQuery("x-ms-documentdb-isquery"),
    /**
     * If set to true, Cosmos DB creates the document with the ID (and partition key value if applicable) if it doesn’t exist, or update the document if it exists.
     */
    MSDocumentDBIsUpsert("x-ms-documentdb-is-upsert"),
    /**
     * The partition key value for the requested document or attachment operation. Required for operations against documents and attachments when the collection definition includes a partition key definition. Supported in API versions 2015-12-16 and newer. Currently, the SQL API supports a single partition key, so this is an array containing just one value.
     */
    MSDocumentDBPartitionKey("x-ms-documentdb-partitionkey"),
    /**
     * If the collection is partitioned, this must be set to True to allow execution across multiple partitions. Queries that filter against a single partition key, or against single-partitioned collections do not need to set the header.
     */
    MSDocumentDBQueryEnableCrossPartition("x-ms-documentdb-query-enablecrosspartition", "If the collection is partitioned, this must be set to True to allow execution across multiple partitions. Queries that filter against a single partition key, or against single-partitioned collections do not need to set the header."),
    /**
     * Used in change feed requests. The partition key range ID for reading data.
     */
    MSDocumentDBPartitionKeyRangeId("x-ms-documentdb-partitionkeyrangeid"),
    /**
     * The acceptable value is Include or Exclude.

     * - Include adds the document to the index.
     * - Exclude omits the document from indexing.

     * The default for indexing behavior is determined by the automatic property’s value in the indexing policy for the collection.
     */
    MSIndexingDirective("x-ms-indexing-directive"),
    /**
     * The number of items returned for a query or read-feed request.
     */
    MSItemCount("x-ms-item-count", "x-ms-item-count: The number of items returned for a query or read-feed request."),
    MSLastStateChange("x-ms-last-state-change-utc"),
    /**
     * An integer indicating the maximum number of items to be returned per page. An x-ms-max-item-count of -1 can be specified to let the service determine the optimal item count. This is the recommended configuration value for x-ms-max-item-count.
     */
    MSMaxItemCount("x-ms-max-item-count"),
    /**
     * Used to set the throughput value upon Database creation; accepts a number that increments by units of 100
     */
    MSOfferThroughput("x-ms-offer-throughput", "Used to set the throughput value upon Database creation; accepts a number that increments by units of 100"),
    /**
     * This is the number of normalized requests a.k.a. request units (RU) for the operation.
     */
    MSRequestCharge("x-ms-request-charge", "x-ms-request-charge: This is the number of normalized requests a.k.a. request units (RU) for the operation. For more information, see Request units in Azure Cosmos DB."),
    /**
     * Shows the allotted quota for a resource in an account.
     */
    MSResourceQuota("x-ms-resource-quota", "x-ms-resource-quota: Shows the allotted quota for a resource in an account."),
    /**
     * Shows the current usage count of a resource in an account. When deleting a resource, this shows the number of resources after the deletion.
     */
    MSResourceUsage("x-ms-resource-usage", "x-ms-resource-usage: Shows the current usage cout of a resource in an account. When deleting a resource, this shows the number of resources after the deletion."),
    /**
     * The number of milliseconds to wait to retry the operation after an initial operation received HTTP status code 429 and was throttled.
     */
    MSRetryAfterMs("x-ms-retry-after-ms", "x-ms-retry-after-ms: The number of milliseconds to wait to retry the operation after an initial operation received HTTP status code 429 and was throttled."),
    /**
     * Shows the resource schema version number.
     */
    MSSchemaVersion("x-ms-schemaversion", "x-ms-schemaversion: Shows the resource schema version number."),
    /**
     * Shows the service version number.
     */
    MSServiceVersion("x-ms-serviceversion", "x-ms-serviceversion: Shows the service version number."),
    /**
     * A string token used with session level consistency.  The session token of the request. For session consistency, clients must echo this request via the x-ms-session-token request header for subsequent operations made to the corresponding collection.
     */
    MSSessionToken("x-ms-session-token", "x-ms-session-token: The session token of the request. For session consistency, clients must echo this request via the x-ms-session-token request header for subsequent operations made to the corresponding collection."),
    /**
     * The version of the Cosmos DB REST service.
     */
    MSVersion("x-ms-version")
}