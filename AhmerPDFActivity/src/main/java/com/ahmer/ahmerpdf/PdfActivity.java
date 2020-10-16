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

import com.ahmer.afzal.pdfium.Bookmark;
import com.ahmer.afzal.pdfium.PdfPasswordException;
import com.ahmer.afzal.pdfviewer.link.DefaultLinkHandler;
import com.ahmer.afzal.pdfviewer.listener.OnLoadCompleteListener;
import com.ahmer.afzal.pdfviewer.listener.OnPageChangeListener;
import com.ahmer.afzal.pdfviewer.scroll.DefaultScrollHandle;
import com.ahmer.afzal.pdfviewer.util.FitPolicy;
import com.ahmer.afzal.pdfviewer.util.PdfFileUtils;
import com.ahmer.afzal.utils.utilcode.SPUtils;
import com.ahmer.afzal.utils.utilcode.StringUtils;
import com.ahmer.afzal.utils.utilcode.ThrowableUtils;
import com.ahmer.afzal.utils.utilcode.ToastUtils;
import com.ahmer.ahmerpdf.databinding.ActivityPdfBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PdfActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    private static boolean isHorizontal = false;
    private String password = null;
    private boolean isNightMode = false;
    private String pdfFile = null;
    private int totalPages = 0;
    private SPUtils prefPage = null;
    private SPUtils prefSwab = null;
    private ActivityPdfBinding binding;

    private static void printBookmarksTree(List<Bookmark> tree, String sep) {
        for (Bookmark bookmark : tree) {
            Log.v(MainActivity.TAG, String.format(Locale.getDefault(), "%s %s, Page %d", sep,
                    bookmark.getTitle(), bookmark.getPageIdx()));
            if (bookmark.hasChildren()) {
                printBookmarksTree(bookmark.getChildren(), sep + "-");
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefPage = SPUtils.getInstance("page");
        prefSwab = SPUtils.getInstance("swab");
        binding.toolbar.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        });
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menuPdfInfo) {
                try {
                    DialogMoreInfo dialogMoreInfo = new DialogMoreInfo(PdfActivity.this,
                            binding.pdfView, PdfFileUtils.fileFromAsset(PdfActivity.this, pdfFile));
                    Window dialogMoreInfoWindow = dialogMoreInfo.getWindow();
                    Objects.requireNonNull(dialogMoreInfoWindow).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogMoreInfo.show();
                    dialogMoreInfoWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (item.getItemId() == R.id.menuPdfJumpTo) {
                DialogJumpTo dialogJumpTo = new DialogJumpTo(PdfActivity.this, numPage -> {
                    if (numPage > totalPages) {
                        ToastUtils.showShort(getString(R.string.no_page));
                    } else {
                        binding.pdfView.jumpTo(numPage - 1, true);
                    }
                });
                Window window = dialogJumpTo.getWindow();
                Objects.requireNonNull(window).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogJumpTo.show();
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            } else if (item.getItemId() == R.id.menuPdfSwitchView) {
                if (!isHorizontal) {
                    isHorizontal = true;
                    prefSwab.put("rememberSwipe", true);
                    binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setIcon(R.drawable.ic_menu_pdf_vertical);
                } else {
                    isHorizontal = false;
                    prefSwab.put("rememberSwipe", false);
                    binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setIcon(R.drawable.ic_menu_pdf_horizontal);
                }
                displayFromAsset();
            } else if (item.getItemId() == R.id.menuPdfNightMode) {

                if (!isNightMode) {
                    isNightMode = true;
                    binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).setIcon(R.drawable.ic_menu_pdf_sun);
                } else {
                    isNightMode = false;
                    binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).setIcon(R.drawable.ic_menu_pdf_moon);
                }
                displayFromAsset();
            } else if (item.getItemId() == R.id.menuPdfSearch) {
                /*
                if (binding.layoutSearch.getVisibility() != View.VISIBLE) {
                    binding.layoutSearch.setVisibility(View.VISIBLE);
                } else {
                    binding.etSearch.setText("");
                    binding.layoutSearch.setVisibility(View.GONE);
                }
                */
                ToastUtils.showLong(getString(R.string.under_progress));
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
                password = "5632";
            } else if (getIntent().hasExtra("pdfProtected")) {
                pdfFile = "grammar.pdf";
                password = null;
            }
            displayFromAsset();
        } catch (Exception e) {
            ThrowableUtils.getFullStackTrace(e);
            Log.v(MainActivity.TAG, "Calling Intent or getIntent won't work in " + getClass() + " Activity!");
        }
    }

    private void displayFromAsset() {
        binding.toolbar.getMenu().findItem(R.id.menuPdfInfo).setEnabled(false);
        binding.toolbar.getMenu().findItem(R.id.menuPdfJumpTo).setEnabled(false);
        binding.toolbar.getMenu().findItem(R.id.menuPdfSwitchView).setEnabled(false);
        binding.toolbar.getMenu().findItem(R.id.menuPdfNightMode).setEnabled(false);
        binding.pdfView.setBackgroundColor(Color.LTGRAY);
        binding.pdfView.fromAsset(pdfFile)
                .defaultPage(prefPage.getInt(pdfFile))
                .onLoad(this)
                .onPageChange(this)
                .onPageScroll((page, positionOffset) -> Log.v(MainActivity.TAG, "onPageScrolled: Page "
                        + page + " PositionOffset: " + positionOffset))
                .onError(t -> {
                    if (t instanceof PdfPasswordException) {
                        showPasswordDialog();
                    } else {
                        ToastUtils.showLong(getResources().getString(R.string.error_loading_pdf));
                        t.printStackTrace();
                        Log.v(MainActivity.TAG, " onError: " + t);
                    }
                })
                .onPageError((page, t) -> {
                    t.printStackTrace();
                    ToastUtils.showLong("onPageError");
                    Log.v(MainActivity.TAG, "onPageError: " + t + " on page: " + page);
                })
                .onRender(nbPages -> binding.pdfView.fitToWidth(prefPage.getInt(pdfFile)))
                .onTap(e -> true)
                .fitEachPage(true)
                .nightMode(isNightMode)
                .enableSwipe(true)
                .swipeHorizontal(prefSwab.getBoolean("rememberSwipe"))
                .pageSnap(true) // snap pages to screen boundaries
                .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
                .pageFling(false) // make a fling change only a single page like ViewPager
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .password(password)
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
        Dialog dialog = new Dialog(this);
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
                    password = inputPass.getText().toString();
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
        prefPage.put(pdfFile, page);
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