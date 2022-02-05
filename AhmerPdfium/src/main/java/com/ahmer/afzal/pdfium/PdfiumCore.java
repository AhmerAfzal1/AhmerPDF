package com.ahmer.afzal.pdfium;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Surface;

import androidx.collection.ArrayMap;
import androidx.core.util.ObjectsCompat;

import com.ahmer.afzal.pdfium.util.Size;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PdfiumCore {

    /* synchronize native methods */
    private static final String TAG = PdfiumCore.class.getName();
    private static int mCurrentDpi = 0;
    private static ParcelFileDescriptor parcelFileDescriptor = null;
    public static final Map<Integer, Long> mNativePagesPtr = new ArrayMap<>();
    public static final Map<Integer, Long> mNativeTextPagesPtr = new ArrayMap<>();
    public static final Object lock = new Object();
    public static final Object searchLock = new Object();
    public static long mNativeDocPtr = 0L;

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

    private native Point nativePageCoordinateToDevice(long pagePtr, int startX, int startY, int sizeX, int sizeY, int rotate, double pageX, double pageY);

    private native PointF nativeDeviceCoordinateToPage(long pagePtr, int startX, int startY, int sizeX, int sizeY, int rotate, int deviceX, int deviceY);

    private native RectF nativeGetLinkRect(long linkPtr);

    private native Size nativeGetPageSizeByIndex(long docPtr, int pageIndex, int dpi);

    private native String nativeGetBookmarkTitle(long bookmarkPtr);

    private native String nativeGetDocumentMetaText(long docPtr, String tag);

    private native String nativeGetLinkURI(long docPtr, long linkPtr);

    private native void nativeCloseDocument(long docPtr);

    private native void nativeClosePage(long pagePtr);

    private native void nativeClosePages(long[] pagesPtr);

    private native void nativeRenderPage(long pagePtr, Surface surface, int startX, int startY, int drawSizeHor, int drawSizeVer, boolean renderAnnot);

    private native void nativeRenderPageBitmap(long docPtr, long pagePtr, Bitmap bitmap, int dpi, int startX, int startY, int drawSizeHor, int drawSizeVer, boolean renderAnnot);

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

    public static native long nativeGetStringChars(String key, boolean isCopy);

    /**
     * Create new document from file
     */
    public void newDocument(ParcelFileDescriptor fd) throws IOException {
        newDocument(fd, null);
    }

    /**
     * Create new document from file with password
     */
    public void newDocument(ParcelFileDescriptor fd, String password) throws IOException {
        parcelFileDescriptor = fd;
        synchronized (lock) {
            mNativeDocPtr = nativeOpenDocument(fd.getFd(), password);
        }
    }

    /**
     * Create new document from byteArray
     */
    public void newDocument(byte[] data) throws IOException {
        newDocument(data, null);
    }

    /**
     * Create new document from byteArray with password
     */
    public void newDocument(byte[] data, String password) throws IOException {
        synchronized (lock) {
            mNativeDocPtr = nativeOpenMemDocument(data, password);
        }
    }

    /**
     * Get total number of pages in document
     */
    public int getPageCount() {
        synchronized (lock) {
            return nativeGetPageCount(mNativeDocPtr);
        }
    }

    /**
     * Open page
     */
    public long openPage(int pageIndex) {
        synchronized (lock) {
            Long pagePtr = mNativePagesPtr.get(pageIndex);
            if (pagePtr == null) {
                pagePtr = nativeLoadPage(mNativeDocPtr, pageIndex);
                mNativePagesPtr.put(pageIndex, pagePtr);
            }
            return pagePtr;
        }
    }

    /**
     * Open range of pages
     */
    public long[] openPage(int fromIndex, int toIndex) {
        long[] pagesPtr;
        synchronized (lock) {
            pagesPtr = nativeLoadPages(mNativeDocPtr, fromIndex, toIndex);
            int pageIndex = fromIndex;
            for (long page : pagesPtr) {
                if (pageIndex > toIndex) {
                    break;
                }
                mNativePagesPtr.put(pageIndex, page);
                pageIndex++;
            }
            return pagesPtr;
        }
    }

    /**
     * Close page
     */
    public void closePage(int pageIndex) {
        synchronized (lock) {
            Long pagePtr = mNativePagesPtr.get(pageIndex);
            if (pagePtr != null) {
                nativeClosePage(pagePtr);
                mNativePagesPtr.remove(pageIndex);
            }
        }
    }

    public long openText(long pagePtr) {
        synchronized (lock) {
            return nativeLoadTextPage(pagePtr);
        }
    }

    public int getTextRects(long pagePtr, int offsetY, int offsetX, Size size, ArrayList<RectF> arr, long textPtr, int selSt, int selEd) {
        synchronized (lock) {
            return nativeCountAndGetRects(pagePtr, offsetY, offsetX, size.getWidth(), size.getHeight(), arr, textPtr, selSt, selEd);
        }
    }

    /**
     * Get page width in pixels.
     * This method requires page to be opened.
     */
    public int getPageWidth(int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = mNativePagesPtr.get(index)) != null) {
                return nativeGetPageWidthPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }

    /**
     * Get page height in pixels.
     * This method requires page to be opened.
     */
    public int getPageHeight(int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = mNativePagesPtr.get(index)) != null) {
                return nativeGetPageHeightPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).
     * This method requires page to be opened.
     */
    public int getPageWidthPoint(int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = mNativePagesPtr.get(index)) != null) {
                return nativeGetPageWidthPoint(pagePtr);
            }
            return 0;
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).
     * This method requires page to be opened.
     */
    public int getPageHeightPoint(int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = mNativePagesPtr.get(index)) != null) {
                return nativeGetPageHeightPoint(pagePtr);
            }
            return 0;
        }
    }

    /**
     * Get size of page in pixels.
     * This method does not require given page to be opened.
     */
    public Size getPageSize(int index) {
        synchronized (lock) {
            return nativeGetPageSizeByIndex(mNativeDocPtr, index, mCurrentDpi);
        }
    }

    /**
     * Render page fragment on {@link Surface}.
     * Page must be opened before rendering.
     */
    public void renderPage(Surface surface, int pageIndex, int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPage(surface, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Surface}. This method allows to render annotations.
     * Page must be opened before rendering.
     */
    public void renderPage(Surface surface, int pageIndex, int startX, int startY, int drawSizeX, int drawSizeY, boolean renderAnnot) {
        synchronized (lock) {
            try {
                nativeRenderPage(ObjectsCompat.requireNonNull(mNativePagesPtr.get(pageIndex)), surface, startX, startY, drawSizeX, drawSizeY, renderAnnot);
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
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
    public void renderPageBitmap(Bitmap bitmap, int pageIndex, int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPageBitmap(bitmap, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Bitmap}. This method allows to render annotations.
     * Page must be opened before rendering.
     * <p>
     * For more info see {PdfiumCore#renderPageBitmap(Bitmap, int, int, int, int, int, boolean)}
     */
    public void renderPageBitmap(Bitmap bitmap, int pageIndex, int startX, int startY, int drawSizeX, int drawSizeY, boolean renderAnnot) {
        synchronized (lock) {
            try {
                nativeRenderPageBitmap(ObjectsCompat.requireNonNull(mNativePagesPtr.get(pageIndex)), bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot);
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
    }

    /**
     * Release native resources and opened file
     */
    public void closeDocument() {
        synchronized (lock) {
            for (Integer index : mNativePagesPtr.keySet()) {
                nativeClosePage(ObjectsCompat.requireNonNull(mNativePagesPtr.get(index)));
            }
            mNativePagesPtr.clear();
            nativeCloseDocument(mNativeDocPtr);
            if (parcelFileDescriptor != null) { //if document was loaded from file
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                parcelFileDescriptor = null;
            }
        }
    }

    /**
     * Get metadata for given document
     */
    public Meta getDocumentMeta() {
        synchronized (lock) {
            Meta meta = new Meta();
            meta.title = nativeGetDocumentMetaText(mNativeDocPtr, "Title");
            meta.author = nativeGetDocumentMetaText(mNativeDocPtr, "Author");
            meta.subject = nativeGetDocumentMetaText(mNativeDocPtr, "Subject");
            meta.keywords = nativeGetDocumentMetaText(mNativeDocPtr, "Keywords");
            meta.creator = nativeGetDocumentMetaText(mNativeDocPtr, "Creator");
            meta.producer = nativeGetDocumentMetaText(mNativeDocPtr, "Producer");
            meta.creationDate = nativeGetDocumentMetaText(mNativeDocPtr, "CreationDate");
            meta.modDate = nativeGetDocumentMetaText(mNativeDocPtr, "ModDate");
            meta.totalPages = getPageCount();
            return meta;
        }
    }

    /**
     * Get table of contents (bookmarks) for given document
     */
    public List<Bookmark> getTableOfContents() {
        synchronized (lock) {
            List<Bookmark> topLevel = new ArrayList<>();
            Long first = nativeGetFirstChildBookmark(mNativeDocPtr, null);
            if (first != null) {
                recursiveGetBookmark(topLevel, first);
            }
            return topLevel;
        }
    }

    private void recursiveGetBookmark(@NotNull List<Bookmark> tree, long bookmarkPtr) {
        Bookmark bookmark = new Bookmark();
        bookmark.mNativePtr = bookmarkPtr;
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr);
        bookmark.pageIdx = nativeGetBookmarkDestIndex(mNativeDocPtr, bookmarkPtr);
        tree.add(bookmark);
        Long child = nativeGetFirstChildBookmark(mNativeDocPtr, bookmarkPtr);
        if (child != null) {
            recursiveGetBookmark(bookmark.getChildren(), child);
        }
        Long sibling = nativeGetSiblingBookmark(mNativeDocPtr, bookmarkPtr);
        if (sibling != null) {
            recursiveGetBookmark(tree, sibling);
        }
    }

    /**
     * Get all links from given page
     */
    public List<Link> getPageLinks(int pageIndex) {
        synchronized (lock) {
            List<Link> links = new ArrayList<>();
            Long nativePagePtr = mNativePagesPtr.get(pageIndex);
            if (nativePagePtr == null) {
                return links;
            }
            long[] linkPtrs = nativeGetPageLinks(nativePagePtr);
            for (long linkPtr : linkPtrs) {
                Integer index = nativeGetDestPageIndex(mNativeDocPtr, linkPtr);
                String uri = nativeGetLinkURI(mNativeDocPtr, linkPtr);

                RectF rect = nativeGetLinkRect(linkPtr);
                if (rect != null && (index != null || uri != null)) {
                    links.add(new Link(rect, index, uri));
                }
            }
            return links;
        }
    }

    /**
     * Map page coordinates to device screen coordinates
     *
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
    public Point mapPageCoordsToDevice(int pageIndex, int startX, int startY, int sizeX, int sizeY, int rotate, double pageX, double pageY) {
        long pagePtr = ObjectsCompat.requireNonNull(mNativePagesPtr.get(pageIndex));
        return nativePageCoordinateToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY);
    }

    /**
     * Convert the screen coordinates of a point to page coordinates.
     * <p>
     * The page coordinate system has its origin at the left-bottom corner
     * of the page, with the X-axis on the bottom going to the right, and
     * the Y-axis on the left side going up.
     * <p>
     * NOTE: this coordinate system can be altered when you zoom, scroll,
     * or rotate a page, however, a point on the page should always have
     * the same coordinate values in the page coordinate system.
     * <p>
     * The device coordinate system is device dependent. For screen device,
     * its origin is at the left-top corner of the window. However this
     * origin can be altered by the Windows coordinate transformation
     * utilities.
     * <p>
     * You must make sure the start_x, start_y, size_x, size_y
     * and rotate parameters have exactly same values as you used in
     * the FPDF_RenderPage() function call.
     *
     * @param pageIndex index of page
     * @param startX    Left pixel position of the display area in device coordinates.
     * @param startY    Top pixel position of the display area in device coordinates.
     * @param sizeX     Horizontal size (in pixels) for displaying the page.
     * @param sizeY     Vertical size (in pixels) for displaying the page.
     * @param rotate    Page orientation:
     *                  0 (normal)
     *                  1 (rotated 90 degrees clockwise)
     *                  2 (rotated 180 degrees)
     *                  3 (rotated 90 degrees counter-clockwise)
     * @param deviceX   X value in device coordinates to be converted.
     * @param deviceY   Y value in device coordinates to be converted.
     * @return mapped coordinates
     */
    public PointF mapDeviceCoordsToPage(int pageIndex, int startX, int startY, int sizeX, int sizeY, int rotate, int deviceX, int deviceY) {
        long pagePtr = ObjectsCompat.requireNonNull(mNativePagesPtr.get(pageIndex));
        return nativeDeviceCoordinateToPage(pagePtr, startX, startY, sizeX, sizeY, rotate, deviceX, deviceY);
    }

    /**
     * @return mapped coordinates
     * @see PdfiumCore#mapPageCoordsToDevice(int, int, int, int, int, int, double, double)
     */
    public RectF mapRectToDevice(int pageIndex, int startX, int startY, int sizeX, int sizeY, int rotate, @NotNull RectF coords) {
        Point leftTop = mapPageCoordsToDevice(pageIndex, startX, startY, sizeX, sizeY, rotate, coords.left, coords.top);
        Point rightBottom = mapPageCoordsToDevice(pageIndex, startX, startY, sizeX, sizeY, rotate, coords.right, coords.bottom);
        return new RectF(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y);
    }

    /**
     * @return mapped coordinates
     * @see PdfiumCore#mapDeviceCoordsToPage(int, int, int, int, int, int, int, int)
     */
    public RectF mapRectToPage(int pageIndex, int startX, int startY, int sizeX, int sizeY, int rotate, @NotNull RectF coords) {
        PointF leftTop = mapDeviceCoordsToPage(pageIndex, startX, startY, sizeX, sizeY, rotate, (int) coords.left, (int) coords.top);
        PointF rightBottom = mapDeviceCoordsToPage(pageIndex, startX, startY, sizeX, sizeY, rotate, (int) coords.right, (int) coords.bottom);
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

    public static int getCurrentDpi() {
        return mCurrentDpi;
    }

    public static void setCurrentDpi(int currentDpi) {
        mCurrentDpi = currentDpi;
    }

    public static boolean hasPage(int index) {
        return mNativePagesPtr.containsKey(index);
    }

    public static boolean hasTextPage(int index) {
        return mNativeTextPagesPtr.containsKey(index);
    }
}