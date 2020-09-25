package com.ahmer.ahmerpdf;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.utils.utilcode.FileUtils;
import com.ahmer.afzal.utils.utilcode.ThrowableUtils;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class DialogInfo {

    private final Context context;
    private final File file;
    private final String password;

    public DialogInfo(Context context, File file) {
        this.context = context;
        this.file = file;
        password = null;
    }

    public DialogInfo(Context context, File file, String password) {
        this.context = context;
        this.file = file;
        this.password = password;
    }

    public void showPDFInfo() {
        Dialog dialog = new Dialog(context);
        try {
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(Uri.fromFile(file), "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd, password);
            PdfDocument.Meta meta = pdfiumCore.getDocumentMeta(pdfDocument);
            Objects.requireNonNull(dialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setContentView(R.layout.dialog_pdf_info);
            dialog.getWindow().setLayout(-1, -2);
            dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            ((TextView) dialog.findViewById(R.id.tvTitle)).setText(meta.getTitle());
            ((TextView) dialog.findViewById(R.id.tvAuthor)).setText(meta.getAuthor());
            ((TextView) dialog.findViewById(R.id.tvTotalPage)).setText(String.format(
                    Locale.getDefault(), "%d", pdfiumCore.getPageCount(pdfDocument)));
            ((TextView) dialog.findViewById(R.id.tvSubject)).setText(meta.getSubject());
            ((TextView) dialog.findViewById(R.id.tvKeywords)).setText(meta.getKeywords());
            ((TextView) dialog.findViewById(R.id.tvCreationDate)).setText(meta.getCreationDate());
            ((TextView) dialog.findViewById(R.id.tvModifyDate)).setText(meta.getModDate());
            ((TextView) dialog.findViewById(R.id.tvCreator)).setText(meta.getCreator());
            ((TextView) dialog.findViewById(R.id.tvProducer)).setText(meta.getProducer());
            ((TextView) dialog.findViewById(R.id.tvFileSize)).setText(FileUtils.getSize(file));
            ((TextView) dialog.findViewById(R.id.tvFilePath)).setText(file.getPath());
            dialog.findViewById(R.id.tvOk).setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Log.v(MainActivity.TAG, getClass().getSimpleName() + " -> Exception showPDFInfo: " + e.getMessage());
            ThrowableUtils.getFullStackTrace(e);
        }
    }
}