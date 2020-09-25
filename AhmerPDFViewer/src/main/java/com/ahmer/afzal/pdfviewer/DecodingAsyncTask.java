package com.ahmer.afzal.pdfviewer;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfium.util.Size;
import com.ahmer.afzal.pdfviewer.source.DocumentSource;
import com.ahmer.afzal.utils.async.AsyncTask;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;
    private PDFView pdfView;
    private PdfiumCore pdfiumCore;
    private String password;
    private DocumentSource docSource;
    private int[] userPages;
    private PdfFile pdfFile;

    DecodingAsyncTask(DocumentSource docSource, String password, int[] userPages, PDFView pdfView, PdfiumCore pdfiumCore) {
        this.docSource = docSource;
        this.userPages = userPages;
        this.cancelled = false;
        this.pdfView = pdfView;
        this.password = password;
        this.pdfiumCore = pdfiumCore;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Throwable doInBackground(Void aVoid) throws Exception {
        if (pdfView != null) {
            PdfDocument pdfDocument = docSource.createDocument(pdfView.getContext(), pdfiumCore, password);
            pdfFile = new PdfFile(pdfiumCore, pdfDocument, pdfView.getPageFitPolicy(), getViewSize(pdfView),
                    userPages, pdfView.isOnDualPageMode(), pdfView.isSwipeVertical(), pdfView.getSpacingPx(),
                    pdfView.isAutoSpacingEnabled(), pdfView.isFitEachPage(), pdfView.isOnLandscapeOrientation());
            return null;
        } else {
            return new NullPointerException("pdfView == null");
        }
    }

    @Override
    protected void onPostExecute(Throwable t) {
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
    protected void onBackgroundError(Exception e) {
        e.printStackTrace();
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }

    private Size getViewSize(PDFView pdfView) {
        return new Size(pdfView.getWidth(), pdfView.getHeight());
    }

}
