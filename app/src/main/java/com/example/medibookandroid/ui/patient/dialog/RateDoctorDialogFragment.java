package com.example.medibookandroid.ui.patient.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.medibookandroid.databinding.DialogRateDoctorBinding;

public class RateDoctorDialogFragment extends DialogFragment {

    private DialogRateDoctorBinding binding;
    private String doctorId;
    private String doctorName;
    private OnRateListener listener;

    // Interface để gửi dữ liệu về Fragment cha
    public interface OnRateListener {
        void onSubmitRate(String doctorId, String doctorName, float rating, String comment);
    }

    public static RateDoctorDialogFragment newInstance(String doctorId, String doctorName) {
        RateDoctorDialogFragment fragment = new RateDoctorDialogFragment();
        Bundle args = new Bundle();
        args.putString("doctorId", doctorId);
        args.putString("doctorName", doctorName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(OnRateListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogRateDoctorBinding.inflate(inflater, container, false);

        // Làm nền trong suốt để bo tròn đẹp
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            doctorId = getArguments().getString("doctorId");
            doctorName = getArguments().getString("doctorName");
            binding.tvRateDoctorName.setText("Bác sĩ " + doctorName);
        }

        binding.btnCancelRate.setOnClickListener(v -> dismiss());

        binding.btnSubmitRate.setOnClickListener(v -> {
            float rating = binding.ratingBarInput.getRating();
            String comment = binding.etReviewComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onSubmitRate(doctorId, doctorName, rating, comment);
            }
            dismiss();
        });
    }
}