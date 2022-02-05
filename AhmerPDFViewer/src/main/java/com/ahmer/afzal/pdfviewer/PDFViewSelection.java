package com.ahmer.afzal.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ahmer.afzal.pdfviewer.model.SearchRecord;
import com.ahmer.afzal.pdfviewer.model.SearchRecordItem;
import com.ahmer.afzal.pdfviewer.util.PdfUtils;

import java.util.ArrayList;

/**
 * A View to paint PDF selections, [magnifier] and search highlights
 */
public class PDFViewSelection extends View {
    private final PointF vCursorPos = new PointF();
    private final RectF tmpPosRct = new RectF();
    public boolean supressRecalcInval;
    PDFView pdfView;
    float drawableWidth = 60;
    float drawableHeight = 30;
    float drawableDeltaW = drawableWidth / 4;
    Paint rectPaint;
    Paint rectFramePaint;
    Paint rectHighlightPaint;
    /**
     * Small Canvas for magnifier.
     * {@link Canvas#clipPath ClipPath} fails if the canvas it too high. ( will never happen in this project. )
     * see <a href="https://issuetracker.google.com/issues/132402784">issuetracker</a>)
     */
    Canvas cc;
    Bitmap PageCache;
    BitmapDrawable PageCacheDrawable;
    Path magClipper;
    RectF magClipperR;
    float magFactor = 1.5f;
    int magW = 560;
    int magH = 280;
    /**
     * output image
     */
    Drawable frameDrawable;
    int rectPoolSize = 0;

    ArrayList<ArrayList<RectF>> rectPool = new ArrayList<>();
    ArrayList<RectF> magSelBucket = new ArrayList<>();
    private float framew;

    public PDFViewSelection(Context context) {
        super(context);
        init();
    }


