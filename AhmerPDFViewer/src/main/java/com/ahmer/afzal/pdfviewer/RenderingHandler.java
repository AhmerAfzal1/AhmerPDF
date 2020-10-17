package com.ahmer.afzal.pdfviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ahmer.afzal.pdfviewer.exception.PageRenderingException;
import com.ahmer.afzal.pdfviewer.model.PagePart;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Handler} that will process incoming {@link RenderingTask} messages
 * and alert {@link PDFView#onBitmapRendered(PagePart)} when the portion of the
 * PDF is ready to render.
 */
class RenderingHandler extends Handler {
    /**
     * {@link Message#what} kind of message this handler processes.
     */
    static final int MSG_RENDER_TASK = 1;
    private static final String TAG = RenderingHandler.class.getName();
    private final PDFView pdfView;
    private final RectF renderBounds = new RectF();
    private final Rect roundedRenderBounds = new Rect();
    private final Matrix renderMatrix = new Matrix();
    private boolean running = false;

    RenderingHandler(Looper looper, PDFView pdfView) {
        super(looper);
        this.pdfView = pdfView;
    }

    private static Bitmap toNightMode(@NotNull Bitmap bmpOriginal, boolean bestQuality) {
        int width;
        int height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap nightModeBitmap = Bitmap.createBitmap(width, height,
                bestQuality ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas c = new Canvas(nightModeBitmap);
        Paint paint = new Paint();
        ColorMatrix grayScaleMatrix = new ColorMatrix();
        grayScaleMatrix.setSaturation(0);
        ColorMatrix invertMatrix =
                new ColorMatrix(new float[]{
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, 1, 0});
        ColorMatrix nightModeMatrix = new ColorMatrix();
        nightModeMatrix.postConcat(grayScaleMatrix);
        nightModeMatrix.postConcat(invertMatrix);
        paint.setColorFilter(new ColorMatrixColorFilter(nightModeMatrix));
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return nightModeBitmap;
    }

    void addRenderingTask(int page, float width, float height, RectF bounds, boolean thumbnail,
                          int cacheOrder, boolean bestQuality, boolean annotationRendering) {
        RenderingTask task = new RenderingTask(width, height, bounds, page, thumbnail, cacheOrder,
                bestQuality, annotationRendering);
        Message msg = obtainMessage(MSG_RENDER_TASK, task);
        sendMessage(msg);
    }

    @Override
    public void handleMessage(@NotNull Message message) {
        RenderingTask task = (RenderingTask) message.obj;
        try {
            PagePart part = proceed(task);
            if (part != null) {
                if (running) {
                    pdfView.post(new Runnable() {
                        @Override
                        public void run() {
                            pdfView.onBitmapRendered(part);
                        }
                    });
                } else {
                    part.getRenderedBitmap().recycle();
                }
            }
        } catch (PageRenderingException ex) {
            pdfView.post(new Runnable() {
                @Override
                public void run() {
                    pdfView.onPageError(ex);
                }
            });
        }
    }

    private @Nullable PagePart proceed(@NotNull RenderingTask renderingTask) throws PageRenderingException {
        PdfFile pdfFile = pdfView.pdfFile;
        pdfFile.openPage(renderingTask.page);
        int w = Math.round(renderingTask.width);
        int h = Math.round(renderingTask.height);
        if (w == 0 || h == 0 || pdfFile.pageHasError(renderingTask.page)) {
            return null;
        }
        Bitmap render;
        try {
            render = Bitmap.createBitmap(w, h, renderingTask.bestQuality ?
                    Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Cannot create bitmap", e);
            return null;
        }
        calculateBounds(w, h, renderingTask.bounds);
        pdfFile.renderPageBitmap(render, renderingTask.page,
                roundedRenderBounds, renderingTask.annotationRendering);
        if (pdfView.isNightMode()) {
            render = toNightMode(render, renderingTask.bestQuality);
        }
        return new PagePart(renderingTask.page, render, renderingTask.bounds,
                renderingTask.thumbnail, renderingTask.cacheOrder);
    }

    private void calculateBounds(int width, int height, @NotNull RectF pageSliceBounds) {
        renderMatrix.reset();
        renderMatrix.postTranslate(-pageSliceBounds.left * width, -pageSliceBounds.top * height);
        renderMatrix.postScale(1 / pageSliceBounds.width(), 1 / pageSliceBounds.height());
        renderBounds.set(0, 0, width, height);
        renderMatrix.mapRect(renderBounds);
        renderBounds.round(roundedRenderBounds);
    }

    void stop() {
        running = false;
    }

    void start() {
        running = true;
    }

    private static class RenderingTask {

        final float width;
        final float height;
        final RectF bounds;
        final int page;
        final boolean thumbnail;
        final int cacheOrder;
        final boolean bestQuality;
        final boolean annotationRendering;

        RenderingTask(float width, float height, RectF bounds, int page, boolean thumbnail,
                      int cacheOrder, boolean bestQuality, boolean annotationRendering) {
            this.page = page;
            this.width = width;
            this.height = height;
            this.bounds = bounds;
            this.thumbnail = thumbnail;
            this.cacheOrder = cacheOrder;
            this.bestQuality = bestQuality;
            this.annotationRendering = annotationRendering;
        }
    }
}
