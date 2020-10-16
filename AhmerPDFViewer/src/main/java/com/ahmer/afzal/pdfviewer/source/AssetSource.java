package com.ahmer.afzal.pdfviewer.source;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfviewer.util.PdfFileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class AssetSource implements DocumentSource {

    private final String assetName;

    public AssetSource(String assetName) {
        this.assetName = assetName;
    }

    @Override
    public void createDocument(Context context, @NotNull PdfiumCore core, String password) throws IOException {
        File f = PdfFileUtils.fileFromAsset(context, assetName);
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
        core.newDocument(pfd, password);
    }
}
