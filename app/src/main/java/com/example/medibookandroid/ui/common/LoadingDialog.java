package com.example.medibookandroid.ui.common;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class LoadingDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Sử dụng AlertDialog.Builder để tạo một dialog tùy chỉnh
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Tạo một ProgressBar
        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);

        // Đặt ProgressBar làm view cho Dialog
        builder.setView(progressBar);

        Dialog dialog = builder.create();

        // Rất quan trọng:
        // 1. Đặt nền trong suốt để chỉ thấy ProgressBar
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // 2. Không cho phép hủy bằng nút Back hoặc chạm ra ngoài
        setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}