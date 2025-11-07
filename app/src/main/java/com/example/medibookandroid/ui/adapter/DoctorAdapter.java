package com.example.medibookandroid.ui.adapter;

import android.content.Context; // ⭐️ THÊM
import android.os.Build; // ⭐️ THÊM
import android.text.Html; // ⭐️ THÊM
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // ⭐️ THÊM
import com.example.medibookandroid.R; // ⭐️ THÊM
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.databinding.ItemPatientDoctorCardBinding;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors;
    private final OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách bác sĩ và báo cho RecyclerView vẽ lại
     */
    public void updateData(List<Doctor> newDoctors) {
        this.doctors = newDoctors;
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
        Doctor doctor = doctors.get(position);
        holder.bind(doctor, listener);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientDoctorCardBinding binding;
        private final Context context; // ⭐️ THÊM: Cần Context cho Glide

        public DoctorViewHolder(ItemPatientDoctorCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = itemView.getContext(); // ⭐️ THÊM
        }

        public void bind(final Doctor doctor, final OnDoctorClickListener listener) {

            // ⭐️ BẮT ĐẦU SỬA: Logic in đậm ⭐️

            // --- 1. Tên Bác sĩ ---
            String nameLabel = "<b>Bs.</b> "; // In đậm
            String nameValue = (doctor.getFullName() != null) ? doctor.getFullName() : "Không rõ";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvDoctorName.setText(Html.fromHtml(nameLabel + nameValue, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvDoctorName.setText(Html.fromHtml(nameLabel + nameValue));
            }

            // --- 2. Chuyên khoa ---
            String specialtyLabel = "<b>Chuyên khoa:</b> "; // In đậm
            String specialtyValue = (doctor.getSpecialty() != null && !doctor.getSpecialty().isEmpty())
                    ? doctor.getSpecialty()
                    : "Chưa cập nhật";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvDoctorSpecialty.setText(Html.fromHtml(specialtyLabel + specialtyValue, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvDoctorSpecialty.setText(Html.fromHtml(specialtyLabel + specialtyValue));
            }

            // ⭐️ KẾT THÚC SỬA ⭐️

            // --- 3. Tải Avatar ---
            if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(doctor.getAvatarUrl())
                        .placeholder(R.drawable.logo2) // Ảnh mặc định
                        .circleCrop() // Bo tròn
                        .into(binding.ivDoctorAvatar); // Giả sử layout của bạn có ID này
            } else {
                // (Tùy chọn: Set ảnh mặc định nếu URL rỗng)
                binding.ivDoctorAvatar.setImageResource(R.drawable.logo2);
            }

            // --- 4. Click Listener ---
            itemView.setOnClickListener(v -> listener.onDoctorClick(doctor));
        }
    }
}