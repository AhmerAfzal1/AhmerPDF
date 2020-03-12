package com.ahmer.afzal.pdfviewer.search;

import android.graphics.RectF;

public class SearchItem {

    private int currentIndex;
    private int pageIndex;
    private RectF bounds;

    public SearchItem(int currentIndex, int pageIndex, RectF bounds) {
        this.currentIndex = currentIndex;
        this.pageIndex = pageIndex;
        this.bounds = bounds;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public RectF getBounds() {
        return bounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchItem item = (SearchItem) o;

        if (currentIndex != item.currentIndex) return false;
        if (pageIndex != item.pageIndex) return false;
        return bounds != null ? bounds.equals(item.bounds) : item.bounds == null;
    }

    @Override
    public int hashCode() {
        int result = currentIndex;
        result = 31 * result + pageIndex;
        result = 31 * result + (bounds != null ? bounds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "currentIndex=" + currentIndex +
                ", pageIndex=" + pageIndex +
                ", bounds=" + bounds +
                '}';
    }
}