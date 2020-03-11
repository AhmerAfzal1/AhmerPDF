package com.ahmer.afzal.pdfviewer.link;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ahmer.afzal.pdfviewer.PDFView;
import com.ahmer.afzal.pdfviewer.model.LinkTapEvent;

public class DefaultLinkHandler implements LinkHandler {

    private static final String TAG = DefaultLinkHandler.class.getSimpleName();

    private PDFView pdfView;

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
        Context context = pdfView.getContext();
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Log.w(TAG, "No activity found for URI: " + uri);
        }
    }

    private void handlePage(int page) {
        pdfView.jumpTo(page);
    }
}
