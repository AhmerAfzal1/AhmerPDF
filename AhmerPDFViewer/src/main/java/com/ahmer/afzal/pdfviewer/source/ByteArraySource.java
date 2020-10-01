package com.ahmer.afzal.pdfviewer.source;

import android.content.Context;

import com.ahmer.afzal.pdfium.PdfiumCore;

import java.io.IOException;

public class ByteArraySource implements DocumentSource {

    private final byte[] data;

    public ByteArraySource(byte[] data) {
        this.data = data;
    }

    @Override
    public void createDocument(Context context, PdfiumCore core, String password) throws IOException {
        core.newDocument(data, password);
    }
}
