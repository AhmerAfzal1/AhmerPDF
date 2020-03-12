package com.ahmer.afzal.pdfium;

import android.graphics.RectF;

public abstract class SearchHandle {

    protected PdfDocument document;
    protected int pageIndex;
    protected String findWhat;
    protected boolean matchCase;
    protected boolean matchWholeWord;

    protected SearchHandle(PdfDocument document, int pageIndex, String findWhat, boolean matchCase, boolean matchWholeWord) {
        this.document = document;
        this.pageIndex = pageIndex;
        this.findWhat = findWhat;
        this.matchCase = matchCase;
        this.matchWholeWord = matchWholeWord;
        prepareSearch();
    }

    protected abstract void prepareSearch();

    public RectF startSearch() {
        return searchNext();
    }

    public abstract RectF searchNext();

    public abstract RectF searchPrev();

    public abstract void stopSearch();

    public abstract int countResult();
}
