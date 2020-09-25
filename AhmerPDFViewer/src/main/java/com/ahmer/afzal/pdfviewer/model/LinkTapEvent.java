package com.ahmer.afzal.pdfviewer.model;

import android.graphics.RectF;

import com.ahmer.afzal.pdfium.PdfDocument;

public class LinkTapEvent {

    private final float originalX;
    private final float originalY;
    private final float documentX;
    private final float documentY;
    private final RectF mappedLinkRect;
    private final PdfDocument.Link link;

    public LinkTapEvent(float originalX, float originalY, float documentX, float documentY, RectF mappedLinkRect, PdfDocument.Link link) {
        this.originalX = originalX;
        this.originalY = originalY;
        this.documentX = documentX;
        this.documentY = documentY;
        this.mappedLinkRect = mappedLinkRect;
        this.link = link;
    }

    public float getOriginalX() {
        return originalX;
    }

    public float getOriginalY() {
        return originalY;
    }

    public float getDocumentX() {
        return documentX;
    }

    public float getDocumentY() {
        return documentY;
    }

    public RectF getMappedLinkRect() {
        return mappedLinkRect;
    }

    public PdfDocument.Link getLink() {
        return link;
    }
}
