package com.azure.android.core.util.paging;

import java.util.List;

public interface Page<T> {
    String getPageId();
    String getNextPageId();
    List<T> getItems();
}
