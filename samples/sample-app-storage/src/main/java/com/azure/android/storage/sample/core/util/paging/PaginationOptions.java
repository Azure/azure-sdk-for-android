package com.azure.android.storage.sample.core.util.paging;

import androidx.annotation.NonNull;
import androidx.paging.PagedList;

import java.util.concurrent.Executor;

public class PaginationOptions {
    private final Executor pageLoadExecutor;
    private PagedList.Config pagedListConfig;
    private boolean interactiveLoginEnabled;

    public PaginationOptions(Executor pageLoadExecutor) {
        this.pageLoadExecutor = pageLoadExecutor;
    }

    public Executor getPageLoadExecutor() {
        return this.pageLoadExecutor;
    }

    public PagedList.Config getPagedListConfig() {
        return this.pagedListConfig;
    }

    @NonNull
    public PaginationOptions setPagedListConfig(PagedList.Config config) {
        this.pagedListConfig = config;
        return this;
    }

    public boolean isInteractiveLoginEnabled() {
        return this.interactiveLoginEnabled;
    }

    public PaginationOptions enableInteractiveLogin(boolean enabled) {
        this.interactiveLoginEnabled = enabled;
        return this;
    }
}
