// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.micro.util;

import com.azure.core.logging.ClientLogger;

import java.util.Objects;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
 * Most applications do not need to pass arbitrary data to the pipeline and can pass {@code Context.NONE} or
 * {@code null}.
 * <p>
 * Each context object is immutable. The {@link #addData(Object, Object)} method creates a new
 * {@code Context} object that refers to its parent, forming a linked list.
 */
public class Context {
    private final ClientLogger logger = new ClientLogger(Context.class);

    // All fields must be immutable.
    //
    /**
     * Signifies that no data needs to be passed to the pipeline.
     */
    public static final Context NONE = new Context(null, null, null);

    private final Context parent;
    private final Object key;
    private final Object value;

    /**
     * Constructs a new {@link Context} object.
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context(Object key, Object value) {
        this.parent = null;
        this.key = Objects.requireNonNull(key, "'key' cannot be null.");
        this.value = value;
    }

    private Context(Context parent, Object key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    /**
     * Adds a new immutable {@link Context} object with the specified key-value pair to
     * the existing {@link Context} chain.
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     * @return the new {@link Context} object containing the specified pair added to the set of pairs.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key cannot be null"));
        }
        return new Context(this, key, value);
    }

    /**
     * Scans the linked-list of {@link Context} objects looking for one with the specified key.
     * Note that the first key found, i.e. the most recently added, will be returned.
     *
     * @param key The key to search for.
     * @return The value of the specified key if it exists.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Option<Object> getData(Object key) {
        if (key == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key cannot be null"));
        }
        for (Context c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return Option.of(c.value);
            }
        }
        return Option.uninitialized();
    }
}
