package com.example.medibookandroid.ui.adapter; // (Hoặc package của bạn)

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.databinding.ItemDoctorAppointmentCardBinding;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorScheduleViewModel; // Sửa import

import java.util.List;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private DoctorScheduleViewModel viewModel;

    private OnCompleteClickListener completeClickListener;

    public interface OnCompleteClickListener {
        void onCompleteClick(Appointment appointment);
    }

    public DoctorAppointmentAdapter(List<Appointment> appointments, DoctorScheduleViewModel viewModel, OnCompleteClickListener completeClickListener) {
        this.appointments = appointments;
        this.viewModel = viewModel;
        this.completeClickListener = completeClickListener; // ⭐️ THÊM
    }


    // ⭐️⭐️ THÊM HÀM NÀY VÀO ⭐️⭐️
    /**
     * Cập nhật danh sách lịch hẹn và báo cho RecyclerView vẽ lại
     */
    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged(); // Rất quan trọng!
    }
    // ⭐️⭐️ KẾT THÚC HÀM MỚI ⭐️⭐️

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorAppointmentCardBinding binding = ItemDoctorAppointmentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        // ⭐️ SỬA: Truyền listener vào ViewHolder
        return new AppointmentViewHolder(binding, viewModel, completeClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        // (Giữ nguyên)
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, (LifecycleOwner) holder.itemView.getContext());
    }

    @Override
    public int getItemCount() {
        return appointments.size(); // (GiV)
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        // (Giữ nguyên)
        private final ItemDoctorAppointmentCardBinding binding;
        private DoctorScheduleViewModel viewModel;
        // ⭐️ THÊM
        private OnCompleteClickListener completeClickListener;

        // ⭐️ SỬA: Thêm listener
        public AppointmentViewHolder(ItemDoctorAppointmentCardBinding binding, DoctorScheduleViewModel viewModel, OnCompleteClickListener completeClickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
            this.completeClickListener = completeClickListener; // ⭐️ THÊM
        }

        public void bind(final Appointment appointment, LifecycleOwner owner) {
            // (Giữ nguyên)
            binding.tvAppointmentTime.setText(appointment.getTime());
            // Gán sự kiện click cho nút "Hoàn tất"
            binding.ibMarkCompleted.setOnClickListener(v -> {
                completeClickListener.onCompleteClick(appointment);
            });
            binding.tvPatientName.setText("Đang tải...");
            MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
            viewModel.loadPatientInfo(appointment.getPatientId(), patientLiveData);
            patientLiveData.observe(owner, patient -> {
                if (patient != null) {
                    binding.tvPatientName.setText(patient.getFullName());
                } else {
                    binding.tvPatientName.setText("Không rõ bệnh nhân");
                }
            });
        }
    }
}