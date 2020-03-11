package com.ahmer.afzal.pdfviewer.util;

import android.content.Context;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static int getDP(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n;
        while (-1 != (n = inputStream.read(buffer))) {
            os.write(buffer, 0, n);
        }
        return os.toByteArray();
    }
}
