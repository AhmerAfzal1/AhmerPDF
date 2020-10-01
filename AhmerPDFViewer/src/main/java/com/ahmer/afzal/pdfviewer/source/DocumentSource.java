package com.ahmer.afzal.pdfviewer.source;

import android.content.Context;

import com.ahmer.afzal.pdfium.PdfiumCore;

import java.io.IOException;

public interface DocumentSource {

    void createDocument(Context context, PdfiumCore core, String password) throws IOException;
}
