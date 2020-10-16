package com.ahmer.afzal.pdfviewer.scroll;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.ahmer.afzal.pdfviewer.PDFView;
import com.ahmer.afzal.pdfviewer.R;
import com.ahmer.afzal.pdfviewer.util.PdfUtils;

import org.jetbrains.annotations.NotNull;

public class DefaultScrollHandle extends RelativeLayout implements ScrollHandle {

    private final static int HANDLE_LONG = 65;
    private final static int HANDLE_SHORT = 40;
    private final static int DEFAULT_TEXT_SIZE = 16;
    protected final TextView textView;
    protected final Context context;
    private final boolean inverted;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hidePageScrollerRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private float relativeHandlerMiddle = 0f;
    private PDFView pdfView;
    private float currentPos;

    public DefaultScrollHandle(Context context) {
        this(context, false);
    }

    public DefaultScrollHandle(Context context, boolean inverted) {
        super(context);
        this.context = context;
        this.inverted = inverted;
        textView = new TextView(context);
        setVisibility(View.INVISIBLE);
        setTextColor(Color.BLACK);
        setTextSize(DEFAULT_TEXT_SIZE);
    }

    @Override
    public void setupLayout(@NotNull PDFView pdfView) {
        int align;
        int width;
        int height;
        Drawable background;
        // determine handler position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical()) {
            width = HANDLE_LONG;
            height = HANDLE_SHORT;
            if (inverted) { // left
                align = RelativeLayout.ALIGN_PARENT_LEFT;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_left);
            } else { // right
                align = RelativeLayout.ALIGN_PARENT_RIGHT;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_right);
            }
        } else {
            width = HANDLE_SHORT;
            height = HANDLE_LONG;
            if (inverted) { // top
                align = RelativeLayout.ALIGN_PARENT_TOP;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_top);
            } else { // bottom
                align = RelativeLayout.ALIGN_PARENT_BOTTOM;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_bottom);
            }
        }
        setBackground(background);
        LayoutParams lp = new LayoutParams(PdfUtils.getDP(context, width), PdfUtils.getDP(context, height));
        lp.setMargins(0, 0, 0, 0);
        LayoutParams tvlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(textView, tvlp);
        lp.addRule(align);
        pdfView.addView(this, lp);
        this.pdfView = pdfView;
    }

    @Override
    public void destroyLayout() {
        pdfView.removeView(this);
    }

    @Override
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            handler.removeCallbacks(hidePageScrollerRunnable);
        }
        if (pdfView != null) {
            setPosition((pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth()) * position);
        }
    }

    private void setPosition(float pos) {
        if (Float.isInfinite(pos) || Float.isNaN(pos)) {
            return;
        }
        float pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pdfViewSize = pdfView.getHeight();
        } else {
            pdfViewSize = pdfView.getWidth();
        }
        pos -= relativeHandlerMiddle;
        if (pos < 0) {
            pos = 0;
        } else if (pos > pdfViewSize - PdfUtils.getDP(context, HANDLE_SHORT)) {
            pos = pdfViewSize - PdfUtils.getDP(context, HANDLE_SHORT);
        }
        if (pdfView.isSwipeVertical()) {
            setY(pos);
        } else {
            setX(pos);
        }
        calculateMiddle();
        invalidate();
    }

    private void calculateMiddle() {
        float pos;
        float viewSize;
        float pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pos = getY();
            viewSize = getHeight();
            pdfViewSize = pdfView.getHeight();
        } else if (pdfView.isOnDualPageMode()) {
            pos = getX();
            viewSize = getWidth() / 2f;
            pdfViewSize = pdfView.getWidth() / 2f;
        } else {
            pos = getX();
            viewSize = getWidth();
            pdfViewSize = pdfView.getWidth();
        }
        relativeHandlerMiddle = ((pos + relativeHandlerMiddle) / pdfViewSize) * viewSize;
    }

    @Override
    public void hideDelayed() {
        handler.postDelayed(hidePageScrollerRunnable, 1000);
    }

    @Override
    public void setPageNum(int pageNum) {
        String text = String.valueOf(pageNum);
        if (!textView.getText().equals(text)) {
            textView.setText(text);
        }
    }

    @Override
    public boolean shown() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(View.INVISIBLE);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    /**
     * @param size text size in dp
     */
    public void setTextSize(int size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    private boolean isPDFViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0 && !pdfView.documentFitsView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPDFViewReady()) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                pdfView.stopFling();
                handler.removeCallbacks(hidePageScrollerRunnable);
                if (pdfView.isSwipeVertical()) {
                    currentPos = event.getRawY() - getY();
                } else {
                    currentPos = event.getRawX() - getX();
                }
            case MotionEvent.ACTION_MOVE:
                if (pdfView.isSwipeVertical()) {
                    setPosition(event.getRawY() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getHeight(), false);
                } else {
                    setPosition(event.getRawX() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getWidth(), false);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                hideDelayed();
                pdfView.performPageSnap();
                return true;
        }
        return super.onTouchEvent(event);
    }
}
