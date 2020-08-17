package com.azure.android.core.util.paging;

import java.util.List;

public interface Page<T> {
    List<T> getItems();
}
