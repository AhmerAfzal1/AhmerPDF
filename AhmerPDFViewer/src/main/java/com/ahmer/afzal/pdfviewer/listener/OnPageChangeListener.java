package com.ahmer.afzal.pdfviewer.listener;

/**
 * Implements this interface to receive events from PDFView
 * when a page has changed through swipe
 */
public interface OnPageChangeListener {

    /**
     * Called when the user use swipe to change page
     *
     * @param page      the new page displayed, starting from 0
     * @param pageCount the total page count
     */
    void onPageChanged(int page, int pageCount);
}
