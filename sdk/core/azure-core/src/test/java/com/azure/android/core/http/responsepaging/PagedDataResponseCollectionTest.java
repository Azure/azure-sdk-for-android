package com.azure.android.core.http.responsepaging;

import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PagedDataResponseCollectionTest {
    private static class Item {
        public final int value;

        public Item(int value) {
            this.value = value;
        }
    }
    private static class ThreePageProvider extends PagedDataResponseRetriever<Item, Page<Item>> {
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
        public Response<Page<Item>> getFirstPage() {
            getFirstPageCallCount++;
            return new Response(null, 200, null, pages.get("1"));
        }

        @Override
        public Response<Page<Item>> getPage(String pageId) {
            getPageCallCount++;
            if (!pages.containsKey(pageId))
                throw new IllegalArgumentException();

            return new Response(null, 200, null, pages.get(pageId));
        }

        public void resetCounters() {
            getFirstPageCallCount = 0;
            getPageCallCount = 0;
        }
    }

    private ThreePageProvider provider;
    private PagedDataResponseCollection<Item, Page<Item>> collection;

    @Before
    public void setup() {
        provider = new ThreePageProvider();
        collection = new PagedDataResponseCollection(provider);
    }

    @Test
    public void iterationTest() {
        Response<Page<Item>> pageResponse = collection.getFirstPage();
        assertNotNull(pageResponse);
        assertNotNull(pageResponse.getValue());
        int pageCount = 1;
        while(pageResponse.getValue().getNextPageId() != null) {
            String currentPageId = pageResponse.getValue().getPageId();
            pageResponse = collection.getPage(pageResponse.getValue().getNextPageId());
            assertNotNull(pageResponse);
            assertNotNull(pageResponse.getValue());
            assertEquals(currentPageId, pageResponse.getValue().getPreviousPageId());
            pageCount++;
        }
        assertEquals(3, pageCount);
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
        Response<Page<Item>> p1Response = collection.getFirstPage();
        assertEquals(1, provider.getFirstPageCallCount);

        p1Response = collection.getFirstPage();
        assertEquals(1, provider.getFirstPageCallCount);
    }

    @Test
    public void cachingPageTest() {
        provider.resetCounters();
        Response<Page<Item>> p1 = collection.getFirstPage();
        Response<Page<Item>> p2 = collection.getPage(p1.getValue().getNextPageId());
        assertEquals(1, provider.getPageCallCount);

        p2 = collection.getPage(p1.getValue().getNextPageId());
        assertEquals(1, provider.getPageCallCount);
    }
}
