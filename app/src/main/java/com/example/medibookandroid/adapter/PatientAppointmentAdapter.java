package com.example.medibookandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibookandroid.databinding.ItemPatientAppointmentCardBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.Doctor;
import com.example.medibookandroid.model.StorageRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter này quản lý việc hiển thị các thẻ lịch hẹn (item_patient_appointment_card.xml)
 * trong danh sách của bệnh nhân.
 */
public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointments;
    private final OnAppointmentCancelListener listener;

    /**
     * Interface để xử lý sự kiện khi người dùng nhấn nút "Hủy"
     */
    public interface OnAppointmentCancelListener {
        void onCancelClick(Appointment appointment);
    }

    public PatientAppointmentAdapter(List<Appointment> appointments, OnAppointmentCancelListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng tệp item XML (item_patient_appointment_card.xml)
        ItemPatientAppointmentCardBinding binding = ItemPatientAppointmentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    /**
     * ViewHolder chứa logic để gán (bind) dữ liệu Appointment vào View
     */
    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientAppointmentCardBinding binding;
        // Định dạng ngày giờ (ví dụ: 09:00, 30/10/2025)
        private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

        public AppointmentViewHolder(ItemPatientAppointmentCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Appointment appointment, final OnAppointmentCancelListener listener) {
            // Lấy thông tin bác sĩ từ Repository
            StorageRepository repository = StorageRepository.getInstance(itemView.getContext());
            Doctor doctor = repository.findDoctorById(appointment.getDoctorId());

            if (doctor != null) {
                binding.tvDoctorName.setText(doctor.getName());
                // TODO: Tải ảnh avatar bác sĩ (nếu có)
                // binding.ivDoctorAvatar.setImage...
            } else {
                binding.tvDoctorName.setText("Không rõ bác sĩ");
            }

            // Định dạng ngày giờ
            binding.tvAppointmentDatetime.setText(sdf.format(appointment.getAppointmentDate()));

            // --- BẮT ĐẦU SỬA ĐỔI ---
            // Chỉ hiển thị nút "Hủy" khi lịch hẹn còn ở trạng thái "Pending" (Chờ)
            if (appointment.getStatus().equalsIgnoreCase("Pending")) {

                binding.btnCancelOrChange.setVisibility(View.VISIBLE);
                binding.btnCancelOrChange.setText("Hủy lịch");
                binding.btnCancelOrChange.setOnClickListener(v -> listener.onCancelClick(appointment));
            } else {
                // Ẩn nút Hủy cho các trạng thái "Confirmed", "Completed", "Canceled"
                binding.btnCancelOrChange.setVisibility(View.GONE);
            }
            // --- KẾT THÚC SỬA ĐỔI ---
        }
    }
}

