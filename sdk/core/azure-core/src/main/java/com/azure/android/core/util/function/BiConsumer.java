package com.azure.android.core.util.function;

/**
 * Simple interface make code compatible with Java 7
 */
public interface BiConsumer<Input1, Input2> {
    void accept(Input1 input1, Input2 input2);
}
