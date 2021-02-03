/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Portions Copyright (c) Microsoft Corporation
 */

package android.util;

import java.util.Objects;

/**
 * Mock Pair implementation for testing on non android host.
 *
 * @param <F> The type of first value in the pair.
 * @param <S> The type of second value in the pair.
 */
public class Pair<F, S> {
    /**
     * The first value in the pair.
     */
    public final F first;
    /**
     * The second value in the pair.
     */
    public final S second;

    /**
     * Creates a Mock pair.
     *
     * @param first The first value in the pair.
     * @param second The second value in the pair.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return "Pair{" + String.valueOf(first) + " " + String.valueOf(second) + "}";
    }

    /**
     * Creates a Mock pair.
     *
     * @param first The first value in the pair.
     * @param second The second value in the pair.
     * @param <A> The first value in the pair.
     * @param <B> The second value in the pair.
     * @return an instance of mock pair with given values.
     */
    public static <A, B> Pair <A, B> create(A first, B second) {
        return new Pair<A, B>(first, second);
    }
}
