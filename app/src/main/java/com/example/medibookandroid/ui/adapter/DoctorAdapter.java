package com.example.medibookandroid.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.databinding.ItemPatientDoctorCardBinding;

import java.util.List;
import java.util.Locale;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctorList;
    private final OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctorList, OnDoctorClickListener listener) {
        this.doctorList = doctorList;
        this.listener = listener;
    }

    public void updateData(List<Doctor> newDoctors) {
        this.doctorList = newDoctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientDoctorCardBinding binding = ItemPatientDoctorCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DoctorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        holder.bind(doctor, listener);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientDoctorCardBinding binding;
        private final Context context;

        public DoctorViewHolder(ItemPatientDoctorCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = itemView.getContext();
        }

        public void bind(final Doctor doctor, final OnDoctorClickListener listener) {
            binding.tvDoctorName.setText("Bs. " + doctor.getFullName());
            binding.tvDoctorSpecialty.setText(doctor.getSpecialty());

            if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(doctor.getAvatarUrl())
                        .placeholder(R.drawable.logo2)
                        .circleCrop()
                        .into(binding.ivDoctorAvatar);
            } else {
                binding.ivDoctorAvatar.setImageResource(R.drawable.logo2);
            }

            // ⭐️ LOGIC MỚI: HIỂN THỊ ĐÁNH GIÁ VÀO GIAO DIỆN CŨ ⭐️

            // 1. Hiển thị điểm số (ví dụ: 4.5)
            String ratingText = String.format(Locale.US, "%.1f", doctor.getRating());
            binding.tvRatingScore.setText(ratingText);

            // 2. Hiển thị số lượng (ví dụ: (12))
            if (doctor.getReviewCount() > 0) {
                binding.tvReviewCount.setText("(" + doctor.getReviewCount() + ")");
            } else {
                binding.tvReviewCount.setText("(0)");
            }

            itemView.setOnClickListener(v -> listener.onDoctorClick(doctor));
        }
    }
}