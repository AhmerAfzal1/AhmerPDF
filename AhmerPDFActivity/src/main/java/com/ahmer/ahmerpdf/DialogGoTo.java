package com.ahmer.ahmerpdf;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.ahmer.afzal.utils.utilcode.ToastUtils;
import com.ahmer.ahmerpdf.databinding.DialogPdfGotoBinding;


public class DialogGoTo extends Dialog {

    private final Context context;
    private final JumpListener listener;

    public DialogGoTo(@NonNull Context context, JumpListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogPdfGotoBinding binding = DialogPdfGotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvCancel.setOnClickListener(view -> dismiss());
        binding.tvGoTo.setOnClickListener(view -> {
            String numPage = binding.etPageNumber.getText().toString();
            if (numPage.matches("")) {
                ToastUtils.showShort(context.getString(R.string.please_enter_number));
            } else {
                dismiss();
                int num = Integer.parseInt(numPage);
                listener.onJump(num);
            }
        });
    }

    public interface JumpListener {
        void onJump(int numPage);
    }
}
