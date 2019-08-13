package com.azure.core.util.polling;

import android.os.SystemClock;
import com.azure.core.util.function.Function;
import com.azure.core.util.polling.PollResponse.OperationStatus;

public class Poller<T> {
    /*
     * poll operation is a function that takes the previous PollResponse, and
     * returns a new PollResponse to represent the current state
     */
    private final Function<PollResponse<T>, PollResponse<T>> pollOperation;

    /*
     * poll interval before next auto poll. This value will be used if the PollResponse does not include retryAfter from the service.
     */
    private Integer pollInterval;

    /*
     * This will save last poll response.
     */
    private PollResponse<T> pollResponse;

    /**
     * Create a {@link Poller} instance with poll interval and poll operation. The polling is initiated by calling one of the following method.
     * <ul>
     *     <li>poll()</li>
     *     <li>pollUntilDone()</li>
     *     <li>pollUntil(PollResponse.OperationStatus terminalStatus)</li>
     *     <li>pollUntil(PollResponse.OperationStatus terminalStatus, Integer timeoutInMs)</li>
     * </ul>
     * The poll cycle will be defined by {@code retryAfter} value in {@link PollResponse}.
     * In absence of {@code retryAfter}, the {@link Poller} will use {@code pollInterval}.
     *
     * @param pollInterval Not-null and greater than zero poll interval in milliseconds.
     * @param pollOperation The polling operation to be called by the {@link Poller} instance. This is a callback into the client library,
     * which must never return {@code null}, and which must always have a non-null {@link PollResponse.OperationStatus}.
     * poll operation should never throw exception.If any unexpected scenario happens in poll operation,
     * it should be handled by client library and return a valid {@link PollResponse}. However if poll operation throws,
     * the {@link Poller} will disregard that and continue to poll.
     * @throws IllegalArgumentException if {@code pollInterval} is less than or equal to zero and if {@code pollInterval} or {@code pollOperation} are {@code null}
     */
    public Poller(Integer pollInterval, Function<PollResponse<T>, PollResponse<T>> pollOperation) {
        if (pollInterval == null || pollInterval <= 0) {
            new IllegalArgumentException("Null, negative or zero value for poll interval is not allowed.");
        }
        if (pollOperation == null) {
            new IllegalArgumentException("Null value for poll operation is not allowed.");
        }

        this.pollInterval = pollInterval;
        this.pollOperation = pollOperation;
        this.pollResponse = new PollResponse<>(OperationStatus.NOT_STARTED, null);
    }

    /**
     * Enable user to take control of polling and trigger manual poll operation. It will call poll operation once.
     * This will not turn off auto polling.
     *
     * @return {@link PollResponse} from the poll operation.
     */
    public PollResponse<T> poll() {
        this.pollResponse = this.pollOperation.apply(this.pollResponse);
        return this.pollResponse;
    }

    /**
     * poll until it completes. The polling is considered complete based on status defined in {@link PollResponse.OperationStatus}.
     *
     * @return returns final {@link PollResponse} when polling is complete as defined in {@link PollResponse.OperationStatus}.
     */
    public PollResponse<T> pollUntilDone() {
        do {
            try {
                this.poll();
            } catch (Exception ex) {
                // ignore exception as per contract
            }
            SystemClock.sleep(getCurrentDelay());
        } while (!hasCompleted());
        return this.pollResponse;
    }

    /**
     * Poll indefinitely until given {@link PollResponse.OperationStatus} is received.
     * @param terminalStatus The desired {@link PollResponse.OperationStatus} to match for and it can be any valid {@link PollResponse.OperationStatus} value.
     * @return {@link PollResponse} for matching desired status.
     * @throws IllegalArgumentException If {@code statusToBlockFor} is {@code null}.
     */
    public PollResponse<T> pollUntil(OperationStatus terminalStatus) {
        if (terminalStatus == null) {
            throw new IllegalArgumentException("Null value for status is not allowed.");
        }
        do {
            try {
                this.poll();
            } catch (Exception ex) {
                // ignore exception as per contract
            }
            SystemClock.sleep(getCurrentDelay());
        } while (getStatus() != terminalStatus);
        return this.pollResponse;
    }

    /**
     * Poll with a timeout until given {@link PollResponse.OperationStatus} is received.
     * @param terminalStatus The desired {@link PollResponse.OperationStatus} to match for and it can be any valid {@link PollResponse.OperationStatus} value.
     * @param timeoutInMs The time after which it will stop polling. A {@code null} value will cause to block indefinitely. Zero or negative are not valid values.
     * @return {@link PollResponse} for matching desired status.
     * @throws IllegalArgumentException If {@code statusToBlockFor} is {@code null}.
     */
    public PollResponse<T> pollUntil(OperationStatus terminalStatus, Integer timeoutInMs) {
        if (terminalStatus == null) {
            throw new IllegalArgumentException("Null value for status is not allowed.");
        }
        if (timeoutInMs == null || timeoutInMs <= 0) {
            new IllegalArgumentException("Null, negative or zero value for timeout is not allowed.");
        }
        Integer remainingTimeInMs = timeoutInMs;
        do {
            if (remainingTimeInMs <= 0) {
                return this.pollResponse;
            }
            try {
                this.poll();
            } catch (Exception ex) {
                // ignore exception as per contract
            }
            Integer sleepFor = Math.min(remainingTimeInMs, getCurrentDelay());
            SystemClock.sleep(sleepFor);
            remainingTimeInMs = remainingTimeInMs - sleepFor;
        } while (getStatus() != terminalStatus);
        return this.pollResponse;
    }

    /**
     * Current known status as a result of last poll event or last response from a manual polling.
     *
     * @return current status or {@code null} if no status is available.
     */
    public OperationStatus getStatus() {
        return this.pollResponse != null ? this.pollResponse.getStatus() : null;
    }

    /*
     * We will use  {@link PollResponse#getRetryAfter} if it is greater than zero otherwise use poll interval.
     */
    private Integer getCurrentDelay() {
        return (this.pollResponse != null
                && this.pollResponse.getRetryAfter() != null
                && this.pollResponse.getRetryAfter() > 0) ? this.pollResponse.getRetryAfter() : this.pollInterval;
    }

    /*
     * An operation will be considered complete if it is in one of the following state:
     * <ul>
     *     <li>SUCCESSFULLY_COMPLETED</li>
     *     <li>USER_CANCELLED</li>
     *     <li>FAILED</li>
     * </ul>
     * Also see {@link OperationStatus}
     * @return true if operation is done/complete.
     */
    private boolean hasCompleted() {
        return pollResponse != null && (pollResponse.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED
                || pollResponse.getStatus() == OperationStatus.FAILED
                || pollResponse.getStatus() == OperationStatus.USER_CANCELLED);
    }
}
