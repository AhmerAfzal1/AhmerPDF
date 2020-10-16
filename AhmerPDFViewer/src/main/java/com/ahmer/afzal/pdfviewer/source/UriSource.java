package com.ahmer.afzal.pdfviewer.source;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.ahmer.afzal.pdfium.PdfiumCore;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class UriSource implements DocumentSource {

    private final Uri uri;

    public UriSource(Uri uri) {
        this.uri = uri;
    }

    @Override
    public void createDocument(@NotNull Context context, @NotNull PdfiumCore core, String password) throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
        core.newDocument(pfd, password);
    }
}
