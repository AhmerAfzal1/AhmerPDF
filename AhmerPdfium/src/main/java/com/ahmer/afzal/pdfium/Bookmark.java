package com.ahmer.afzal.pdfium;

import java.util.ArrayList;
import java.util.List;

public class Bookmark {

    private final List<Bookmark> children = new ArrayList<>();
    String title;
    long pageIdx;
    long mNativePtr;

    public List<Bookmark> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public long getPageIdx() {
        return pageIdx;
    }
}
