package com.ahmer.afzal.pdfium;

import android.graphics.RectF;

public class Link {

    private final RectF bounds;
    private final Integer destPageIdx;
    private final String uri;

    public Link(RectF bounds, Integer destPageIdx, String uri) {
        this.bounds = bounds;
        this.destPageIdx = destPageIdx;
        this.uri = uri;
    }

    public Integer getDestPageIdx() {
        return destPageIdx;
    }

    public String getUri() {
        return uri;
    }

    public RectF getBounds() {
        return bounds;
    }
}
