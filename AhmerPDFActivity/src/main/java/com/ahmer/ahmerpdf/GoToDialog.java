package com.ahmer.ahmerpdf;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


public class GoToDialog extends Dialog {

    private final Context context;
    private final JumpListener listener;
    private EditText editText;

    public GoToDialog(@NonNull Context context, JumpListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_jump_page);
        TextView btnJump, btnCancel;
        btnJump = findViewById(R.id.btn_jump);
        btnCancel = findViewById(R.id.btn_cancel);
        editText = findViewById(R.id.edt_page_number);

        btnCancel.setOnClickListener(view -> dismiss());

        btnJump.setOnClickListener(view -> {
            String numPage = editText.getText().toString();
            if (numPage.matches("")) {
                Toast.makeText(context, context.getString(R.string.please_enter_number), Toast.LENGTH_SHORT).show();
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
