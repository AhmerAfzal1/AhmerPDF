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

import com.ahmer.afzal.pdfium.util.Size;

import org.jetbrains.annotations.NotNull;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PdfiumCore {

    public static final Object searchLock = new Object();
    private static final Class FD_CLASS = FileDescriptor.class;
    /* synchronize native methods */
    private static final Object lock = new Object();
    private static final String FD_FIELD_NAME = "descriptor";
    private static final String TAG = PdfiumCore.class.getName();
    private static final Map<Integer, Long> mNativePagesPtr = new ArrayMap<>();
    private static final Map<Integer, Long> mNativeTextPagesPtr = new ArrayMap<>();
    private static Field mFdField = null;
    private static long mNativeDocPtr = 0L;
    private static ParcelFileDescriptor parcelFileDescriptor = null;
    private static int mCurrentDpi = 0;

    static {
        System.loadLibrary("pdfium");
        System.loadLibrary("jniPdfium");
    }

    /**
     * Context needed to get screen density
     */
    public PdfiumCore(@NotNull Context context) {
        mCurrentDpi = context.getResources().getDisplayMetrics().densityDpi;
        Log.d(TAG, "Starting AhmerPdfium...");
    }

    public static int getNumFd(ParcelFileDescriptor fileDescriptor) {
        try {
            if (mFdField == null) {
                mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME);
                mFdField.setAccessible(true);
            }
            return mFdField.getInt(fileDescriptor.getFileDescriptor());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static boolean validPtr(Long ptr) {
        return ptr != null && ptr != -1;
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

    private static native int nativeGetPageRotation(long docPtr, int pageIndex);

    private native long nativeOpenDocument(int fd, String password);

    private native long nativeOpenMemDocument(byte[] data, String password);

    private native void nativeCloseDocument(long docPtr);

    private native int nativeGetPageCount(long docPtr);

    private native long nativeLoadPage(long docPtr, int pageIndex);

    //private native long nativeGetNativeWindow(Surface surface);

    //private native void nativeRenderPage(long pagePtr, long nativeWindowPtr);

    private native long[] nativeLoadPages(long docPtr, int fromIndex, int toIndex);

    private native void nativeClosePage(long pagePtr);

    private native void nativeClosePages(long[] pagesPtr);

    private native int nativeGetPageWidthPixel(long pagePtr, int dpi);

    private native int nativeGetPageHeightPixel(long pagePtr, int dpi);

    private native int nativeGetPageWidthPoint(long pagePtr);

    private native int nativeGetPageHeightPoint(long pagePtr);

    private native void nativeRenderPage(long pagePtr, Surface surface, int dpi, int startX, int startY, int drawSizeHor, int drawSizeVer, boolean renderAnnot);

    private native void nativeRenderPageBitmap(long pagePtr, Bitmap bitmap, int dpi, int startX, int startY, int drawSizeHor, int drawSizeVer, boolean renderAnnot);

    private native String nativeGetDocumentMetaText(long docPtr, String tag);

    private native Long nativeGetFirstChildBookmark(long docPtr, Long bookmarkPtr);

    private native Long nativeGetSiblingBookmark(long docPtr, long bookmarkPtr);

    private native String nativeGetBookmarkTitle(long bookmarkPtr);

    private native long nativeGetBookmarkDestIndex(long docPtr, long bookmarkPtr);

    private native long nativeLoadTextPage(long docPtr, long pagePtr);

    private native void nativeCloseTextPage(long pagePtr);

    private native int nativeTextCountChars(long textPagePtr);

    private native int nativeTextGetText(long textPagePtr, int start_index, int count, short[] result);

    private native int nativeTextGetUnicode(long textPagePtr, int index);

    private native double[] nativeTextGetCharBox(long textPagePtr, int index);

    private native int nativeTextGetCharIndexAtPos(long textPagePtr, double x, double y, double xTolerance, double yTolerance);

    private native int nativeTextCountRects(long textPagePtr, int start_index, int count);

    private native double[] nativeTextGetRect(long textPagePtr, int rect_index);

    private native int nativeTextGetBoundedText(long textPagePtr, double left, double top, double right, double bottom, short[] arr);

    private native Size nativeGetPageSizeByIndex(long docPtr, int pageIndex, int dpi);

    private native long[] nativeGetPageLinks(long pagePtr);

    private native Integer nativeGetDestPageIndex(long docPtr, long linkPtr);

    private native String nativeGetLinkURI(long docPtr, long linkPtr);

    private native RectF nativeGetLinkRect(long linkPtr);

    private native Point nativePageCoordsToDevice(long pagePtr, int startX, int startY, int sizeX, int sizeY, int rotate, double pageX, double pageY);

    private native PointF nativeDeviceCoordsToPage(long pagePtr, int startX, int startY, int sizeX, int sizeY, int rotate, int deviceX, int deviceY);

    private native long nativeLoadTextPage(long docPtr, int pageIndex);

    private native long[] nativeLoadTextPages(long docPtr, int fromIndex, int toIndex);

    private native int nativeTextGetBoundedTextLength(long textPagePtr, double left, double top, double right, double bottom);

    /*
     * New add
     */
    public native int nativeGetCharIndexAtCoord(long pagePtr, double width, double height, long textPtr, double posX, double posY, double tolX, double tolY);

    private native int nativeCountAndGetRects(long pagePtr, int offsetY, int offsetX, int width, int height, ArrayList<RectF> arr, long tid, int selSt, int selEd);

    public native String nativeGetText(long textPtr);

    public native int nativeGetCharPos(long pagePtr, int offsetY, int offsetX, int width, int height, RectF pt, long tid, int index, boolean loose);

    public native int nativeGetMixedLooseCharPos(long pagePtr, int offsetY, int offsetX, int width, int height, RectF pt, long tid, int index, boolean loose);

    public native void nativeFindAll(long mNativeDocPtr, int pages, String key, int flag, ArrayList<SearchRecord> arr);

    public native SearchRecord nativeFindPage(long mNativeDocPtr, String key, int pageIdx, int flag);

    public native int nativeFindTextPage(long pagePtr, String key, int flag);

    public native RectF nativeGetAnnotRect(long pagePtr, int index, int width, int height);

    public native int nativeCountAnnot(long pagePtr);

    public native long nativeOpenAnnot(long page, int idx);

    public native void nativeCloseAnnot(long annotPtr);

    public native int nativeCountAttachmentPoints(long annotPtr);

    public native long nativeCreateAnnot(long pagePtr, int type);

    public native void nativeSetAnnotRect(long pagePtr, long annotPtr, float left, float top, float right, float bottom, double width, double height);

    public native void nativeAppendAnnotPoints(long pagePtr, long annotPtr, double left, double top, double right, double bottom, double width, double height);

    public native void nativeSetAnnotColor(long annotPtr, int R, int G, int B, int A);

    public native boolean nativeGetAttachmentPoints(long pagePtr, long annotPtr, int idx, int width, int height, PointF p1, PointF p2, PointF p3, PointF p4);

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
            mNativeDocPtr = nativeOpenDocument(getNumFd(fd), password);
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
            if (mNativePagesPtr == null) {
                return -1;
            }
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
                //nativeRenderPage(mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
                nativeRenderPage(mNativePagesPtr.get(pageIndex), surface, mCurrentDpi, startX, startY, drawSizeX, drawSizeY, renderAnnot);
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
                nativeRenderPageBitmap(mNativePagesPtr.get(pageIndex), bitmap, mCurrentDpi, startX, startY, drawSizeX, drawSizeY, renderAnnot);
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
            if (mNativePagesPtr != null) {
                for (Integer index : mNativePagesPtr.keySet()) {
                    nativeClosePage(mNativePagesPtr.get(index));
                }
                mNativePagesPtr.clear();
            }
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
        long pagePtr = mNativePagesPtr.get(pageIndex);
        return nativePageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY);
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
        long pagePtr = mNativePagesPtr.get(pageIndex);
        return nativeDeviceCoordsToPage(pagePtr, startX, startY, sizeX, sizeY, rotate, deviceX, deviceY);
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

    public long openTextPage(int pageIndex) {
        synchronized (lock) {
            long page = openPage(pageIndex);
            Long textPagePtr = mNativeTextPagesPtr.get(pageIndex);
            if (textPagePtr == null) {
                textPagePtr = nativeLoadTextPage(mNativeDocPtr, page);
                mNativeTextPagesPtr.put(pageIndex, textPagePtr);
            }
            return textPagePtr;
        }

    }

    public void closeTextPage(int pageIndex) {
        synchronized (lock) {
            Long nativeLoadTextPage = mNativeTextPagesPtr.get(pageIndex);
            if (nativeLoadTextPage != null) {
                nativeCloseTextPage(nativeLoadTextPage);
                mNativeTextPagesPtr.remove(pageIndex);
            }
        }
    }

    public long[] openTextPage(int fromIndex, int toIndex) {
        long[] textPagesPtr;
        synchronized (lock) {
            textPagesPtr = nativeLoadPages(mNativeDocPtr, fromIndex, toIndex);
            int pageIndex = fromIndex;
            for (long page : textPagesPtr) {
                if (pageIndex > toIndex) {
                    break;
                }
                mNativeTextPagesPtr.put(pageIndex, page);
                pageIndex++;
            }
            return textPagesPtr;
        }
    }

    public int textPageCountChars(int textPageIndex) {
        synchronized (lock) {
            try {
                return nativeTextCountChars(mNativeTextPagesPtr.get(textPageIndex));
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
        return -1;
    }

    public String textPageGetText(int textPageIndex, int startIndex, int length) {
        synchronized (lock) {
            try {
                short[] buf = new short[length + 1];
                int r = nativeTextGetText(mNativeTextPagesPtr.get(textPageIndex), startIndex, length, buf);
                byte[] bytes = new byte[(r - 1) * 2];
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < r - 1; i++) {
                    short s = buf[i];
                    bb.putShort(s);
                }
                return new String(bytes, StandardCharsets.UTF_16LE);
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
            return null;
        }
    }

    public char textPageGetUnicode(int textPageIndex, int index) {
        synchronized (lock) {
            try {
                return (char) nativeTextGetUnicode(mNativeTextPagesPtr.get(textPageIndex), index);
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
        return 0;
    }

    public RectF textPageGetCharBox(int textPageIndex, int index) {
        synchronized (lock) {
            try {
                double[] o = nativeTextGetCharBox(mNativeTextPagesPtr.get(textPageIndex), index);
                RectF r = new RectF();
                r.left = (float) o[0];
                r.right = (float) o[1];
                r.bottom = (float) o[2];
                r.top = (float) o[3];
                return r;
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
        return null;
    }

    public int textPageGetCharIndexAtPos(int textPageIndex, double x, double y, double xTolerance, double yTolerance) {
        synchronized (lock) {
            try {
                return nativeTextGetCharIndexAtPos(mNativeTextPagesPtr.get(textPageIndex), x, y, xTolerance, yTolerance);
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int textPageCountRects(int textPageIndex, int start_index, int count) {
        synchronized (lock) {
            try {
                return nativeTextCountRects(mNativeTextPagesPtr.get(textPageIndex), start_index, count);
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
        return -1;
    }

    public RectF textPageGetRect(int textPageIndex, int rect_index) {
        synchronized (lock) {
            try {
                double[] o = nativeTextGetRect(mNativeTextPagesPtr.get(textPageIndex), rect_index);
                RectF r = new RectF();
                r.left = (float) o[0];
                r.top = (float) o[1];
                r.right = (float) o[2];
                r.bottom = (float) o[3];
                return r;
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
        return null;
    }

    public String textPageGetBoundedText(int textPageIndex, RectF rect, int length) {
        synchronized (lock) {
            try {
                short[] buf = new short[length + 1];
                int r = nativeTextGetBoundedText(mNativeTextPagesPtr.get(textPageIndex), rect.left, rect.top, rect.right, rect.bottom, buf);
                byte[] bytes = new byte[(r - 1) * 2];
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < r - 1; i++) {
                    short s = buf[i];
                    bb.putShort(s);
                }
                return new String(bytes, StandardCharsets.UTF_16LE);
            } catch (NullPointerException e) {
                Log.e(TAG, "Context may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Prepare information about all characters in a page.
     * Application must call FPDFText_ClosePage to release the text page information.
     *
     * @param pageIndex index of page.
     * @return A handle to the text page information structure. NULL if something goes wrong.
     */
    public long prepareTextInfo(int pageIndex) {
        long textPagePtr;
        textPagePtr = nativeLoadTextPage(mNativeDocPtr, pageIndex);
        if (validPtr(textPagePtr)) {
            mNativeTextPagesPtr.put(pageIndex, textPagePtr);
        }
        return textPagePtr;
    }

    /**
     * Release all resources allocated for a text page information structure.
     *
     * @param pageIndex index of page.
     */
    public void releaseTextInfo(int pageIndex) {
        long textPagePtr;
        textPagePtr = mNativeTextPagesPtr.get(pageIndex);
        if (validPtr(textPagePtr)) {
            nativeCloseTextPage(textPagePtr);
        }
    }

    /**
     * Prepare information about all characters in a range of pages.
     * Application must call FPDFText_ClosePage to release the text page information.
     *
     * @param fromIndex start index of page.
     * @param toIndex   end index of page.
     * @return list of handles to the text page information structure. NULL if something goes wrong.
     */
    public long[] prepareTextInfo(int fromIndex, int toIndex) {
        long[] textPagesPtr;
        textPagesPtr = nativeLoadTextPages(mNativeDocPtr, fromIndex, toIndex);
        int pageIndex = fromIndex;
        for (long page : textPagesPtr) {
            if (pageIndex > toIndex) {
                break;
            }
            if (validPtr(page)) {
                mNativeTextPagesPtr.put(pageIndex, page);
            }
            pageIndex++;
        }

        return textPagesPtr;
    }

    /**
     * Release all resources allocated for a text page information structure.
     *
     * @param fromIndex start index of page.
     * @param toIndex   end index of page.
     */
    public void releaseTextInfo(int fromIndex, int toIndex) {
        long textPagesPtr;
        for (int i = fromIndex; i < toIndex + 1; i++) {
            textPagesPtr = mNativeTextPagesPtr.get(i);
            if (validPtr(textPagesPtr)) {
                nativeCloseTextPage(textPagesPtr);
            }
        }
    }

    private Long ensureTextPage(int pageIndex) {
        Long ptr = mNativeTextPagesPtr.get(pageIndex);
        if (!validPtr(ptr)) {
            return prepareTextInfo(pageIndex);
        }
        return ptr;
    }

    public int countCharactersOnPage(int pageIndex) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? nativeTextCountChars(ptr) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Extract unicode text string from the page.
     *
     * @param pageIndex  index of page.
     * @param startIndex Index for the start characters.
     * @param length     Number of characters to be extracted.
     * @return Number of characters written into the result buffer, including the trailing terminator.
     */
    public String extractCharacters(int pageIndex, int startIndex, int length) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }
            short[] buf = new short[length + 1];

            int r = nativeTextGetText(ptr, startIndex, length, buf);

            byte[] bytes = new byte[(r - 1) * 2];
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < r - 1; i++) {
                short s = buf[i];
                bb.putShort(s);
            }

            return new String(bytes, StandardCharsets.UTF_16LE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get page rotation in degrees
     *
     * @param pageIndex the page index
     * @return page rotation
     */
    public int getPageRotation(int pageIndex) {
        return nativeGetPageRotation(mNativeDocPtr, pageIndex);
    }

    /**
     * Get Unicode of a character in a page.
     *
     * @param pageIndex index of page.
     * @param index     Zero-based index of the character.
     * @return The Unicode of the particular character. If a character is not encoded in Unicode, the return value will be zero.
     */
    public char extractCharacter(int pageIndex, int index) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? (char) nativeTextGetUnicode(ptr, index) : (char) 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get bounding box of a particular character.
     *
     * @param pageIndex index of page.
     * @param index     Zero-based index of the character.
     * @return the character position measured in PDF "user space".
     */
    public RectF measureCharacterBox(int pageIndex, int index) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }
            double[] o = nativeTextGetCharBox(ptr, index);
            RectF r = new RectF();
            r.left = (float) o[0];
            r.right = (float) o[1];
            r.bottom = (float) o[2];
            r.top = (float) o[3];
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the index of a character at or nearby a certain position on the page
     *
     * @param pageIndex  index of page.
     * @param x          X position in PDF "user space".
     * @param y          Y position in PDF "user space".
     * @param xTolerance An x-axis tolerance value for character hit detection, in point unit.
     * @param yTolerance A y-axis tolerance value for character hit detection, in point unit.
     * @return The zero-based index of the character at, or nearby the point (x,y). If there is no character at or nearby the point, return value will be -1. If an error occurs, -3 will be returned.
     */
    public int getCharacterIndex(int pageIndex, double x, double y, double xTolerance, double yTolerance) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? nativeTextGetCharIndexAtPos(ptr, x, y, xTolerance, yTolerance) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Count number of rectangular areas occupied by a segment of texts.
     * <p>
     * This function, along with FPDFText_GetRect can be used by applications to detect the position
     * on the page for a text segment, so proper areas can be highlighted or something.
     * FPDFTEXT will automatically merge small character boxes into bigger one if those characters
     * are on the same line and use same font settings.
     *
     * @param pageIndex index of page.
     * @param charIndex Index for the start characters.
     * @param count     Number of characters.
     * @return texts areas count.
     */
    public int countTextRect(int pageIndex, int charIndex, int count) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? nativeTextCountRects(ptr, charIndex, count) : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Get a rectangular area from the result generated by FPDFText_CountRects.
     *
     * @param pageIndex index of page.
     * @param rectIndex Zero-based index for the rectangle.
     * @return the text rectangle.
     */
    public RectF getTextRect(int pageIndex, int rectIndex) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }
            double[] o = nativeTextGetRect(ptr, rectIndex);
            RectF r = new RectF();
            r.left = (float) o[0];
            r.top = (float) o[1];
            r.right = (float) o[2];
            r.bottom = (float) o[3];
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract unicode text within a rectangular boundary on the page.
     * If the buffer is too small, as much text as will fit is copied into it.
     *
     * @param pageIndex index of page.
     * @param rect      the text rectangle to extract.
     * @return If buffer is NULL or buflen is zero, return number of characters (not bytes) of text
     * present within the rectangle, excluding a terminating NUL.
     * <p>
     * Generally you should pass a buffer at least one larger than this if you want a terminating NUL,
     * which will be provided if space is available. Otherwise, return number of characters copied
     * into the buffer, including the terminating NUL  when space for it is available.
     */
    public String extractText(int pageIndex, RectF rect) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }
            int length = nativeTextGetBoundedTextLength(ptr, rect.left, rect.top, rect.right, rect.bottom);
            if (length <= 0) {
                return null;
            }
            short[] buf = new short[length + 1];
            int r = nativeTextGetBoundedText(ptr, rect.left, rect.top, rect.right, rect.bottom, buf);
            byte[] bytes = new byte[(r - 1) * 2];
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < r - 1; i++) {
                short s = buf[i];
                bb.putShort(s);
            }
            return new String(bytes, StandardCharsets.UTF_16LE);
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * New Add
     */
    public int getTextRects(long pagePtr, int offsetY, int offsetX, Size size, ArrayList<RectF> arr, long textPtr, int selSt, int selEd) {
        synchronized (lock) {
            return nativeCountAndGetRects(pagePtr, offsetY, offsetX, size.getWidth(), size.getHeight(), arr, textPtr, selSt, selEd);
        }
    }
}