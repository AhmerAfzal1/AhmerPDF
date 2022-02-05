package com.ahmer.afzal.pdfium;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Surface;

import com.ahmer.afzal.pdfium.util.Size;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfiumCore {

    public static final Object lock = new Object();
    /* synchronize native methods */
    private static final String TAG = PdfiumCore.class.getName();
    private static int mCurrentDpi = 0;

    static {
        System.loadLibrary("pdfsdk");
        System.loadLibrary("pdfsdk_jni");
    }

    /**
     * Context needed to get screen density
     */
    public PdfiumCore(@NotNull Context context) {
        mCurrentDpi = context.getResources().getDisplayMetrics().densityDpi;
        Log.d(TAG, "Starting AhmerPdfium...");
    }

    private native int nativeCountAndGetRects(long pagePtr, int offsetY, int offsetX, int width, int height, ArrayList<RectF> arr, long tid, int selSt, int selEd);

    private native int nativeGetPageCount(long docPtr);

    private native int nativeGetPageHeightPixel(long pagePtr, int dpi);

    private native int nativeGetPageHeightPoint(long pagePtr);

    private native int nativeGetPageRotation(long pagePtr);

    private native int nativeGetPageWidthPixel(long pagePtr, int dpi);

    private native int nativeGetPageWidthPoint(long pagePtr);

    private native Integer nativeGetDestPageIndex(long docPtr, long linkPtr);

    private native long nativeGetBookmarkDestIndex(long docPtr, long bookmarkPtr);

    private native Long nativeGetFirstChildBookmark(long docPtr, Long bookmarkPtr);

    private native Long nativeGetSiblingBookmark(long docPtr, long bookmarkPtr);

    private native long nativeLoadPage(long docPtr, int pageIndex);

    private native long nativeOpenDocument(int fd, String password);

    private native long nativeOpenMemDocument(byte[] data, String password);

    private native long[] nativeGetPageLinks(long pagePtr);

    private native long[] nativeLoadPages(long docPtr, int fromIndex, int toIndex);

    private native Point nativePageCoordsToDevice(long pagePtr, int startX, int startY, int sizeX, int sizeY, int rotate, double pageX, double pageY);

    private native RectF nativeGetLinkRect(long linkPtr);

    private native Size nativeGetPageSizeByIndex(long docPtr, int pageIndex, int dpi);

    private native String nativeGetBookmarkTitle(long bookmarkPtr);

    private native String nativeGetDocumentMetaText(long docPtr, String tag);

    private native String nativeGetLinkURI(long docPtr, long linkPtr);

    private native void nativeCloseDocument(long docPtr);

    private native void nativeClosePage(long pagePtr);

    private native void nativeClosePages(long[] pagesPtr);

    private native void nativeRenderPage(long pagePtr, Surface surface, int startX, int startY, int drawSizeHor, int drawSizeVer, boolean renderAnnot);

    private native void nativeRenderPageBitmap(long docPtr, long pagePtr, Bitmap bitmap, int startX, int startY, int drawSizeHor, int drawSizeVer, boolean renderAnnot);

    private native void nativeRenderPageBitmap(long pagePtr, Bitmap bitmap, int startX, int startY, int drawSizeHor, int drawSizeVer, boolean renderAnnot);

    public native boolean nativeFindTextPageNext(long searchPtr);

    public native boolean nativeGetRect(long pagePtr, int offsetY, int offsetX, int width, int height, long textPtr, RectF rect, int idx);

    public native int nativeCountRects(long textPtr, int st, int ed);

    public native int nativeFindTextPage(long pagePtr, String key, int flag);

    public native int nativeGetCharIndexAtCoord(long pagePtr, double width, double height, long textPtr, double posX, double posY, double tolX, double tolY);

    public native int nativeGetCharPos(long pagePtr, int offsetY, int offsetX, int width, int height, RectF pt, long tid, int index, boolean loose);

    public native int nativeGetFindIdx(long searchPtr);

    public native int nativeGetFindLength(long searchPtr);

    public native int nativeGetMixedLooseCharPos(long pagePtr, int offsetY, int offsetX, int width, int height, RectF pt, long tid, int index, boolean loose);

    public native long nativeFindTextPageStart(long textPtr, long keyStr, int flag, int startIdx);

    public native long nativeGetLinkAtCoord(long pagePtr, double width, double height, double posX, double posY);

    public native long nativeLoadTextPage(long pagePtr);

    public native String nativeGetLinkTarget(long docPtr, long linkPtr);

    public native String nativeGetText(long textPtr);

    public native void nativeFindTextPageEnd(long searchPtr);

    public static native long nativeGetStringChars(String key);

    /**
     * Create new document from file
     */
    public PdfDocument newDocument(ParcelFileDescriptor fd) throws IOException {
        return newDocument(fd, null);
    }

    /**
     * Create new document from file with password
     */
    public PdfDocument newDocument(ParcelFileDescriptor fd, String password) throws IOException {
        PdfDocument document = new PdfDocument();
        document.parcelFileDescriptor = fd;
        synchronized (lock) {
            document.mNativeDocPtr = nativeOpenDocument(fd.getFd(), password);
        }
        return document;
    }

    /**
     * Create new document from bytearray
     */
    public PdfDocument newDocument(byte[] data) throws IOException {
        return newDocument(data, null);
    }

    /**
     * Create new document from bytearray with password
     */
    public PdfDocument newDocument(byte[] data, String password) throws IOException {
        PdfDocument document = new PdfDocument();
        synchronized (lock) {
            document.mNativeDocPtr = nativeOpenMemDocument(data, password);
        }
        return document;
    }

    /**
     * Get total numer of pages in document
     */
    public int getPageCount(PdfDocument doc) {
        synchronized (lock) {
            return nativeGetPageCount(doc.mNativeDocPtr);
        }
    }

    /**
     * Open page
     */
    public long openPage(PdfDocument doc, int pageIndex) {
        long pagePtr;
        synchronized (lock) {
            pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex);
            doc.mNativePagesPtr.put(pageIndex, pagePtr);
            return pagePtr;
        }
    }

    /**
     * Open range of pages
     */
    public long[] openPage(PdfDocument doc, int fromIndex, int toIndex) {
        long[] pagesPtr;
        synchronized (lock) {
            pagesPtr = nativeLoadPages(doc.mNativeDocPtr, fromIndex, toIndex);
            int pageIndex = fromIndex;
            for (long page : pagesPtr) {
                if (pageIndex > toIndex) break;
                doc.mNativePagesPtr.put(pageIndex, page);
                pageIndex++;
            }
            return pagesPtr;
        }
    }

    public long openText(long pagePtr) {
        synchronized (lock) {
            return nativeLoadTextPage(pagePtr);
        }
    }

    /**
     * Close page
     */
    /*
    public void closePage(PdfDocument doc, int pageIndex) {
        synchronized (lock) {
            Long pagePtr = doc.mNativePagesPtr.get(pageIndex);
            if (pagePtr != null) {
                nativeClosePage(pagePtr);
                doc.mNativePagesPtr.remove(pageIndex);
            }
        }
    }*/
    public int getTextRects(long pagePtr, int offsetY, int offsetX, Size size, ArrayList<RectF> arr, long textPtr, int selSt, int selEd) {
        synchronized (lock) {
            return nativeCountAndGetRects(pagePtr, offsetY, offsetX, size.getWidth(), size.getHeight(), arr, textPtr, selSt, selEd);
        }
    }

    /**
     * Get page width in pixels.
     * This method requires page to be opened.
     */
    public int getPageWidth(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageWidthPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }

    /**
     * Get page height in pixels.
     * This method requires page to be opened.
     */
    public int getPageHeight(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageHeightPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).
     * This method requires page to be opened.
     */
    public int getPageWidthPoint(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageWidthPoint(pagePtr);
            }
            return 0;
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).
     * This method requires page to be opened.
     */
    public int getPageHeightPoint(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageHeightPoint(pagePtr);
            }
            return 0;
        }
    }

    /**
     * Get size of page in pixels.
     * This method does not require given page to be opened.
     */
    public Size getPageSize(PdfDocument doc, int index) {
        synchronized (lock) {
            return nativeGetPageSizeByIndex(doc.mNativeDocPtr, index, mCurrentDpi);
        }
    }

    /**
     * Render page fragment on {@link Surface}.
     * Page must be opened before rendering.
     */
    public void renderPage(PdfDocument doc, Surface surface, int pageIndex,
                           int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPage(doc, surface, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Surface}. This method allows to render annotations.
     * Page must be opened before rendering.
     */
    public void renderPage(PdfDocument doc, Surface surface, int pageIndex, int startX, int startY,
                           int drawSizeX, int drawSizeY, boolean renderAnnot) {
        Long mDoc = doc.mNativePagesPtr.get(pageIndex);
        if (mDoc != null) {
            synchronized (lock) {
                try {
                    nativeRenderPage(mDoc, surface, startX, startY, drawSizeX, drawSizeY, renderAnnot);
                } catch (NullPointerException e) {
                    Log.e(TAG, "mContext may be null");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "Exception throw from native");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Render page fragment on {@link Bitmap}.
     * Page must be opened before rendering.
     * <p>
     * Supported bitmap configurations:
     * <p>
     * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     * RGB_565 - little worse quality, twice less memory usage
     */
    public void renderPageBitmap(PdfDocument doc, Bitmap bitmap, int pageIndex, int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPageBitmap(doc, bitmap, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Bitmap}. This method allows to render annotations.
     * Page must be opened before rendering.
     * <p>
     * For more info see {PdfiumCore#renderPageBitmap(Bitmap, int, int, int, int, int, boolean)}
     */
    public void renderPageBitmap(PdfDocument doc, Bitmap bitmap, int pageIndex, int startX,
                                 int startY, int drawSizeX, int drawSizeY, boolean renderAnnot) {
        Long mDoc = doc.mNativePagesPtr.get(pageIndex);
        if (mDoc != null) {
            synchronized (lock) {
                try {
                    nativeRenderPageBitmap(mDoc, bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot);
                } catch (NullPointerException e) {
                    Log.e(TAG, "mContext may be null");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "Exception throw from native");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Release native resources and opened file
     */
    public void closeDocument(PdfDocument doc) {
        synchronized (lock) {
            for (Integer index : doc.mNativePagesPtr.keySet()) {
                Long mDoc = doc.mNativePagesPtr.get(index);
                if (mDoc != null) {
                    nativeClosePage(mDoc);
                }
            }
            doc.mNativePagesPtr.clear();
            nativeCloseDocument(doc.mNativeDocPtr);
            if (doc.parcelFileDescriptor != null) { //if document was loaded from file
                try {
                    doc.parcelFileDescriptor.close();
                } catch (IOException e) {
                    /* ignore */
                }
                doc.parcelFileDescriptor = null;
            }
        }
    }

    /**
     * Get metadata for given document
     */
    public PdfDocument.Meta getDocumentMeta(PdfDocument doc) {
        synchronized (lock) {
            PdfDocument.Meta meta = new PdfDocument.Meta();
            meta.title = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Title");
            meta.author = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Author");
            meta.subject = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Subject");
            meta.keywords = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Keywords");
            meta.creator = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Creator");
            meta.producer = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Producer");
            meta.creationDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "CreationDate");
            meta.modDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "ModDate");
            meta.totalPages = getPageCount(doc);
            return meta;
        }
    }

    /**
     * Get table of contents (bookmarks) for given document
     */
    public List<PdfDocument.Bookmark> getTableOfContents(PdfDocument doc) {
        synchronized (lock) {
            List<PdfDocument.Bookmark> topLevel = new ArrayList<>();
            Long first = nativeGetFirstChildBookmark(doc.mNativeDocPtr, null);
            if (first != null) {
                recursiveGetBookmark(topLevel, doc, first);
            }
            return topLevel;
        }
    }

    private void recursiveGetBookmark(List<PdfDocument.Bookmark> tree, PdfDocument doc, long bookmarkPtr) {
        PdfDocument.Bookmark bookmark = new PdfDocument.Bookmark();
        bookmark.mNativePtr = bookmarkPtr;
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr);
        bookmark.pageIdx = nativeGetBookmarkDestIndex(doc.mNativeDocPtr, bookmarkPtr);
        tree.add(bookmark);
        Long child = nativeGetFirstChildBookmark(doc.mNativeDocPtr, bookmarkPtr);
        if (child != null) {
            recursiveGetBookmark(bookmark.getChildren(), doc, child);
        }
        Long sibling = nativeGetSiblingBookmark(doc.mNativeDocPtr, bookmarkPtr);
        if (sibling != null) {
            recursiveGetBookmark(tree, doc, sibling);
        }
    }

    /**
     * Get all links from given page
     */
    public List<PdfDocument.Link> getPageLinks(PdfDocument doc, int pageIndex) {
        synchronized (lock) {
            List<PdfDocument.Link> links = new ArrayList<>();
            Long nativePagePtr = doc.mNativePagesPtr.get(pageIndex);
            if (nativePagePtr == null) {
                return links;
            }
            long[] linkPtrs = nativeGetPageLinks(nativePagePtr);
            for (long linkPtr : linkPtrs) {
                Integer index = nativeGetDestPageIndex(doc.mNativeDocPtr, linkPtr);
                String uri = nativeGetLinkURI(doc.mNativeDocPtr, linkPtr);
                RectF rect = nativeGetLinkRect(linkPtr);
                if (rect != null && (index != null || uri != null)) {
                    links.add(new PdfDocument.Link(rect, index, uri));
                }
            }
            return links;
        }
    }

    /**
     * Map page coordinates to device screen coordinates
     *
     * @param doc       pdf document
     * @param pageIndex index of page
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     *                  2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates
     */
    public Point mapPageCoordsToDevice(PdfDocument doc, int pageIndex, int startX, int startY, int sizeX,
                                       int sizeY, int rotate, double pageX, double pageY) {
        Long mDoc = doc.mNativePagesPtr.get(pageIndex);
        Point point = null;
        if (mDoc != null) {
            point = nativePageCoordsToDevice(mDoc, startX, startY, sizeX, sizeY, rotate, pageX, pageY);
        }
        return point;
    }

    /**
     * @return mapped coordinates
     * @see PdfiumCore#mapPageCoordsToDevice(PdfDocument, int, int, int, int, int, int, double, double)
     */
    public RectF mapRectToDevice(PdfDocument doc, int pageIndex, int startX, int startY, int sizeX, int sizeY, int rotate, RectF coords) {
        Point leftTop = mapPageCoordsToDevice(doc, pageIndex, startX, startY, sizeX, sizeY, rotate, coords.left, coords.top);
        Point rightBottom = mapPageCoordsToDevice(doc, pageIndex, startX, startY, sizeX, sizeY, rotate, coords.right, coords.bottom);
        return new RectF(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y);
    }

    /**
     * Get page rotation in degrees
     *
     * @param pageIndex the page index
     * @return page rotation
     */
    public int getPageRotation(int pageIndex) {
        return nativeGetPageRotation(pageIndex);
    }
}