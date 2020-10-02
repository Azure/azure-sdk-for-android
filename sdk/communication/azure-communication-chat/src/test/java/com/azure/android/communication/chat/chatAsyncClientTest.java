package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatThreadInfo;
import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.responsepaging.AsyncPagedDataCollection;
import com.azure.android.core.util.paging.Page;

import org.junit.After;
import org.junit.Test;
import org.threeten.bp.OffsetDateTime;

import java.util.concurrent.TimeUnit;

import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class chatAsyncClientTest {

    private static final MockWebServer mockWebServer = new MockWebServer();
    private static final String BASE_URL = mockWebServer.url("/").toString();
    private static AzureCommunicationChatServiceAsyncClient chatServiceAsyncClient =
        new AzureCommunicationChatServiceAsyncClient.Builder()
            .serviceClientBuilder(new ServiceClient.Builder().setBaseUrl(BASE_URL))
            .build();

    @After
    public void tearDown() throws InterruptedException {
        // For ensuring the responses enqueued are consumed before making the next call.
        mockWebServer.takeRequest(20, TimeUnit.MILLISECONDS);
    }

    @Test
    public void listThreadPages() {
        mockThreadsResponse(5);

        chatServiceAsyncClient.listChatThreadsPages(new Callback<AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>>>() {
            @Override
            public void onSuccess(AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>> result, Response response) {
                AsyncPagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>> asyncPagedDataCollection = result;
                asyncPagedDataCollection.getFirstPage(new Callback<Page<ChatThreadInfo>>() {
                    @Override
                    public void onSuccess(Page<ChatThreadInfo> result, Response response) {
                        assertEquals(5, result.getItems().size());
                        mockThreadsResponse(3);
                        asyncPagedDataCollection.getPage(result.getNextPageId(), new Callback<Page<ChatThreadInfo>>() {
                            @Override
                            public void onSuccess(Page<ChatThreadInfo> result, Response response) {
                                assertEquals(3, result.getItems().size());

                                 asyncPagedDataCollection.getPage(result.getPreviousPageId(), new Callback<Page<ChatThreadInfo>>() {
                                    @Override
                                    public void onSuccess(Page<ChatThreadInfo> result, Response response) {
                                        assertEquals(5, result.getItems().size());
                                    }

                                    @Override
                                    public void onFailure(Throwable throwable, Response response) {
                                        assertTrue(false);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Throwable throwable, Response response) {
                                assertTrue(false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable, Response response) {
                        assertTrue(false);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                assertTrue(false);
            }
        }, 5, OffsetDateTime.now());

    }

    @Test
    public void listThreadMessagePages() {
        mockMessagesResponse(5);

        chatServiceAsyncClient.listChatMessagesPages("threadId", new Callback<AsyncPagedDataCollection<ChatMessage, Page<ChatMessage>>>() {
            @Override
            public void onSuccess(AsyncPagedDataCollection<ChatMessage, Page<ChatMessage>> result, Response response) {
                AsyncPagedDataCollection<ChatMessage, Page<ChatMessage>> asyncPagedDataCollection = result;
                asyncPagedDataCollection.getFirstPage(new Callback<Page<ChatMessage>>() {
                    @Override
                    public void onSuccess(Page<ChatMessage> result, Response response) {
                        assertEquals(5, result.getItems().size());
                        mockMessagesResponse(3);
                        asyncPagedDataCollection.getPage(result.getNextPageId(), new Callback<Page<ChatMessage>>() {
                            @Override
                            public void onSuccess(Page<ChatMessage> result, Response response) {
                                assertEquals(3, result.getItems().size());

                                asyncPagedDataCollection.getPage(result.getPreviousPageId(), new Callback<Page<ChatMessage>>() {
                                    @Override
                                    public void onSuccess(Page<ChatMessage> result, Response response) {
                                        assertEquals(5, result.getItems().size());
                                    }

                                    @Override
                                    public void onFailure(Throwable throwable, Response response) {
                                        assertTrue(false);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Throwable throwable, Response response) {
                                assertTrue(false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable, Response response) {
                        assertTrue(false);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable, Response response) {
                assertTrue(false);
            }
        }, 5, OffsetDateTime.now());

    }

    private void mockThreadsResponse(int n) {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(ThreadsMocker.mockThreads(n));
        mockWebServer.enqueue(mockResponse);
    }

    private void mockMessagesResponse(int n) {
        String body = MessagesMocker.mockThreadMessages(n);
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(body);
        mockWebServer.enqueue(mockResponse);
    }
}
