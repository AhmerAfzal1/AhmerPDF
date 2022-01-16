package com.ahmer.afzal.pdfium.writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class PDFWriterImpl implements PDFWriter {

    private final File file;

    public PDFWriterImpl(File file) {
        this.file = file;
    }

    @Override
    public void write(ByteBuffer buffer) {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] arr;
            if (buffer.hasArray()) {
                arr = buffer.array();
                out.write(arr, buffer.arrayOffset(), buffer.capacity());
            } else {
                arr = new byte[buffer.capacity()];
                buffer.get(arr);
                out.write(arr);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
