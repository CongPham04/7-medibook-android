package com.example.medibookandroid.ui.adapter;

import android.content.Context; // ⭐️ THÊM
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner; // ⭐️ THÊM
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.databinding.ItemDoctorAppointmentCardBinding;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorScheduleViewModel;

import java.util.List;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private DoctorScheduleViewModel viewModel;
    private OnCompleteClickListener completeClickListener;
    private LifecycleOwner lifecycleOwner; // ⭐️ THÊM: Biến để giữ LifecycleOwner

    public interface OnCompleteClickListener {
        void onCompleteClick(Appointment appointment);
    }

    // ⭐️ SỬA: Constructor (hàm khởi tạo) giờ nhận 4 tham số
    public DoctorAppointmentAdapter(List<Appointment> appointments,
                                    DoctorScheduleViewModel viewModel,
                                    OnCompleteClickListener completeClickListener,
                                    LifecycleOwner lifecycleOwner) { // ⭐️ THÊM
        this.appointments = appointments;
        this.viewModel = viewModel;
        this.completeClickListener = completeClickListener;
        this.lifecycleOwner = lifecycleOwner; // ⭐️ THÊM
    }


    /**
     * Cập nhật danh sách lịch hẹn và báo cho RecyclerView vẽ lại
     */
    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorAppointmentCardBinding binding = ItemDoctorAppointmentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        // ⭐️ SỬA: Chỉ cần truyền ViewModel
        return new AppointmentViewHolder(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        // ⭐️ SỬA: Truyền listener và owner (đã lưu) vào hàm bind
        holder.bind(appointment, completeClickListener, lifecycleOwner);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorAppointmentCardBinding binding;
        private DoctorScheduleViewModel viewModel;
        // ⭐️ XÓA: Không cần listener ở đây

        // ⭐️ SỬA: Constructor chỉ cần ViewModel
        public AppointmentViewHolder(ItemDoctorAppointmentCardBinding binding, DoctorScheduleViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
        }

        // ⭐️ SỬA: Hàm bind nhận listener và owner
        public void bind(final Appointment appointment, final OnCompleteClickListener listener, LifecycleOwner owner) {
            binding.tvAppointmentTime.setText("Thời gian: " + appointment.getTime());

            // Gán sự kiện click cho nút "Hoàn tất"
            binding.ibMarkCompleted.setOnClickListener(v -> {
                listener.onCompleteClick(appointment);
            });

            binding.tvPatientName.setText("Đang tải...");
            MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
            viewModel.loadPatientInfo(appointment.getPatientId(), patientLiveData);

            // Dùng owner (LifecycleOwner) để observe
            patientLiveData.observe(owner, patient -> {
                if (patient != null) {
                    binding.tvPatientName.setText("Bệnh nhân: " + patient.getFullName());
                } else {
                    binding.tvPatientName.setText("Không rõ bệnh nhân");
                }
            });
        }
    }
}