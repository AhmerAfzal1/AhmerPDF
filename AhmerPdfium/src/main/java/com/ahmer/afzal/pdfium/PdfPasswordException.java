package com.ahmer.afzal.pdfium;

import androidx.annotation.Keep;

import java.io.IOException;

public class PdfPasswordException extends IOException {

    @Keep
    public PdfPasswordException() {
        super();
    }

    @Keep
    public PdfPasswordException(String detailMessage) {
        super(detailMessage);
    }
}
