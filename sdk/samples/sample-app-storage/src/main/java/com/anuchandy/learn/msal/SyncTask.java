package com.anuchandy.learn.msal;

import java.util.concurrent.CountDownLatch;

class SyncTask<T> {
    private final Work<T> work;

    SyncTask(Work<T> work) {
        this.work = work;
    }

    T getResult() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Work.Output<T> output = new Work.Output<>(latch);
        this.work.execute(output);
        latch.await();
        if (output.getError() != null) {
            throw output.getError();
        } else {
            return output.getValue();
        }
    }

    @FunctionalInterface
    interface Work<T> {
        void execute(Output<T> output);

        class Output<T> {
            private final CountDownLatch latch;
            private volatile T value;
            private volatile Exception error;

            Output(CountDownLatch latch) {
                this.latch = latch;
            }

            void setValue(T value) {
                if (this.value != null) {
                    throw new IllegalStateException("value is already set.");
                }
                this.value = value;
                this.latch.countDown();
            }

            void setError(Exception error) {
                if (this.error != null) {
                    throw new IllegalStateException("error is already set.");
                }
                this.error = error;
                this.latch.countDown();
            }

            T getValue() {
                return this.value;
            }

            Exception getError() {
                return this.error;
            }
        }
    }
}
