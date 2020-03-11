package com.ahmer.ahmerpdf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfPasswordException;
import com.ahmer.afzal.pdfviewer.PDFView;
import com.ahmer.afzal.pdfviewer.link.DefaultLinkHandler;
import com.ahmer.afzal.pdfviewer.listener.OnErrorListener;
import com.ahmer.afzal.pdfviewer.listener.OnLoadCompleteListener;
import com.ahmer.afzal.pdfviewer.listener.OnPageChangeListener;
import com.ahmer.afzal.pdfviewer.listener.OnPageErrorListener;
import com.ahmer.afzal.pdfviewer.listener.OnPageScrollListener;
import com.ahmer.afzal.pdfviewer.listener.OnRenderListener;
import com.ahmer.afzal.pdfviewer.listener.OnTapListener;
import com.ahmer.afzal.pdfviewer.scroll.DefaultScrollHandle;
import com.ahmer.afzal.pdfviewer.util.Constants;
import com.ahmer.afzal.pdfviewer.util.FitPolicy;

import java.util.List;

public class PdfActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {
    private static final String TAG = PdfActivity.class.getSimpleName();
    private static final String sPreference = "PdfActivityRememberLastPage";
    private static final String SAMPLE_FILE = "grammar.pdf";
    private int totalPages = 0;
    private ProgressBar mProgressBar;
    private PDFView pdfView;
    private SharedPreferences sharedPreferences;
    private boolean nightMode = false;
    boolean swipeHorizontal = true;
    private FitPolicy fitPolicy = FitPolicy.BOTH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_app_bar_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        /*MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.i(TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.i(TAG, "onAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.i(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.i(TAG, "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.i(TAG, "onAdLoaded");
            }
        });*/
        mProgressBar = findViewById(R.id.pdfProgressBar);
        pdfView = findViewById(R.id.pdfView);
        displayFromAsset();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.pdf_menu_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        if (item.getItemId() == R.id.menu_info_icon) {
            ((AnimationDrawable) item.getIcon()).start();
            pickFile();
            return true;
        }

        if (item.getItemId() == R.id.menu_night_mode_icon) {
            if (!nightMode) {
                nightMode = true;
                item.setIcon(R.drawable.ic_day_mode);
                Log.d(TAG, "Night mode is: " + nightMode);
            } else {
                nightMode = false;
                item.setIcon(R.drawable.ic_night_mode);
                Log.d(TAG, "Night mode is: " + nightMode);
            }
            displayFromAsset();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayFromAsset() {
        pdfView.setBackgroundColor(Color.GRAY);
        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(LoadInt())
                .onLoad(this)
                .onPageChange(this)
                .onPageScroll(new OnPageScrollListener() {
                    @Override
                    public void onPageScrolled(int page, float positionOffset) {
                        Log.d(TAG, "onPageScrolled: Page " + page + " PositionOffset " + positionOffset);
                    }
                })
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        if (t instanceof PdfPasswordException) {
                            Toast.makeText(PdfActivity.this, R.string.error_loading_pdf, Toast.LENGTH_LONG).show();
                        }
                        t.printStackTrace();
                        Log.d(TAG, " onError while Loading. " + t);
                    }
                })
                .onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        t.printStackTrace();
                        Log.d(TAG, "onPageError while Loading. " + t);
                        Log.e(TAG, "onPageError Cannot load page " + page);
                    }
                })
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {
                        pdfView.fitToWidth(LoadInt());
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .onTap(new OnTapListener() {
                    @Override
                    public boolean onTap(MotionEvent e) {
                        return true;
                    }
                })
                .fitEachPage(true)
                .nightMode(nightMode)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .pageSnap(true) // snap pages to screen boundaries
                .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
                .pageFling(false) // make a fling change only a single page like ViewPager
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .password("5632")
                .scrollHandle(new DefaultScrollHandle(this))
                .enableAntialiasing(true)
                .spacing(0)
                .linkHandler(new DefaultLinkHandler(pdfView))
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        pdfView.useBestQuality(true);
        pdfView.setMinZoom(1.0f);
        pdfView.setMidZoom(2.5f);
        pdfView.setMaxZoom(4.0f);
        Log.d(TAG, "Constant Thumbnail Ratio: " + Constants.THUMBNAIL_RATIO);
        Log.d(TAG, "Constant Part Size: " + Constants.PART_SIZE);
        Log.d(TAG, "Constant Preload Offset: " + Constants.PRELOAD_OFFSET);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        SaveInt(page);
        setTitle(String.format("%s %s of %s", "Page", page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        printBookmarksTree(pdfView.getTableOfContents(), "-");
        totalPages = nbPages;
    }

    private void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {
            Log.d(TAG, String.format("%s %s, Page %d", sep, b.getTitle(), b.getPageIdx()));
            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    private void pickFile() {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        AlertDialog.Builder infoAlert = new AlertDialog.Builder(this);
        infoAlert.setTitle(R.string.attention_info);
        infoAlert.setIcon(R.drawable.version_os_alert);
        infoAlert.setMessage("\nTitle: " + meta.getTitle() + "\n\nAuthor: " + meta.getAuthor() + "\n\nTotal Pages: " + totalPages +
                "\n\nSubject: " + meta.getSubject() + "\n\nKeywords: " + meta.getKeywords() + "\n\nCreation Date: " + meta.getCreationDate() +
                "\n\nModify Date: " + meta.getModDate() + "\n\nCreator: " + meta.getCreator() + "\n\nProducer: " + meta.getProducer());
        infoAlert.setCancelable(true);
        infoAlert.setPositiveButton(getString(android.R.string.ok), null);
        AlertDialog infoDialog = infoAlert.create();
        infoDialog.show();
        TextView messageViewInfo = infoDialog.findViewById(android.R.id.message);
        messageViewInfo.setTextColor(Color.BLACK);
        messageViewInfo.setTextSize(16);
        //messageViewInfo.setTypeface(typeface);
    }

    /*@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                pdfView.jumpTo(pdfView.getCurrentPage() - 1, true);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                pdfView.jumpTo(pdfView.getCurrentPage() + 1, true);
                return true;
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    private void SaveInt(int value) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(sPreference, value);
        editor.apply();
    }

    private int LoadInt() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getInt(sPreference, 0);
    }
}