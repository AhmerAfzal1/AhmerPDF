package com.ahmer.afzal.pdfviewer.model;

public class SearchRecord {
    public final int pageIdx;
    public final int findStart;
    public int currentPage = -1;
    public Object data;

    public SearchRecord(int pageIdx, int findStart) {
        this.pageIdx = pageIdx;
        this.findStart = findStart;
    }
}
