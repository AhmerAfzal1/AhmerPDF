package com.ahmer.ahmerpdf;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import com.ahmer.afzal.pdfium.Meta;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfviewer.PDFView;
import com.ahmer.afzal.utils.utilcode.FileUtils;
import com.ahmer.ahmerpdf.databinding.DialogPdfInfoBinding;

import java.io.File;
import java.util.Locale;

public class DialogMoreInfo extends Dialog {

    private final File file;
    private Meta meta = null;

    public DialogMoreInfo(@NonNull Context context, final File file) {
        super(context);
        this.file = file;
        try {
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfiumCore.newDocument(fileDescriptor);
            meta = pdfiumCore.getDocumentMeta();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DialogMoreInfo(@NonNull Context context, File file, String password) {
        super(context);
        this.file = file;
        try {
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfiumCore.newDocument(fileDescriptor, password);
            meta = pdfiumCore.getDocumentMeta();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DialogMoreInfo(@NonNull Context context, PDFView pdfView, File file) {
        super(context);
        this.file = file;
        meta = pdfView.getDocumentMeta();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogPdfInfoBinding binding = DialogPdfInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvTitle.setText(meta.getTitle());
        binding.tvAuthor.setText(meta.getAuthor());
        binding.tvTotalPage.setText(String.format(Locale.getDefault(), "%d", meta.getTotalPages()));
        binding.tvSubject.setText(meta.getSubject());
        binding.tvKeywords.setText(meta.getKeywords());
        binding.tvCreationDate.setText(meta.getCreationDate());
        binding.tvModifyDate.setText(meta.getModDate());
        binding.tvCreator.setText(meta.getCreator());
        binding.tvProducer.setText(meta.getProducer());
        binding.tvFileSize.setText(FileUtils.getSize(file));
        binding.tvFilePath.setText(file.getPath());
        binding.tvOk.setOnClickListener(v -> dismiss());
    }
}