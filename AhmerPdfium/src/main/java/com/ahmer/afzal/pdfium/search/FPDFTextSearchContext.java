package com.ahmer.afzal.pdfium.search;

import com.ahmer.afzal.pdfium.PdfDocument;

public abstract class FPDFTextSearchContext implements TextSearchContext {
    protected final PdfDocument document;
    protected final int pageIndex;
    protected final String query;
    protected final boolean matchCase;
    protected final boolean matchWholeWord;
    protected boolean mHasNext = true;
    protected boolean mHasPrev = false;

    protected FPDFTextSearchContext(PdfDocument doc, int pageIndex, String query, boolean matchCase, boolean matchWholeWord) {
        this.document = doc;
        this.pageIndex = pageIndex;
        this.query = query;
        this.matchCase = matchCase;
        this.matchWholeWord = matchWholeWord;
        prepareSearch();
    }

    @Override
    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public boolean isMatchCase() {
        return matchCase;
    }

    @Override
    public boolean isMatchWholeWord() {
        return matchWholeWord;
    }

    @Override
    public boolean hasNext() {
        return countResult() > 0 || mHasNext;
    }

    @Override
    public boolean hasPrev() {
        return countResult() > 0 || mHasPrev;
    }

    @Override
    public void startSearch() {
        searchNext();
    }

    @Override
    public void stopSearch() {

    }

    public int getFirstCharIndex() {
        return -1;
    }
}