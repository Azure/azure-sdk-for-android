package com.azure.android.core.http;

//import com.azure.android.core.util.Context;
//
//import org.junit.Test;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;

public class ServiceCallTest {
//    private final ServiceCallTask call = new ServiceCallTask(new SimpleCall(), Context.NONE);
//
//    @Test
//    public void cancelCall() {
//        assertFalse(call.isCanceled());
//
//        call.cancel();
//
//        assertTrue(call.isCanceled());
//    }

    private class SimpleCall implements Call<Void> {
        boolean isCanceled = false;

        @SuppressWarnings("NullableProblems")
        @Override
        public Response<Void> execute() throws IOException {
            return null;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void enqueue(Callback<Void> callback) {
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public void cancel() {
            isCanceled = true;
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @SuppressWarnings({"NullableProblems", "ConstantConditions", "MethodDoesntCallSuperMethod"})
        @Override
        public Call<Void> clone() {
            return null;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Request request() {
            return null;
        }
    }
}
