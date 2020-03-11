package com.ahmer.afzal.pdfviewer;

import android.os.AsyncTask;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfium.util.Size;
import com.ahmer.afzal.pdfviewer.source.DocumentSource;

import java.lang.ref.WeakReference;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;
    private WeakReference<PDFView> pdfViewReference;
    private PdfiumCore pdfiumCore;
    private String password;
    private DocumentSource docSource;
    private int[] userPages;
    private PdfFile pdfFile;

    DecodingAsyncTask(DocumentSource docSource, String password, int[] userPages, PDFView pdfView, PdfiumCore pdfiumCore) {
        this.docSource = docSource;
        this.userPages = userPages;
        this.cancelled = false;
        this.pdfViewReference = new WeakReference<>(pdfView);
        this.password = password;
        this.pdfiumCore = pdfiumCore;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            PDFView pdfView = pdfViewReference.get();
            if (pdfView != null) {
                PdfDocument pdfDocument = docSource.createDocument(pdfView.getContext(), pdfiumCore, password);
                pdfFile = new PdfFile(pdfiumCore, pdfDocument, pdfView.getPageFitPolicy(), getViewSize(pdfView),
                        userPages, pdfView.isSwipeVertical(), pdfView.getSpacingPx(), pdfView.isAutoSpacingEnabled(),
                        pdfView.isFitEachPage());
                return null;
            } else {
                return new NullPointerException("pdfView == null");
            }

        } catch (Throwable t) {
            return t;
        }
    }

    private Size getViewSize(PDFView pdfView) {
        return new Size(pdfView.getWidth(), pdfView.getHeight());
    }

    @Override
    protected void onPostExecute(Throwable t) {
        PDFView pdfView = pdfViewReference.get();
        if (pdfView != null) {
            if (t != null) {
                pdfView.loadError(t);
                return;
            }
            if (!cancelled) {
                pdfView.loadComplete(pdfFile);
            }
        }
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }
}
