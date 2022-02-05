package com.ahmer.afzal.pdfviewer;

import android.icu.text.BreakIterator;
import android.os.Build;

public class BreakIteratorHelper {
    private BreakIterator BreakIteratorI;
    private java.text.BreakIterator BreakIteratorJ;

    public BreakIteratorHelper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            BreakIteratorI = BreakIterator.getWordInstance();
        } else {
            BreakIteratorJ = java.text.BreakIterator.getWordInstance();
        }
    }

    public void setText(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            BreakIteratorI.setText(text);
        } else {
            BreakIteratorJ.setText(text);
        }
    }

    public int following(int offset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return BreakIteratorI.following(offset);
        } else {
            return BreakIteratorJ.following(offset);
        }
    }

    public int previous() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return BreakIteratorI.previous();
        } else {
            return BreakIteratorJ.previous();
        }
    }
}
