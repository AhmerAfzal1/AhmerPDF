package com.ahmer.afzal.pdfviewer.link;

import com.ahmer.afzal.pdfviewer.model.LinkTapEvent;

public interface LinkHandler {

    /**
     * Called when link was tapped by user
     *
     * @param event current event
     */
    void handleLinkEvent(LinkTapEvent event);
}