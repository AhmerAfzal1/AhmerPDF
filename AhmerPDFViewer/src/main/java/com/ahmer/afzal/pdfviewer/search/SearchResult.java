package com.ahmer.afzal.pdfviewer.search;

import java.util.Collections;
import java.util.List;

public class SearchResult {

    private final List<SearchItem> results;
    private SearchItem selectedItem;


    public SearchResult(List<SearchItem> partialResults) {
        if (partialResults.size() > 0) {
            selectedItem = partialResults.get(0);
        }
        results = Collections.unmodifiableList(partialResults);
    }

    public int getResultCount() {
        if (results != null) {
            return results.size();
        } else {
            return -1;
        }
    }

    public List<SearchItem> getResults() {
        return results;
    }

    public SearchItem getSelectedItem() {
        return selectedItem;
    }

    public int getCurrentIndex() {
        if (selectedItem != null) {
            return selectedItem.getCurrentIndex();
        } else {
            return 0;
        }
    }

    public SearchItem getNextResult() {
        if (selectedItem == null || results == null) return null;
        int nextIndex = results.indexOf(selectedItem) + 1;
        if (nextIndex < results.size()) {
            SearchItem next = results.get(nextIndex);
            selectedItem = next;
            return next;
        } else {
            return null;
        }
    }

    public SearchItem getPrevResult() {
        if (selectedItem == null || results == null) return null;
        int prevIndex = results.indexOf(selectedItem) - 1;
        if (prevIndex >= 0) {
            SearchItem prev = results.get(prevIndex);
            selectedItem = prev;
            return prev;
        } else {
            return null;
        }
    }
}