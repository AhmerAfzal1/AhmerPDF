package com.ahmer.afzal.pdfviewer.source;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.ahmer.afzal.pdfium.PdfiumCore;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class FileSource implements DocumentSource {

    private final File file;

    public FileSource(File file) {
        this.file = file;
    }

    @Override
    public void createDocument(Context context, @NotNull PdfiumCore core, String password) throws IOException {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        core.newDocument(pfd, password);
    }
}
