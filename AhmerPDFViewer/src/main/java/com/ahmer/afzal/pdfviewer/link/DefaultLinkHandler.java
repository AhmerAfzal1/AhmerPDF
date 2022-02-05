package com.ahmer.afzal.pdfviewer.link;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.ahmer.afzal.pdfviewer.PDFView;
import com.ahmer.afzal.pdfviewer.model.LinkTapEvent;
import com.ahmer.afzal.pdfviewer.util.PdfConstants;

public class DefaultLinkHandler implements LinkHandler {

    private final PDFView pdfView;

    public DefaultLinkHandler(PDFView pdfView) {
        this.pdfView = pdfView;
    }

    @Override
    public void handleLinkEvent(LinkTapEvent event) {
        String uri = event.getLink().getUri();
        Integer page = event.getLink().getDestPageIdx();
        if (uri != null && !uri.isEmpty()) {
            handleUri(uri);
        } else if (page != null) {
            handlePage(page);
        }
    }

    private void handleUri(String uri) {
        Uri parsedUri = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, parsedUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        Context context = pdfView.getContext();
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(PdfConstants.TAG, "No activity found for URI: " + uri);
        }
    }

    private void handlePage(int page) {
        pdfView.jumpTo(page);
    }
}
