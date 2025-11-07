package com.example.medibookandroid.ui.adapter;

import android.content.Context;
import android.util.Log; // ⭐️ THÊM
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.ItemPatientAppointmentCardBinding;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.ui.patient.viewmodel.PatientAppointmentsViewModel;

import java.text.SimpleDateFormat;
import java.util.Date; // ⭐️ THÊM
import java.util.List;
import java.util.Locale;

public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private final OnAppointmentCancelListener listener;
    private final PatientAppointmentsViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public interface OnAppointmentCancelListener {
        void onCancelClick(Appointment appointment);
    }

    public PatientAppointmentAdapter(List<Appointment> appointments,
                                     PatientAppointmentsViewModel viewModel,
                                     OnAppointmentCancelListener listener,
                                     LifecycleOwner lifecycleOwner) {
        this.appointments = appointments;
        this.viewModel = viewModel;
        this.listener = listener;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientAppointmentCardBinding binding = ItemPatientAppointmentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener, lifecycleOwner);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientAppointmentCardBinding binding;
        private final PatientAppointmentsViewModel viewModel;
        private final Context context;

        // ⭐️ BẮT ĐẦU SỬA: Thêm 2 định dạng (input và output) ⭐️
        // Định dạng (Format) của ngày lưu trên Firestore ("2025-11-06")
        private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // Định dạng bạn muốn hiển thị ("06/11/2025")
        private static final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // ⭐️ KẾT THÚC SỬA ⭐️

        public AppointmentViewHolder(ItemPatientAppointmentCardBinding binding, PatientAppointmentsViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
            this.context = itemView.getContext();
        }

        public void bind(final Appointment appointment, final OnAppointmentCancelListener listener, LifecycleOwner owner) {
            // 1. Tải thông tin bác sĩ (giữ nguyên)
            binding.tvDoctorName.setText("Đang tải...");

            viewModel.getDoctorById(appointment.getDoctorId()).observe(owner, doctor -> {
                if (doctor != null) {
                    binding.tvDoctorName.setText("Bs. " + doctor.getFullName());
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

            // ⭐️ BẮT ĐẦU SỬA: Logic định dạng lại ngày ⭐️
            // 2. Gán dữ liệu lịch hẹn
            String displayDate = appointment.getDate(); // Mặc định là chuỗi gốc
            try {
                // Cố gắng "parse" (phân tích) chuỗi từ "yyyy-MM-dd"
                Date date = inputFormat.parse(appointment.getDate());
                if (date != null) {
                    // "format" (định dạng) lại thành "dd/MM/yyyy"
                    displayDate = outputFormat.format(date);
                }
            } catch (Exception e) {
                // Nếu lỗi (ví dụ: date là null hoặc sai định dạng),
                // cứ dùng giá trị gốc (displayDate)
                Log.e("PatientApptAdapter", "Lỗi định dạng ngày: " + e.getMessage());
            }

            String dateTime = appointment.getTime() + ", " + displayDate; // Dùng 'displayDate' đã định dạng
            binding.tvAppointmentDatetime.setText("Thời gian: " + dateTime);
            // ⭐️ KẾT THÚC SỬA ⭐️

            // 3. Xử lý logic nút Hủy (giữ nguyên)
            if ("pending".equalsIgnoreCase(appointment.getStatus())) {
                binding.btnCancelOrChange.setVisibility(View.VISIBLE);
                binding.btnCancelOrChange.setText("Hủy lịch");
                binding.btnCancelOrChange.setOnClickListener(v -> listener.onCancelClick(appointment));
            } else {
                binding.btnCancelOrChange.setVisibility(View.GONE);
            }
        }
    }
}