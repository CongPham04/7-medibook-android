package com.example.medibookandroid.ui.adapter;

import android.content.Context; // ⭐️ THÊM
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner; // ⭐️ THÊM
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // ⭐️ THÊM
import com.example.medibookandroid.R; // ⭐️ THÊM
import com.example.medibookandroid.databinding.ItemPatientAppointmentCardBinding;
import com.example.medibookandroid.data.model.Appointment; // ⭐️ SỬA
import com.example.medibookandroid.data.model.Doctor; // ⭐️ SỬA
import com.example.medibookandroid.ui.patient.viewmodel.PatientAppointmentsViewModel; // ⭐️ THÊM

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private final OnAppointmentCancelListener listener;
    private final PatientAppointmentsViewModel viewModel; // ⭐️ THÊM
    private final LifecycleOwner lifecycleOwner; // ⭐️ THÊM

    public interface OnAppointmentCancelListener {
        void onCancelClick(Appointment appointment);
    }

    // ⭐️ SỬA: Constructor nhận ViewModel và LifecycleOwner
    public PatientAppointmentAdapter(List<Appointment> appointments,
                                     PatientAppointmentsViewModel viewModel,
                                     OnAppointmentCancelListener listener,
                                     LifecycleOwner lifecycleOwner) {
        this.appointments = appointments;
        this.viewModel = viewModel;
        this.listener = listener;
        this.lifecycleOwner = lifecycleOwner;
    }

    // ⭐️ THÊM: Hàm cập nhật dữ liệu
    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientAppointmentCardBinding binding = ItemPatientAppointmentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        // ⭐️ SỬA: Truyền ViewModel vào ViewHolder
        return new AppointmentViewHolder(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        // ⭐️ SỬA: Truyền cả LifecycleOwner
        holder.bind(appointment, listener, lifecycleOwner);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientAppointmentCardBinding binding;
        private final PatientAppointmentsViewModel viewModel; // ⭐️ THÊM
        private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
        private final Context context; // ⭐️ THÊM

        public AppointmentViewHolder(ItemPatientAppointmentCardBinding binding, PatientAppointmentsViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel; // ⭐️ THÊM
            this.context = itemView.getContext(); // ⭐️ THÊM
        }

        // ⭐️ SỬA: Hàm bind
        public void bind(final Appointment appointment, final OnAppointmentCancelListener listener, LifecycleOwner owner) {
            // 1. Tải thông tin bác sĩ bất đồng bộ
            binding.tvDoctorName.setText("Đang tải...");

            viewModel.getDoctorById(appointment.getDoctorId()).observe(owner, doctor -> {
                if (doctor != null) {
                    binding.tvDoctorName.setText(doctor.getFullName());
                    if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                        Glide.with(context)
                                .load(doctor.getAvatarUrl())
                                .placeholder(R.drawable.logo2)
                                .circleCrop()
                                .into(binding.ivDoctorAvatar);
                    }
                } else {
                    binding.tvDoctorName.setText("Không rõ bác sĩ");
                }
            });

            // 2. Gán dữ liệu lịch hẹn
            String dateTime = appointment.getDate() + ", " + appointment.getTime();
            binding.tvAppointmentDatetime.setText(dateTime);

            // 3. Xử lý logic nút Hủy
            // 3. ⭐️ SỬA LOGIC HIỂN THỊ NÚT ⭐️
            // Chỉ hiển thị nút "Hủy" khi ở "Pending"
            if ("pending".equalsIgnoreCase(appointment.getStatus())) {
                binding.btnCancelOrChange.setVisibility(View.VISIBLE);
                binding.btnCancelOrChange.setText("Hủy lịch");
                binding.btnCancelOrChange.setOnClickListener(v -> listener.onCancelClick(appointment));
            } else {
                // Ẩn nút Hủy cho "Confirmed", "Completed", "Canceled"
                binding.btnCancelOrChange.setVisibility(View.GONE);
            }
            // ⭐️ KẾT THÚC SỬA ⭐️
        }
    }
}
