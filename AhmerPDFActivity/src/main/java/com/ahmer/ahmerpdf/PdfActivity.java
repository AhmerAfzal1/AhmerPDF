package com.ahmer.ahmerpdf;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfviewer.link.DefaultLinkHandler;
import com.ahmer.afzal.pdfviewer.listener.OnLoadCompleteListener;
import com.ahmer.afzal.pdfviewer.listener.OnPageChangeListener;
import com.ahmer.afzal.pdfviewer.scroll.DefaultScrollHandle;
import com.ahmer.afzal.pdfviewer.util.FitPolicy;
import com.ahmer.afzal.utils.SharedPreferencesUtil;
import com.ahmer.afzal.utils.utilcode.StringUtils;
import com.ahmer.afzal.utils.utilcode.ThrowableUtils;
import com.ahmer.afzal.utils.utilcode.ToastUtils;
import com.ahmer.ahmerpdf.databinding.ActivityPdfBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PdfActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    private static boolean isHorizontal = false;
    private final String[] password = new String[1];
    private boolean isNightMode = false;
    private String pdfFile = null;
    private int totalPages = 0;
    private SharedPreferencesUtil prefPage = null;
    private SharedPreferencesUtil prefSwab = null;
    private ActivityPdfBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefPage = new SharedPreferencesUtil(getApplicationContext(), "page");
        prefSwab = new SharedPreferencesUtil(getApplicationContext(), "swab");
        binding.toolbar.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        });
        binding.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuPdfInfo:
                    try {
                        DialogInfo dialogInfo = new DialogInfo(PdfActivity.this, getMyAssets(), password[0]);
                        dialogInfo.showPDFInfo();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.menuPdfJumpTo:
                    DialogGoTo dialogGoTo = new DialogGoTo(PdfActivity.this, numPage -> {
                        if (numPage > totalPages) {
                            ToastUtils.showShort(getString(R.string.no_page));
                        } else {
                            binding.pdfView.jumpTo(numPage - 1, true);
                        }
                    });
                    Window window = dialogGoTo.getWindow();
                    Objects.requireNonNull(window).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogGoTo.show();
                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    break;

                case R.id.menuPdfSwitchView:
                    if (!isHorizontal) {
                        isHorizontal = true;
                        prefSwab.saveSharedPreferences("rememberSwipe", true);
                        binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setIcon(R.drawable.ic_menu_pdf_vertical);
                    } else {
                        isHorizontal = false;
                        prefSwab.saveSharedPreferences("rememberSwipe", false);
                        binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setIcon(R.drawable.ic_menu_pdf_horizontal);
                    }
                    displayFromAsset();
                    break;

                case R.id.menuPdfNightMode:
                    if (!isNightMode) {
                        isNightMode = true;
                        binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).setIcon(R.drawable.ic_menu_pdf_sun);
                    } else {
                        isNightMode = false;
                        binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).setIcon(R.drawable.ic_menu_pdf_moon);
                    }
                    displayFromAsset();
                    break;

                case R.id.menuPdfSearch:
                    /*if (binding.layoutSearch.getVisibility() != View.VISIBLE) {
                        binding.layoutSearch.setVisibility(View.VISIBLE);
                    } else {
                        binding.etSearch.setText("");
                        binding.layoutSearch.setVisibility(View.GONE);
                    }*/
                    ToastUtils.showLong(getString(R.string.under_progress));
                    break;

                default:
                    break;
            }
            return false;
        });
        binding.ivCancelSearch.setOnClickListener(v -> binding.etSearch.setText(""));
        if (!isHorizontal) {
            binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setIcon(R.drawable.ic_menu_pdf_horizontal);
        } else {
            binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setIcon(R.drawable.ic_menu_pdf_vertical);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.toolbar.getMenu().findItem(R.id.menuPdfInfo).getIcon().setTint(Color.WHITE);
            binding.toolbar.getMenu().findItem(R.id.menuPdfJumpTo).getIcon().setTint(Color.WHITE);
            binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).getIcon().setTint(Color.WHITE);
            binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).getIcon().setTint(Color.WHITE);
        }
        init();
    }

    private void init() {
        try {
            if (getIntent().hasExtra("pdfNormal")) {
                pdfFile = "grammar.pdf";
                password[0] = "5632";
            } else if (getIntent().hasExtra("pdfProtected")) {
                pdfFile = "grammar.pdf";
                password[0] = null;
            }
            displayFromAsset();
        } catch (Exception e) {
            ThrowableUtils.getFullStackTrace(e);
            Log.v(MainActivity.TAG, "Calling Intent or getIntent won't work in " + getClass() + " Activity!");
        }
    }

    private File getMyAssets() throws IOException {
        InputStream in = getAssets().open(pdfFile);
        File outFile = new File(getCacheDir(), pdfFile);
        OutputStream out = new FileOutputStream(outFile);
        try {
            int len;
            byte[] buff = new byte[1024];
            while ((len = in.read(buff)) > 0) {
                out.write(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }
        return outFile;
    }

    private void displayFromAsset() {
        binding.toolbar.getMenu().findItem(R.id.menuPdfInfo).setEnabled(false);
        binding.toolbar.getMenu().findItem(R.id.menuPdfJumpTo).setEnabled(false);
        binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setEnabled(false);
        binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).setEnabled(false);
        binding.pdfView.setBackgroundColor(Color.GRAY);
        binding.pdfView.fromAsset(pdfFile)
                .defaultPage(prefPage.loadIntSharedPreference(pdfFile))
                .onLoad(this)
                .onPageChange(this)
                .onPageScroll((page, positionOffset) -> Log.v(MainActivity.TAG, "onPageScrolled: Page "
                        + page + " PositionOffset: " + positionOffset))
                .onError(t -> {
                    if (Objects.requireNonNull(t.getMessage()).contains("Password required or incorrect password.")) {
                        showPasswordDialog();
                    } else {
                        ToastUtils.showLong(getResources().getString(R.string.error_loading_pdf));
                    }
                    t.printStackTrace();
                    Log.v(MainActivity.TAG, " onError while Loading: " + t);
                })
                .onPageError((page, t) -> {
                    t.printStackTrace();
                    ToastUtils.showLong("onPageError");
                    Log.v(MainActivity.TAG, "onPageError while Loading: " + t + "\nonPageError cannot load page: " + page);
                })
                .onRender(nbPages -> binding.pdfView.fitToWidth(prefPage.loadIntSharedPreference(pdfFile)))
                .onTap(e -> true)
                .fitEachPage(true)
                .nightMode(isNightMode)
                .enableSwipe(true)
                .swipeHorizontal(prefSwab.loadBooleanSharedPreference("rememberSwipe"))
                .pageSnap(true) // snap pages to screen boundaries
                .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
                .pageFling(false) // make a fling change only a single page like ViewPager
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .password(password[0])
                .scrollHandle(new DefaultScrollHandle(getApplicationContext()))
                .enableAntialiasing(true)
                .spacing(0)
                .linkHandler(new DefaultLinkHandler(binding.pdfView))
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        binding.pdfView.useBestQuality(true);
        binding.pdfView.setMinZoom(1.0f);
        binding.pdfView.setMidZoom(2.5f);
        binding.pdfView.setMaxZoom(4.0f);
    }

    private void showPasswordDialog() {
        final Dialog dialog = new Dialog(this);
        try {
            Objects.requireNonNull(dialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setContentView(R.layout.dialog_pdf_password);
            dialog.getWindow().setLayout(-1, -2);
            dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            EditText inputPass = dialog.findViewById(R.id.inputPassword);
            inputPass.requestFocus();
            inputPass.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.showSoftInput(inputPass, InputMethodManager.SHOW_IMPLICIT);
            }, 100);
            TextView open = dialog.findViewById(R.id.tvOpen);
            open.setClickable(false);
            open.setOnClickListener(v -> {
                if (inputPass.getText().toString().equals("")) {
                    ToastUtils.showShort(v.getContext().getString(R.string.password_not_empty));
                } else if (StringUtils.isSpace(inputPass.getText().toString())) {
                    ToastUtils.showShort(v.getContext().getString(R.string.password_not_space));
                } else {
                    password[0] = inputPass.getText().toString();
                    displayFromAsset();
                    dialog.dismiss();
                }
            });
            TextView cancel = dialog.findViewById(R.id.tvCancel);
            cancel.setOnClickListener(v -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                dialog.dismiss();
                imm.hideSoftInputFromInputMethod(inputPass.getWindowToken(), 0);
                dialog.dismiss();
                PdfActivity.super.onBackPressed();
            });
            inputPass.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s)) {
                        open.setClickable(false);
                    } else {
                        open.setClickable(true);
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        prefPage.saveSharedPreferences(pdfFile, page);
        binding.toolbar.setTitle(String.format(Locale.getDefault(), "%s %s of %s", "Page", page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        printBookmarksTree(binding.pdfView.getTableOfContents(), "-");
        binding.progressBarPdfView.setVisibility(View.GONE);
        totalPages = nbPages;
        binding.toolbar.getMenu().findItem(R.id.menuPdfInfo).setEnabled(true);
        binding.toolbar.getMenu().findItem(R.id.menuPdfJumpTo).setEnabled(true);
        binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setEnabled(true);
        binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).setEnabled(true);
    }

    private void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark bookmark : tree) {
            Log.v(MainActivity.TAG, String.format(Locale.getDefault(), "%s %s, Page %d", sep,
                    bookmark.getTitle(), bookmark.getPageIdx()));
            if (bookmark.hasChildren()) {
                printBookmarksTree(bookmark.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.etSearch.setText("");
        binding.layoutSearch.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}