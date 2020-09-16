package com.azure.android.core.http.responsepaging;

import com.azure.android.core.http.Callback;
import com.azure.android.core.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AsyncPagedDataCollectionTest {
    private static class Item {
        public final int value;

        public Item(int value) {
            this.value = value;
        }
    }
    private static class ThreePageProvider extends AsyncPagedDataRetriever<Item, Page<Item>> {
        private LinkedHashMap<String, Page<Item>> pages = new LinkedHashMap<String, Page<Item>>();
        public int getFirstPageCallCount = 0;
        public int getPageCallCount = 0;

        public ThreePageProvider() {
            super(3);
            pages.put("1", new Page<Item>("1", Arrays.asList(new Item(1), new Item(2), new Item(3))).setNextPageId("2"));
            pages.put("2", new Page<Item>("2", Arrays.asList(new Item(4), new Item(5), new Item(6))).setNextPageId("3"));
            pages.put("3", new Page<Item>("3", Arrays.asList(new Item(7))));
        }

        public void resetCounters() {
            getFirstPageCallCount = 0;
            getPageCallCount = 0;
        }

        @Override
        public void getFirstPage(Callback<Page<Item>> callback) {
            getFirstPageCallCount++;
            callback.onSuccess(pages.get("1"), null);
        }

        @Override
        public void getPage(String pageId, Callback<Page<Item>> callback) {
            getPageCallCount++;
            if (pages.containsKey(pageId)) {
                callback.onSuccess(pages.get(pageId), null);
            }
            else {
                callback.onFailure(new IllegalArgumentException(), null);
            }
        }
    }

    private ThreePageProvider provider;
    private AsyncPagedDataCollection<Item, Page<Item>> collection;

    private static class PageCallback implements Callback<Page<Item>> {
        public Page<Item> lastPage;
        public Throwable lastError;
        @Override
        public void onSuccess(Page<Item> value, okhttp3.Response response) {
            lastPage = value;
            lastError = null;
        }

        @Override
        public void onFailure(Throwable t, okhttp3.Response response) {
            lastPage = null;
            lastError = t;
        }
    }

    @Before
    public void setup() {
        provider = new ThreePageProvider();
        collection = new AsyncPagedDataCollection(provider);
    }

    @Test
    public void iterationTest() {
        PageCallback pageCallback = new PageCallback();
        collection.getFirstPage(pageCallback);
        assertNotNull(pageCallback.lastPage);
        int pageCount = 1;
        while(pageCallback.lastPage.getNextPageId() != null) {
            String currentPageId = pageCallback.lastPage.getPageId();
            collection.getPage(pageCallback.lastPage.getNextPageId(), pageCallback);
            assertNotNull(pageCallback.lastPage);
            assertEquals(currentPageId, pageCallback.lastPage.getPreviousPageId());
            pageCount++;
        }
        assertEquals(3, pageCount);
    }

    @Test
    public void invalidKeyTest() {
        PageCallback pageCallback = new PageCallback();
        collection.getPage("5", pageCallback);
        assertNull(pageCallback.lastPage);
        assertNotNull(pageCallback.lastError);
    }

    @Test
    public void cachingFirstPageTest() {
        provider.resetCounters();
        PageCallback pageCallback = new PageCallback();
        collection.getFirstPage(pageCallback);
        assertEquals(1, provider.getFirstPageCallCount);

        collection.getFirstPage(pageCallback);
        assertEquals(1, provider.getFirstPageCallCount);
    }

    @Test
    public void cachingPageTest() {
        provider.resetCounters();
        PageCallback pageCallback = new PageCallback();
        collection.getFirstPage(pageCallback);
        String secondPageId = pageCallback.lastPage.getNextPageId();
        collection.getPage(secondPageId, pageCallback);
        assertEquals(1, provider.getPageCallCount);

        collection.getPage(secondPageId, pageCallback);
        assertEquals(1, provider.getPageCallCount);
    }
}
