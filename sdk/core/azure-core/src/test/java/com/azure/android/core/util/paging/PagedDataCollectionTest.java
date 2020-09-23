/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.util.paging;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PagedDataCollectionTest {
    private static class Item {
        public final int value;

        public Item(int value) {
            this.value = value;
        }
    }
    private static class ThreePageProvider extends PagedDataRetriever<Item, Page<Item>> {
        private LinkedHashMap<String, Page<Item>> pages = new LinkedHashMap<String, Page<Item>>();
        public int getFirstPageCallCount = 0;
        public int getPageCallCount = 0;

        public ThreePageProvider() {
            super(3);
            pages.put("1", new Page<Item>("1", Arrays.asList(new Item(1), new Item(2), new Item(3))).setNextPageId("2"));
            pages.put("2", new Page<Item>("2", Arrays.asList(new Item(4), new Item(5), new Item(6))).setNextPageId("3"));
            pages.put("3", new Page<Item>("3", Arrays.asList(new Item(7))));
        }

        @Override
        public Page<Item> getFirstPage() {
            getFirstPageCallCount++;
            return pages.get("1");
        }

        @Override
        public Page<Item> getPage(String pageId) {
            getPageCallCount++;
            if (!pages.containsKey(pageId))
                throw new IllegalArgumentException();

            return pages.get(pageId);
        }

        public void resetCounters() {
            getFirstPageCallCount = 0;
            getPageCallCount = 0;
        }
    }

    private ThreePageProvider provider;
    private PagedDataCollection<Item, Page<Item>> collection;

    @Before
    public void setup() {
        provider = new ThreePageProvider();
        collection = new PagedDataCollection(provider);
    }

    @Test
    public void iterationTest() {
        Page<Item> page = collection.getFirstPage();
        assertNotNull(page);
        while(page.getNextPageId() != null) {
            String currentPageId = page.getPageId();
            page = collection.getPage(page.getNextPageId());
            assertNotNull(page);
            assertEquals(currentPageId, page.getPreviousPageId());
        }
    }

    @Test
    public void invalidKeyTest() {
        try {
            collection.getPage("5");
            assertTrue(false);
        }
        catch (RuntimeException r){
            assertTrue(true);
        }
    }

    @Test
    public void cachingFirstPageTest() {
        provider.resetCounters();
        Page<Item> p1 = collection.getFirstPage();
        assertEquals(1, provider.getFirstPageCallCount);

        p1 = collection.getFirstPage();
        assertEquals(1, provider.getFirstPageCallCount);
    }

    @Test
    public void cachingPageTest() {
        provider.resetCounters();
        Page<Item> p1 = collection.getFirstPage();
        Page<Item> p2 = collection.getPage(p1.getNextPageId());
        assertEquals(1, provider.getPageCallCount);

        p2 = collection.getPage(p1.getNextPageId());
        assertEquals(1, provider.getPageCallCount);
    }
}
