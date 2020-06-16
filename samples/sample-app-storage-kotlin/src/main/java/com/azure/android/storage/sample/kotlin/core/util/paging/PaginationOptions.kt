package com.azure.android.storage.sample.kotlin.core.util.paging

import androidx.paging.PagedList
import java.util.concurrent.Executor

class PaginationOptions(val pageLoadExecutor: Executor) {
    var pagedListConfig: PagedList.Config? = null
        private set
    var isInteractiveLoginEnabled = false
        private set

    fun setPagedListConfig(config: PagedList.Config): PaginationOptions {
        pagedListConfig = config
        return this
    }

    fun enableInteractiveLogin(enabled: Boolean): PaginationOptions {
        isInteractiveLoginEnabled = enabled
        return this
    }
}
