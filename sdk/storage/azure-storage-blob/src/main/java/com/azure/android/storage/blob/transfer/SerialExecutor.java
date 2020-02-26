// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

/**
 * Package private.
 *
 * An {@link Executor} that wrap and delegate work execution to another {@link Executor}.
 * SerialExecutor ensure that the submitted works are executed serially using
 * the wrapped executor.
 *
 * SerialExecutor is used by {@link TransferClient} for all its internal book keeping,
 * which includes creating DB entries for transfer workers, querying DB for status,
 * submitting transfer request to {@link WorkManager}.
 *
 * This is similar to a {@link android.os.Looper} that serially process messages from
 * it's message queue. The difference is while Looper hold a dedicated thread throughout
 * it lifetime, SerialExecutor delegate the work execution to another executor.
 *
 * @see <a href="https://developer.android.com/reference/java/util/concurrent/Executor">Executor</a>
 */
final class SerialExecutor implements Executor {
    // the queue holding the tasks to be executed serially.
    private final ArrayDeque<Task> tasksQueue = new ArrayDeque<>();
    // the executor to delegate the task execution to.
    private final Executor executor;
    // the actively executing task.
    private volatile Task activeTask;

    /**
     * Create SerialExecutor.
     *
     * @param executor the executor to delegate the serial execution of tasks
     */
    SerialExecutor(@NonNull Executor executor) {
        this.executor = executor;
    }

    /**
     * Submit a work to execute.
     *
     * @param runnable the unit of work
     */
    @Override
    public synchronized void execute(@NonNull Runnable runnable) {
        this.tasksQueue.add(new Task(runnable, this));
        if (this.activeTask == null) {
            this.enqueueNextTask();
        }
    }

    /**
     * Enqueue the next task for execution.
     */
    private synchronized void enqueueNextTask() {
        this.activeTask = this.tasksQueue.poll();
        if (this.activeTask != null) {
            this.executor.execute(this.activeTask);
        }
    }

    /**
     * A Task representing a unit of work. Once a Task is executed, it request the
     * executor to enqueue the next Task for execution.
     */
    private static class Task implements Runnable {
        // the unit of work.
        final Runnable runnable;
        // the executor to request execution of next task.
        final SerialExecutor serialExecutor;

        /**
         * Create a Task.
         *
         *  @param runnable the unit of work this task represents
         * @param serialExecutor the executor to enqueue the execution of next task
         */
        Task(Runnable runnable, SerialExecutor serialExecutor) {
            this.runnable = runnable;
            this.serialExecutor = serialExecutor;
        }

        /**
         * Execute this task, once completed enqueue the next task.
         */
        @Override
        public void run() {
            try {
                this.runnable.run();
            } finally {
                this.serialExecutor.enqueueNextTask();
            }
        }
    }
}

