package com.ahmer.ahmerpdf;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfPasswordException;
import com.ahmer.afzal.pdfviewer.PDFView;
import com.ahmer.afzal.pdfviewer.link.DefaultLinkHandler;
import com.ahmer.afzal.pdfviewer.listener.OnLoadCompleteListener;
import com.ahmer.afzal.pdfviewer.listener.OnPageChangeListener;
import com.ahmer.afzal.pdfviewer.scroll.DefaultScrollHandle;
import com.ahmer.afzal.pdfviewer.util.Constants;
import com.ahmer.afzal.pdfviewer.util.FitPolicy;
import com.ahmer.afzal.utils.SharedPreferencesUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

public class PdfActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, View.OnClickListener {
    private static final String TAG = PdfActivity.class.getSimpleName();
    private static final String SAMPLE_FILE = "grammar.pdf";
    private static boolean isHorizontal = false;
    private int totalPages = 0;
    private ProgressBar mProgressBar;
    private PDFView pdfView;
    private SharedPreferencesUtil pref;
    private boolean isNightMode = false;
    private ImageView nightModeIV;
    private TextView tvFileName;
    private EditText search;
    private RelativeLayout layoutSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        pref = new SharedPreferencesUtil(this);
        mProgressBar = findViewById(R.id.pdfProgressBar);
        pdfView = findViewById(R.id.pdfView);
        nightModeIV = findViewById(R.id.nightModeIV);
        tvFileName = findViewById(R.id.tv_file_name);
        layoutSearch = findViewById(R.id.layout_search);
        search = findViewById(R.id.search);
        ImageView info = findViewById(R.id.info_detail);
        ImageView cancelSearch = findViewById(R.id.im_cancel_search);
        ImageView goBack = findViewById(R.id.im_back);
        ImageView switchHorizontal = findViewById(R.id.im_switch_view);
        ImageView jumpTo = findViewById(R.id.im_jump);
        ImageView imSearch = findViewById(R.id.im_search);
        imSearch.setOnClickListener(this);
        jumpTo.setOnClickListener(this);
        switchHorizontal.setOnClickListener(this);
        goBack.setOnClickListener(this);
        cancelSearch.setOnClickListener(this);
        info.setOnClickListener(this);
        nightModeIV.setOnClickListener(this);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //pdfView.startSearch(s.toString());
            }
        });
    }

    private void displayFromAsset(boolean flag) {
        pdfView.setBackgroundColor(Color.GRAY);
        boolean isLandscape = false;
        int orientation = this.getResources().getConfiguration().orientation;
        isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;
        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pref.loadIntSharedPreference(SAMPLE_FILE))
                .onLoad(this)
                .onPageChange(this)
                .landscapeOrientation(isLandscape)
                .dualPageMode(true)
                .onPageScroll((page, positionOffset) -> Log.d(TAG, "onPageScrolled: Page " + page + " PositionOffset " + positionOffset))
                .onError(t -> {
                    if (t instanceof PdfPasswordException) {
                        Toast.makeText(PdfActivity.this, R.string.error_loading_pdf, Toast.LENGTH_LONG).show();
                    }
                    t.printStackTrace();
                    Log.d(TAG, " onError while Loading. " + t);
                })
                .onPageError((page, t) -> {
                    t.printStackTrace();
                    Log.v(TAG, "onPageError while Loading. " + t);
                    Log.v(TAG, "onPageError cannot load page " + page);
                })
                .onRender(nbPages -> pdfView.fitToWidth(pref.loadIntSharedPreference(SAMPLE_FILE)))
                .onTap(e -> true)
                .fitEachPage(true)
                .nightMode(isNightMode)
                .enableSwipe(true)
                .swipeHorizontal(flag)
                .pageSnap(true) // snap pages to screen boundaries
                .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
                .pageFling(true) // make a fling change only a single page like ViewPager
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
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
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
        pref.saveSharedPreferences(SAMPLE_FILE, page);
        tvFileName.setText(String.format("%s %s of %s", "Page", page + 1, pageCount));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        search.setText("");
        layoutSearch.setVisibility(View.GONE);
    }

    @Override
    public void loadComplete(int nbPages, float pageWidth, float pageHeight) {
        printBookmarksTree(pdfView.getTableOfContents(), "-");
        totalPages = nbPages;
        mProgressBar.setVisibility(View.GONE);
        /*
        pdfView.startSearch("Lahore", true, true);
        Log.v(TAG, "Search Word: " + pdfView.getSearchLength());
        */
    }

    private void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {
            Log.d(TAG, String.format("%s %s, Page %d", sep, b.getTitle(), b.getPageIdx()));
            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.im_search:
                if (layoutSearch.getVisibility() != View.VISIBLE) {
                    layoutSearch.setVisibility(View.VISIBLE);
                } else {
                    search.setText("");
                    layoutSearch.setVisibility(View.GONE);
                }
                break;

            case R.id.im_cancel_search:
                search.setText("");
                //pdfView.stopSearch();
                break;

            case R.id.im_back:
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                break;

            case R.id.im_jump:
                GoToDialog goToDialog = new GoToDialog(PdfActivity.this, numPage -> {
                    if (numPage > totalPages) {
                        Toast.makeText(PdfActivity.this, getString(R.string.no_page_like), Toast.LENGTH_SHORT).show();
                    } else {
                        pdfView.jumpTo(numPage - 1, true);
                    }
                });
                Objects.requireNonNull(goToDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                goToDialog.show();
                break;

            case R.id.im_switch_view:
                isHorizontal = !isHorizontal;
                displayFromAsset(isHorizontal);
                break;

            case R.id.nightModeIV:
                if (!isNightMode) {
                    isNightMode = true;
                    nightModeIV.setImageResource(R.drawable.ic_day_mode);
                } else {
                    isNightMode = false;
                    nightModeIV.setImageResource(R.drawable.ic_night_mode);
                }
                displayFromAsset(isHorizontal);
                break;
            case R.id.info_detail:
                double size = (double) SAMPLE_FILE.length() / (1024 * 1024);
                NumberFormat numberFormat = new DecimalFormat("#0.00");
                String sizeText = numberFormat.format(size);
                PdfDocument.Meta meta = pdfView.getDocumentMeta();
                AlertDialog.Builder builderAlert = new AlertDialog.Builder(this);
                builderAlert.setTitle(R.string.attention_info);
                builderAlert.setIcon(R.drawable.ic_information);
                builderAlert.setMessage("\nTitle: " + meta.getTitle() + "\n\nAuthor: " + meta.getAuthor() +
                        "\n\nTotal Pages: " + totalPages + "\n\nSubject: " + meta.getSubject() + "\n\nKeywords: " + meta.getKeywords() +
                        "\n\nCreation Date: " + meta.getCreationDate() + "\n\nModify Date: " + meta.getModDate() + "\n\nCreator: " + meta.getCreator() +
                        "\n\nProducer: " + meta.getProducer() + "\n\nFile Size: " + sizeText + " MB");
                builderAlert.setCancelable(true);
                builderAlert.setPositiveButton(getString(android.R.string.ok), null);
                AlertDialog alertDialog = builderAlert.create();
                alertDialog.show();
                TextView messageView = alertDialog.findViewById(android.R.id.message);
                Objects.requireNonNull(messageView).setTextColor(Color.BLACK);
                messageView.setTextSize(16);
                // messageView.setTypeface(typeface);
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayFromAsset(isHorizontal);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pdfView.recycle();
        pdfView = null;
    }

    /*
    @Override
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
    }
    */

    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}