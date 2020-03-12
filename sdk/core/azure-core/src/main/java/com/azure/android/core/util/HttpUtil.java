package com.azure.android.core.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

import static com.azure.android.core.util.CoreUtils.isNullOrEmpty;

public final class HttpUtil {
    @Nullable
    public static Long getContentLength(@NonNull Request request) {
        return getContentLength(request.headers(), () -> {
            if (request.body() != null) {
                try {
                    return request.body().contentLength();
                } catch (IOException ignore) { }
            }
            return null;
        });
    }

    @Nullable
    public static Long getContentLength(@NonNull Response response) {
        return getContentLength(response.headers(), () -> {
            if (response.body() != null) {
                return response.body().contentLength();
            }
            return null;
        });
    }

    private static Long getContentLength(Headers headers, ContentLengthProducer lengthProducer) {
        Long contentLength = null;
        String headerValue = headers.get("Content-Length");
        if (!isNullOrEmpty(headerValue)) {
            try {
                contentLength = Long.parseLong(headerValue);
            } catch (NumberFormatException ignore) { }
        }

        if (contentLength == null) {
            contentLength = lengthProducer.contentLength();
        }

        if (contentLength != null && contentLength < 0) { // A body's default Content-Length is -1
            return null;
        }

        return contentLength;
    }

    private interface ContentLengthProducer {
        Long contentLength();
    }

    @Nullable
    public static String getContentType(@NonNull Request request) {
        return getContentType(request.headers(), () -> {
            if (request.body() != null) {
                return request.body().contentType();
            }
            return null;
        });
    }

    @Nullable
    public static String getContentType(@NonNull Response response) {
        return getContentType(response.headers(), () -> {
            if (response.body() != null) {
                return response.body().contentType();
            }
            return null;
        });
    }

    private static String getContentType(Headers headers, ContentTypeProducer typeProducer) {
        String contentType = headers.get("Content-Type");
        if (isNullOrEmpty(contentType)) {
            MediaType bodyType = typeProducer.contentType();
            if (bodyType != null) {
                contentType = bodyType.toString();
            }
        }

        if (isNullOrEmpty(contentType)) {
            return null;
        }

        return contentType;
    }

    private interface ContentTypeProducer {
        MediaType contentType();
    }
}
