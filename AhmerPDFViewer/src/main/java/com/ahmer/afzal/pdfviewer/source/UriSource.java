package com.ahmer.afzal.pdfviewer.source;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;

import java.io.IOException;

public class UriSource implements DocumentSource {

    private Uri uri;

    public UriSource(Uri uri) {
        this.uri = uri;
    }

    @Override
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
        return core.newDocument(pfd, password);
    }
}
