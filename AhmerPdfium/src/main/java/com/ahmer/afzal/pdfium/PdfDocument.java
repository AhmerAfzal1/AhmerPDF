package com.ahmer.afzal.pdfium;

import android.annotation.TargetApi;
import android.graphics.RectF;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class PdfDocument {

    /*package*/ final Map<Integer, Long> mNativePagesPtr = new ArrayMap<>();
    /*package*/ final Map<Integer, Long> mNativeTextPagesPtr = new ArrayMap<>();
    /*package*/ final Map<Integer, Long> mNativeSearchHandlePtr = new ArrayMap<>();
    /*package*/ long mNativeDocPtr;
    /*package*/ ParcelFileDescriptor parcelFileDescriptor;

    /*package*/ PdfDocument() {
    }

    public boolean hasSearchHandle(int index) {
        return mNativeSearchHandlePtr.containsKey(index);
    }

    public boolean hasPage(int index) {
        return mNativePagesPtr.containsKey(index);
    }

    public boolean hasTextPage(int index) {
        return mNativeTextPagesPtr.containsKey(index);
    }

    public static class Meta {
        String title;
        String author;
        String subject;
        String keywords;
        String creator;
        String producer;
        String creationDate;
        String modDate;

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getSubject() {
            return subject;
        }

        public String getKeywords() {
            return keywords;
        }

        public String getCreator() {
            return creator;
        }

        public String getProducer() {
            return producer;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public String getModDate() {
            return modDate;
        }
    }

    public static class Bookmark {
        String title;
        long pageIdx;
        long mNativePtr;
        private List<Bookmark> children = new ArrayList<>();

        public List<Bookmark> getChildren() {
            return children;
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }

        public String getTitle() {
            return title;
        }

        public long getPageIdx() {
            return pageIdx;
        }
    }

    public static class Link {
        private RectF bounds;
        private Integer destPageIdx;
        private String uri;

        public Link(RectF bounds, Integer destPageIdx, String uri) {
            this.bounds = bounds;
            this.destPageIdx = destPageIdx;
            this.uri = uri;
        }

        public Integer getDestPageIdx() {
            return destPageIdx;
        }

        public String getUri() {
            return uri;
        }

        public RectF getBounds() {
            return bounds;
        }
    }
}