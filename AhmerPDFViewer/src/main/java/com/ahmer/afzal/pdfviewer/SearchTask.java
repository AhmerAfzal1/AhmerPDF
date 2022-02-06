package com.ahmer.afzal.pdfviewer;

import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfviewer.model.SearchRecord;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchTask implements Runnable {
    public final AtomicBoolean abort = new AtomicBoolean();
    public final String key;
    public final int flag = 0;
    private final ArrayList<SearchRecord> arr = new ArrayList<>();
    private final WeakReference<PDFView> pdfView;
    private Thread t;
    private long keyStr;

    private boolean finished;

    public SearchTask(PDFView pdfView, String key) {

        this.pdfView = new WeakReference<>(pdfView);
        this.key = key + "\0";
    }

    public long getKeyStr() {
        if (keyStr == 0) {
            keyStr = PdfiumCore.nativeGetStringChars(key);
        }
        return keyStr;
    }

    @Override
    public void run() {
        PDFView a = this.pdfView.get();
        if (a == null) {
            return;
        }
        if (finished) {
            //a.setSearchResults(arr);
            //a.showT("findAllTest_Time : "+(System.currentTimeMillis()-CMN.ststrt)+" sz="+arr.size());
            a.endSearch(arr);
        } else {


            SearchRecord schRecord;
            for (int i = 0; i < a.getPageCount(); i++) {
                if (abort.get()) {
                    break;
                }
                schRecord = a.findPageCached(key, i, 0);

                if (schRecord != null) {
                    a.notifyItemAdded(this, arr, schRecord, i);
                } else {
                    //  a.notifyProgress(i);
                }
            }

            finished = true;
            t = null;
            a.post(this);
        }
    }

    public void start() {
        if (finished) {
            return;
        }
        if (t == null) {
            PDFView a = this.pdfView.get();
            if (a != null) {
                a.startSearch(arr, key, flag);
            }
            t = new Thread(this);
            t.start();
        }
    }

    public void abort() {
        abort.set(true);
    }

    public boolean isAborted() {
        return abort.get();
    }
}
