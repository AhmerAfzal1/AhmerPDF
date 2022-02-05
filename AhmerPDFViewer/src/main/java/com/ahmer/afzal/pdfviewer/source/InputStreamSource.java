package com.ahmer.afzal.pdfviewer.source;

import android.content.Context;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfviewer.util.PdfUtils;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamSource implements DocumentSource {

    private final InputStream inputStream;

    public InputStreamSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        return core.newDocument(PdfUtils.toByteArray(inputStream), password);
    }
}
