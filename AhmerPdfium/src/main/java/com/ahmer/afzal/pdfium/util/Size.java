package com.ahmer.afzal.pdfium.util;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

public class Size {

    private final int width;
    private final int height;

    @Keep
    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof Size) {
            Size other = (Size) obj;
            return width == other.width && height == other.height;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return width + "x" + height;
    }

    @Override
    public int hashCode() {
        // Assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return height ^ ((width << (Integer.SIZE / 2)) | (width >>> (Integer.SIZE / 2)));
    }
}
