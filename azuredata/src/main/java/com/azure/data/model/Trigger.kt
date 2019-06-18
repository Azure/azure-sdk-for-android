package com.azure.data.model

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * Represents a trigger in the Azure Cosmos DB service.
 *
 * - Remark:
 *   Azure Cosmos DB supports pre and post triggers written in JavaScript to be executed on creates, updates and deletes.
 *   For additional details, refer to the server-side JavaScript API documentation.
 */
class Trigger(id: String? = null) : Resource(id) {

    /** Gets or sets the body of the trigger for the Azure Cosmos DB service.*/
    var body: String? = null

    /** Gets or sets the operation the trigger is associated with for the Azure Cosmos DB service.*/
    var triggerOperation: TriggerOperation? = null

    /** Get or set the type of the trigger for the Azure Cosmos DB service.*/
    var triggerType: TriggerType? = null

    constructor(id: String, body: String, operation: TriggerOperation, type: TriggerType) : this(id) {

        this.body = body
        this.triggerOperation = operation
        this.triggerType = type
    }

    /**
     * Specifies the operations on which a trigger should be executed in the Azure Cosmos DB service.
     * - all:      Specifies all operations.
     * - insert:   Specifies insert operations only.
     * - replace:  Specifies replace operations only.
     * - delete:   Specifies delete operations only.
     */
    enum class TriggerOperation {

        All,
        Create,
        Replace,
        Delete
    }

    /**
     * Specifies the type of the trigger in the Azure Cosmos DB service.
     * - pre:  Trigger should be executed after the associated operation(s).
     * - post: Trigger should be executed before the associated operation(s).
     */
    enum class TriggerType {

        Pre,
        Post
    }

    companion object {

        const val resourceName = "Trigger"
        const val listName = "Triggers"
    }
}