// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest.util.paging;

import com.azure.android.core.logging.ClientLogger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

public class PagedIterableTests {
    private final ClientLogger logger = new ClientLogger(PagedIterableTests.class);

    @Test
    public void canEnumerateAllPages() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        //
        final PagedIterable<String> iterable = new PagedIterable<String>(pageRetriever,
                continuationToken -> continuationToken != null, logger);

        iterable.byPage().forEach(response -> {
            Assertions.assertNotNull(response);
        });

        Assertions.assertEquals(5, pageRetriever.getCallCount());
    }

    @Test
    public void canEnumerateAllElements() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        //
        final PagedIterable<String> iterable = new PagedIterable<String>(pageRetriever,
            continuationToken -> continuationToken != null, logger);

        iterable.forEach(element -> {
            Assertions.assertNotNull(element);
        });

        Assertions.assertEquals(5, pageRetriever.getCallCount());
    }

    @Test
    public void canStopPageEnumeration() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        //
        final PagedIterable<String> iterable = new PagedIterable<String>(pageRetriever,
            continuationToken -> continuationToken != null, logger);

        for (PagedResponse<String> response : iterable.byPage()) {
            if (response.getContinuationToken().equalsIgnoreCase("3")) {
                break;
            }
        }

        Assertions.assertEquals(3, pageRetriever.getCallCount());
    }

    @Test
    public void canStopElementEnumeration() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5);
        //
        final PagedIterable<String> iterable = new PagedIterable<String>(pageRetriever,
            continuationToken -> continuationToken != null, logger);

        for (String element : iterable) {
            if (element.equalsIgnoreCase("8")) { // "8" is an item in page
                break;
            }
        }

        Assertions.assertEquals(3, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateSdkExceptionWhenIteratePages() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5, 3);
        //
        final PagedIterable<String> iterable = new PagedIterable<String>(pageRetriever,
            continuationToken -> continuationToken != null, logger);

        Throwable error = null;
        try {
            for (PagedResponse<String> response : iterable.byPage()) {

            }
        } catch (Throwable throwable) {
            error = throwable;
        }

        Assertions.assertNotNull(error);
        Assertions.assertTrue(error instanceof UncheckedIOException);
        Assertions.assertNotNull(error.getCause());
        Assertions.assertTrue(error.getCause() instanceof IOException);
        Assertions.assertEquals(4, pageRetriever.getCallCount());
    }

    @Test
    public void shouldPropagateSdkExceptionWhenIterateElements() {
        final StringPageRetriever pageRetriever = new StringPageRetriever(3, 5, 3);
        //
        final PagedIterable<String> iterable = new PagedIterable<String>(pageRetriever,
            continuationToken -> continuationToken != null, logger);

        Throwable error = null;
        try {
            for (String element : iterable) {

            }
        } catch (Throwable throwable) {
            error = throwable;
        }

        Assertions.assertNotNull(error);
        Assertions.assertNotNull(error.getCause());
        Assertions.assertTrue(error instanceof UncheckedIOException);
        Assertions.assertNotNull(error.getCause());
        Assertions.assertTrue(error.getCause() instanceof IOException);
        Assertions.assertEquals(4, pageRetriever.getCallCount());
    }
}