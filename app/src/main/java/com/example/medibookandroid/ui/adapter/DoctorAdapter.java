package com.example.medibookandroid.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.data.model.Doctor; // ⭐️ SỬA IMPORT MODEL
import com.example.medibookandroid.databinding.ItemPatientDoctorCardBinding;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors; // ⭐️ SỬA: Bỏ 'final'
    private final OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    // ⭐️ BẮT ĐẦU HÀM MỚI ⭐️
    /**
     * Cập nhật danh sách bác sĩ và báo cho RecyclerView vẽ lại
     */
    public void updateData(List<Doctor> newDoctors) {
        this.doctors = newDoctors;
        notifyDataSetChanged();
    }
    // ⭐️ KẾT THÚC HÀM MỚI ⭐️

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientDoctorCardBinding binding = ItemPatientDoctorCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DoctorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor, listener);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientDoctorCardBinding binding;

        public DoctorViewHolder(ItemPatientDoctorCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Doctor doctor, final OnDoctorClickListener listener) {
            // ⭐️ SỬA: Dùng getFullName()
            binding.tvDoctorName.setText("Bs. " + doctor.getFullName());
            binding.tvDoctorSpecialty.setText("Chuyên khoa " + doctor.getSpecialty());

            // (Thêm Glide để tải ảnh nếu có)
            // Glide.with(itemView.getContext()).load(doctor.getAvatarUrl())...

            itemView.setOnClickListener(v -> listener.onDoctorClick(doctor));
        }
    }
}