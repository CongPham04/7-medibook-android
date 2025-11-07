package com.example.medibookandroid.ui.adapter;

import android.content.Context;
import android.os.Build; // ⭐️ THÊM
import android.text.Html; // ⭐️ THÊM
import android.util.Log;
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
import java.util.Date;
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

        // ⭐️ SỬA: Xóa 2 dòng SimpleDateFormat 'static' khỏi đây

        public AppointmentViewHolder(ItemPatientAppointmentCardBinding binding, PatientAppointmentsViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
            this.context = itemView.getContext();
        }

        public void bind(final Appointment appointment, final OnAppointmentCancelListener listener, LifecycleOwner owner) {

            // ⭐️ BẮT ĐẦU SỬA: Logic in đậm ⭐️

            // --- 1. Tải thông tin bác sĩ (với nhãn in đậm) ---
            String nameLabel = "<b>Bs.</b> ";
            String loadingText = "Đang tải...";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvDoctorName.setText(Html.fromHtml(nameLabel + loadingText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvDoctorName.setText(Html.fromHtml(nameLabel + loadingText));
            }

            viewModel.getDoctorById(appointment.getDoctorId()).observe(owner, doctor -> {
                String nameValue;
                if (doctor != null) {
                    nameValue = doctor.getFullName();
                    if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                        Glide.with(context)
                                .load(doctor.getAvatarUrl())
                                .placeholder(R.drawable.logo2)
                                .circleCrop()
                                .into(binding.ivDoctorAvatar);
                    } else {
                        binding.ivDoctorAvatar.setImageResource(R.drawable.logo2);
                    }
                } else {
                    nameValue = "Không rõ bác sĩ";
                    binding.ivDoctorAvatar.setImageResource(R.drawable.logo2);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.tvDoctorName.setText(Html.fromHtml(nameLabel + nameValue, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    binding.tvDoctorName.setText(Html.fromHtml(nameLabel + nameValue));
                }
            });

            // --- 2. Gán dữ liệu lịch hẹn (với nhãn in đậm) ---
            String displayDate = appointment.getDate(); // Mặc định là chuỗi gốc

            if (appointment.getDate() != null && !appointment.getDate().isEmpty()) {
                try {
                    // ⭐️ SỬA: Khởi tạo SimpleDateFormat bên trong hàm
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date);
                    }
                } catch (Exception e) {
                    Log.e("PatientApptAdapter", "Lỗi định dạng ngày: " + e.getMessage());
                }
            }

            String timeLabel = "<b>Thời gian:</b> ";
            String dateTimeValue = appointment.getTime() + ", " + displayDate;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvAppointmentDatetime.setText(Html.fromHtml(timeLabel + dateTimeValue, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvAppointmentDatetime.setText(Html.fromHtml(timeLabel + dateTimeValue));
            }
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