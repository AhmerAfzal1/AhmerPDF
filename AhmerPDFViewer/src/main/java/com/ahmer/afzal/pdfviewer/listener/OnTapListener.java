package com.ahmer.afzal.pdfviewer.listener;

import android.view.MotionEvent;

/**
 * Implement this interface to receive events from PDFView
 * when view has been touched
 */
public interface OnTapListener {

    /**
     * Called when the user has a tap gesture, before processing scroll handle toggling
     *
     * @param e MotionEvent that registered as a confirmed single tap
     * @return true if the single tap was handled, false to toggle scroll handle
     */
    boolean onTap(MotionEvent e);
}
