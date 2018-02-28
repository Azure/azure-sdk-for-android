package com.azure.data.model.indexing

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

@DslMarker
annotation class IndexDsl

@IndexDsl
class IndexingPolicyBuilder {

    var automatic = false
    var mode: IndexingMode? = null

    private val includedPaths = mutableListOf<IndexingPolicy.IncludedPath>()
    private val excludedPaths = mutableListOf<IndexingPolicy.ExcludedPath>()

    fun includedPaths(block: IncludedPaths.() -> Unit) {
        includedPaths.addAll(IncludedPaths().apply(block))
    }

    fun excludedPaths(block: ExcludedPaths.() -> Unit) {
        excludedPaths.addAll(ExcludedPaths().apply(block))
    }

    fun build(): IndexingPolicy = IndexingPolicy(automatic, mode, includedPaths, excludedPaths)
}

@IndexDsl
class IncludedPaths : ArrayList<IndexingPolicy.IncludedPath>() {

    fun includedPath(block: IncludedPathBuilder.() -> Unit) {
        add(IncludedPathBuilder().apply(block).build())
    }
}

@IndexDsl
class IncludedPathBuilder {

    var path: String? = null

    private val indexes = mutableListOf<Index>()

    fun indexes(block: Indexes.() -> Unit) {
        indexes.addAll(Indexes().apply(block))
    }

    fun build() : IndexingPolicy.IncludedPath = IndexingPolicy.IncludedPath(path, indexes)
}

@IndexDsl
class Indexes : ArrayList<Index>() {

    fun index(block: IndexBuilder.() -> Unit) {
        add(IndexBuilder().apply(block).build())
    }

    fun index(index: Index) {
        add(index)
    }
}

@IndexDsl
class IndexBuilder {

    var kind: IndexKind? = null
    var dataType: DataType? = null
    var precision: Short? = null

    fun build() : Index = Index(kind, dataType, precision)
}

@IndexDsl
class ExcludedPaths : ArrayList<IndexingPolicy.ExcludedPath>() {

    fun excludedPath(block: ExcludedPathBuilder.() -> Unit) {
        add(ExcludedPathBuilder().apply(block).build())
    }
}

@IndexDsl
class ExcludedPathBuilder {

    var path: String? = null

    fun build() : IndexingPolicy.ExcludedPath = IndexingPolicy.ExcludedPath(path)
}