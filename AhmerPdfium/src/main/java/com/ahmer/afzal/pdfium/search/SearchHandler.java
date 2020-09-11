package com.ahmer.afzal.pdfium.search;

import android.graphics.Rect;

public interface SearchHandler {
    int getStartIndex();

    int getStopIndex();

    Rect[] getResults();

}