    public PDFViewSelection(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PDFViewSelection(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rectPaint = new Paint();
        rectPaint.setColor(0x66109afe);
        rectHighlightPaint = new Paint();
        rectHighlightPaint.setColor(getResources().getColor(R.color.colorHighlight));
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        rectHighlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        rectFramePaint = new Paint();
        rectFramePaint.setColor(0xccc7ab21);
        rectFramePaint.setStyle(Paint.Style.STROKE);
        rectFramePaint.setStrokeWidth(0.5f);
    }

    private void initMagnifier() {
        //setLayerType(LAYER_TYPE_NONE,null);
        cc = new Canvas(PageCache = Bitmap.createBitmap(magW, magH, Bitmap.Config.ARGB_8888));
        PageCacheDrawable = new BitmapDrawable(getResources(), PageCache);
        frameDrawable = getResources().getDrawable(R.drawable.frame);
        framew = getResources().getDimension(R.dimen.frame);
        magClipper = new Path();
        magClipperR = new RectF(PageCacheDrawable.getBounds());
        magClipper.reset();
        magClipperR.set(0, 0, magW, magH);
        magClipper.addRoundRect(magClipperR, framew + 5, framew + 5, Path.Direction.CW);
    }

    public void resetSel() {
        //Log.v(PdfConstants.TAG, "resetSel"+ pDocView.selPageSt +","+ pDocView.selPageEd+","+ pDocView.selStart+","+ pDocView.selEnd);

        if (pdfView != null && pdfView.pdfFile != null && pdfView.hasSelection) {
            long tid = pdfView.dragPinchManager.loadText();
            if (pdfView.isNotCurrentPage(tid)) {
                return;
            }

            boolean b1 = pdfView.selPageEd < pdfView.selPageSt;
            if (b1) {
                pdfView.selPageEd = pdfView.selPageSt;
                pdfView.selPageSt = pdfView.selPageEd;
            } else {
                pdfView.selPageEd = pdfView.selPageEd;
                pdfView.selPageSt = pdfView.selPageSt;
            }
            if (b1 || pdfView.selPageEd == pdfView.selPageSt && pdfView.selEnd < pdfView.selStart) {
                pdfView.selStart = pdfView.selEnd;
                pdfView.selEnd = pdfView.selStart;
            } else {
                pdfView.selStart = pdfView.selStart;
                pdfView.selEnd = pdfView.selEnd;
            }
            int pageCount = pdfView.selPageEd - pdfView.selPageSt;
            int sz = rectPool.size();
            ArrayList<RectF> rectPagePool;
            for (int i = 0; i <= pageCount; i++) {
                if (i >= sz) {
                    rectPool.add(rectPagePool = new ArrayList<>());
                } else {
                    rectPagePool = rectPool.get(i);
                }
                int selSt = i == 0 ? pdfView.selStart : 0;
                int selEd = i == pageCount ? pdfView.selEnd : -1;
                // PDocument.PDocPage page = pDocView.pdfFile.mPDocPages[selPageSt + i];

                pdfView.dragPinchManager.getSelRects(rectPagePool, selSt, selEd);//+10
            }
            recalcHandles();
            rectPoolSize = pageCount + 1;
        } else {
            rectPoolSize = 0;
        }
        if (!supressRecalcInval) {
            invalidate();
        }
    }

    public void recalcHandles() {
        PDFView page = pdfView;
        long tid = page.dragPinchManager.prepareText();
        if (pdfView.isNotCurrentPage(tid)) {
            return;
        }
        float mappedX = -pdfView.getCurrentXOffset() + pdfView.dragPinchManager.lastX;
        float mappedY = -pdfView.getCurrentYOffset() + pdfView.dragPinchManager.lastY;
        int pageIndex = pdfView.pdfFile.getPageAtOffset(pdfView.isSwipeVertical() ? mappedY : mappedX, pdfView.getZoom());

        int st = pdfView.selStart;
        int ed = pdfView.selEnd;
        int dir = pdfView.selPageEd - pdfView.selPageSt;
        dir = (int) Math.signum(dir == 0 ? ed - st : dir);
        if (dir != 0) {
            String atext = page.dragPinchManager.allText;
            int len = atext.length();
            if (st >= 0 && st < len) {
                char c;
                while (((c = atext.charAt(st)) == '\r' || c == '\n') && st + dir >= 0 && st + dir < len) {
                    st += dir;
                }
            }
            page.getCharPos(pdfView.handleLeftPos, st);
            pdfView.lineHeightLeft = pdfView.handleLeftPos.height() / 2;
            page.getCharLoosePos(pdfView.handleLeftPos, st);

            page = pdfView;
            page.dragPinchManager.prepareText();
            atext = page.dragPinchManager.allText;
            len = atext.length();
            int delta = -1;
            if (ed >= 0 && ed < len) {
                char c;
                dir *= -1;
                while (((c = atext.charAt(ed)) == '\r' || c == '\n') && ed + dir >= 0 && ed + dir < len) {
                    delta = 0;
                    ed += dir;
                }
            }
            //Log.v(PdfConstants.TAG, "getCharPos" + page.allText.substring(ed+delta, ed+delta+1));
            page.getCharPos(pdfView.handleRightPos, ed + delta);
            pdfView.lineHeightRight = pdfView.handleRightPos.height() / 2;
            page.getCharLoosePos(pdfView.handleRightPos, ed + delta);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (pdfView == null) {
            return;
        }
        RectF VR = tmpPosRct;
        Matrix matrix = pdfView.matrix;
        if (pdfView.isSearching) {
            // SearchRecord record =  pDocView.searchRecords.get(pDocView.getCurrentPage());
            ArrayList<SearchRecord> searchRecordList = getSearchRecords();
            for (SearchRecord record : searchRecordList) {
                if (record != null) {
                    pdfView.getAllMatchOnPage(record);
                    int page = record.currentPage != -1 ? record.currentPage : pdfView.currentPage;
                    ArrayList<SearchRecordItem> data = (ArrayList<SearchRecordItem>) record.data;
                    for (int j = 0, len = data.size(); j < len; j++) {
                        RectF[] rects = data.get(j).rects;
                        if (rects != null) {
                            for (RectF rI : rects) {
                                pdfView.sourceToViewRectFFSearch(rI, VR, page);
                                matrix.reset();
                                int bmWidth = (int) rI.width();
                                int bmHeight = (int) rI.height();
                                pdfView.setMatrixArray(pdfView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
                                pdfView.setMatrixArray(pdfView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);

                                matrix.setPolyToPoly(pdfView.srcArray, 0, pdfView.dstArray, 0, 4);
                                matrix.postRotate(0, pdfView.getScreenWidth(), pdfView.getScreenHeight());

                                canvas.save();
                                canvas.concat(matrix);
                                VR.set(0, 0, bmWidth, bmHeight);
                                canvas.drawRect(VR, rectHighlightPaint);
                                canvas.restore();
                            }
                        }
                    }
                }
            }
        }

        if (pdfView.hasSelection) {
            pdfView.sourceToViewRectFF(pdfView.handleLeftPos, VR);
            float left = VR.left + drawableDeltaW;
            pdfView.handleLeft.setBounds((int) (left - drawableWidth), (int) VR.bottom, (int) left, (int) (VR.bottom + drawableHeight));
            pdfView.handleLeft.draw(canvas);
            //canvas.drawRect(pDocView.handleLeft.getBounds(), rectPaint);

            pdfView.sourceToViewRectFF(pdfView.handleRightPos, VR);
            left = VR.right - drawableDeltaW;
            pdfView.handleRight.setBounds((int) left, (int) VR.bottom, (int) (left + drawableWidth), (int) (VR.bottom + drawableHeight));
            pdfView.handleRight.draw(canvas);

            // canvas.drawRect(pDocView.handleRight.getBounds(), rectPaint);
            pdfView.sourceToViewCoord(pdfView.sCursorPos, vCursorPos);

            for (int i = 0; i < rectPoolSize; i++) {

                ArrayList<RectF> rectPage = rectPool.get(i);
                for (RectF rI : rectPage) {
                    pdfView.sourceToViewRectFF(rI, VR);
                    matrix.reset();
                    int bmWidth = (int) rI.width();
                    int bmHeight = (int) rI.height();
                    pdfView.setMatrixArray(pdfView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
                    pdfView.setMatrixArray(pdfView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);

                    matrix.setPolyToPoly(pdfView.srcArray, 0, pdfView.dstArray, 0, 4);
                    matrix.postRotate(0, pdfView.getScreenWidth(), pdfView.getScreenHeight());

                    canvas.save();
                    canvas.concat(matrix);
                    VR.set(0, 0, bmWidth, bmHeight);
                    canvas.drawRect(VR, rectPaint);
                    canvas.restore();


                }
            }

        }
    }

    /**
     * To draw search result after and before current page
     **/
    private ArrayList<SearchRecord> getSearchRecords() {
        ArrayList<SearchRecord> list = new ArrayList<>();
        int currentPage = pdfView.getCurrentPage();
        if (PdfUtils.indexExists(pdfView.getPageCount(), currentPage - 1)) {
            int index = currentPage - 1;
            if (pdfView.searchRecords.containsKey(index)) {
                SearchRecord searchRecordPrev = pdfView.searchRecords.get(index);
                if (searchRecordPrev != null)
                    searchRecordPrev.currentPage = index;
                list.add(searchRecordPrev);
            }
        }
        list.add(pdfView.searchRecords.get(currentPage));
        if (PdfUtils.indexExists(pdfView.getPageCount(), currentPage + 1)) {
            int indexNext = currentPage + 1;
            if (pdfView.searchRecords.containsKey(indexNext)) {
                SearchRecord searchRecordNext = pdfView.searchRecords.get(indexNext);
                if (searchRecordNext != null)
                    searchRecordNext.currentPage = indexNext;
                list.add(pdfView.searchRecords.get(indexNext));
            }
        }
        return list;
    }


}
