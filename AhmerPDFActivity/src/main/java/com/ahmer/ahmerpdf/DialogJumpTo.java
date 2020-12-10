package com.ahmer.ahmerpdf;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.ahmer.afzal.utils.utilcode.ToastUtils;
import com.ahmer.ahmerpdf.databinding.DialogPdfJumptoBinding;

import java.util.Objects;

public class DialogJumpTo extends Dialog {

    private final Context context;
    private final JumpListener listener;

    public DialogJumpTo(@NonNull Context context, JumpListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogPdfJumptoBinding binding = DialogPdfJumptoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.inputPageNumber.postDelayed(() -> {
            binding.inputPageNumber.requestFocus();
            binding.inputPageNumber.setCursorVisible(true);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.showSoftInput(binding.inputPageNumber, InputMethodManager.SHOW_IMPLICIT);
        }, 150);
        binding.tvGoTo.setOnClickListener(v -> {
            String pageNumber = Objects.requireNonNull(binding.inputPageNumber.getText()).toString();
            if (pageNumber.matches("")) {
                ToastUtils.showShort(context.getString(R.string.please_enter_number));
            } else {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromInputMethod(binding.inputPageNumber.getWindowToken(), 0);
                dismiss();
                int number = Integer.parseInt(pageNumber);
                listener.onJump(number);
            }
        });
        binding.tvCancel.setOnClickListener(v -> dismiss());
    }

    public interface JumpListener {
        void onJump(int numPage);
    }
}
