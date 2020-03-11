package com.ahmer.afzal.pdfviewer.listener;

/**
 * Implement this interface to receive events from PDFView
 * when loading is complete.
 */
public interface OnLoadCompleteListener {

    /**
     * Called when the PDF is loaded
     *
     * @param nbPages the number of pages in this PDF file
     */
    void loadComplete(int nbPages);
}
