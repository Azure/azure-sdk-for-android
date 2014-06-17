/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.azure.core;

import com.microsoft.azure.Configuration;
import com.microsoft.azure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.azure.core.pipeline.filter.ServiceResponseFilter;

import java.util.concurrent.ExecutorService;

public abstract class ServiceClient<TClient> implements
        FilterableService<TClient> {
    private final ExecutorService executorService;
    private final Configuration configuration;

    public ExecutorService getExecutorService() {
        return this.executorService;
    }
    
    public Configuration getConfiguration() {
        return this.configuration;
    }

    protected ServiceClient(Configuration configuration, ExecutorService executorService) {
        this.configuration = configuration;
        this.executorService = executorService;
    }

    protected abstract TClient newInstance(Configuration configuration, ExecutorService executorService);

    @Override
    public TClient withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TClient withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TClient withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TClient withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter) {
        throw new UnsupportedOperationException();
    }
}
