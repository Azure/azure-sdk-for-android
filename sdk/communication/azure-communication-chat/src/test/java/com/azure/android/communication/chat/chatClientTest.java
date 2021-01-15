package com.azure.android.communication.chat;

import com.azure.android.communication.chat.models.ChatMessage;
import com.azure.android.communication.chat.models.ChatThreadInfo;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.core.http.Response;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.UserAgentInterceptor;
import com.azure.android.core.http.options.TelemetryOptions;
import com.azure.android.core.http.responsepaging.PagedDataResponseCollection;
import com.azure.android.core.util.paging.Page;
import com.azure.android.core.util.paging.PagedDataCollection;

import org.junit.After;
import org.junit.Test;
import org.threeten.bp.OffsetDateTime;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.communication.chat.ChatConfig.chatSDKName;
import static com.azure.android.communication.chat.ChatConfig.chatSDKVersion;
import static junit.framework.TestCase.assertEquals;

public class chatClientTest {

    private static final MockWebServer mockWebServer = new MockWebServer();
    private static final String BASE_URL = mockWebServer.url("/").toString();
    private static final String userApplicationId = "AcsAndroid";
    private static UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(
        new TelemetryOptions(false, userApplicationId),
        chatSDKName,
        chatSDKVersion,
        null,
        null,
        null);
    private static ChatClient chatServiceClient =
        new ChatClient.Builder()
            .serviceClientBuilder(new ServiceClient.Builder().setBaseUrl(BASE_URL))
            .userAgentInterceptor(userAgentInterceptor)
            .build();

    @After
    public void tearDown() throws InterruptedException {
        // For ensuring the responses enqueued are consumed before making the next call.
        mockWebServer.takeRequest(20, TimeUnit.MILLISECONDS);
    }

    @Test
    public void listThreadPages() {
        mockThreadsResponse(5);

        final PagedDataCollection<ChatThreadInfo, Page<ChatThreadInfo>> pages = chatServiceClient.listChatThreadsWithPage(5, OffsetDateTime.now());
        final Page<ChatThreadInfo> firstPage = pages.getFirstPage();
        assertEquals(5, firstPage.getItems().size());

        mockThreadsResponse(3);

        final Page<ChatThreadInfo> nextPage = pages.getPage(firstPage.getNextPageId());
        assertEquals(3, nextPage.getItems().size());

        assertEquals(5, pages.getPage(nextPage.getPreviousPageId()).getItems().size());
    }

    @Test
    public void listThreadPagesWithResponse() {
        mockThreadsResponse(5);

        final PagedDataResponseCollection<ChatThreadInfo, Page<ChatThreadInfo>> pagesWithResponse = chatServiceClient.listChatThreadsWithPageResponse(5, OffsetDateTime.now());
        final Response<Page<ChatThreadInfo>> firstPage = pagesWithResponse.getFirstPage();
        assertEquals(5, firstPage.getValue().getItems().size());

        mockThreadsResponse(3);

        final Response<Page<ChatThreadInfo>> nextPage = pagesWithResponse.getPage(firstPage.getValue().getNextPageId());
        assertEquals(3, nextPage.getValue().getItems().size());

        assertEquals(5, pagesWithResponse.getPage(nextPage.getValue().getPreviousPageId()).getValue().getItems().size());
    }



    private void mockThreadsResponse(int n) {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(ThreadsMocker.mockThreads(n));
        mockWebServer.enqueue(mockResponse);
    }


}
