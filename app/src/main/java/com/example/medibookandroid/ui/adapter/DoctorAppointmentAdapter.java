package com.example.medibookandroid.ui.adapter;

import android.content.Context;
import android.os.Build; // ⭐️ THÊM
import android.text.Html; // ⭐️ THÊM
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
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
    private LifecycleOwner lifecycleOwner;

    public interface OnCompleteClickListener {
        void onCompleteClick(Appointment appointment);
    }

    public DoctorAppointmentAdapter(List<Appointment> appointments,
                                    DoctorScheduleViewModel viewModel,
                                    OnCompleteClickListener completeClickListener,
                                    LifecycleOwner lifecycleOwner) {
        this.appointments = appointments;
        this.viewModel = viewModel;
        this.completeClickListener = completeClickListener;
        this.lifecycleOwner = lifecycleOwner;
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
        return new AppointmentViewHolder(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, completeClickListener, lifecycleOwner, position);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorAppointmentCardBinding binding;
        private DoctorScheduleViewModel viewModel;

        public AppointmentViewHolder(ItemDoctorAppointmentCardBinding binding, DoctorScheduleViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
        }

        public void bind(final Appointment appointment, final OnCompleteClickListener listener, LifecycleOwner owner, int position) {

            // ⭐️ BẮT ĐẦU SỬA (Hiển thị Time) ⭐️
            String timeLabel = "<b>Thời gian:</b> "; // In đậm
            String timeValue = appointment.getTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvAppointmentTime.setText(Html.fromHtml(timeLabel + timeValue, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvAppointmentTime.setText(Html.fromHtml(timeLabel + timeValue));
            }
            // ⭐️ KẾT THÚC SỬA ⭐️

            // Gán sự kiện click cho nút "Hoàn tất"
            binding.ibMarkCompleted.setOnClickListener(v -> {
                listener.onCompleteClick(appointment);
            });

            // Lấy số thứ tự (position bắt đầu từ 0, nên + 1)
            int benhNhanNumber = position + 1;
            String patientLabel = "<b>Bệnh nhân " + benhNhanNumber + ":</b> "; // In đậm

            // ⭐️ BẮT ĐẦU SỬA (Hiển thị tên - trạng thái Đang tải) ⭐️
            String loadingText = "Đang tải...";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvPatientName.setText(Html.fromHtml(patientLabel + loadingText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvPatientName.setText(Html.fromHtml(patientLabel + loadingText));
            }
            // ⭐️ KẾT THÚC SỬA ⭐️

            MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
            viewModel.loadPatientInfo(appointment.getPatientId(), patientLiveData);

            // Dùng owner (LifecycleOwner) để observe
            patientLiveData.observe(owner, patient -> {
                // ⭐️ BẮT ĐẦU SỬA (Hiển thị tên - trạng thái Tải xong) ⭐️
                String patientName;
                if (patient != null) {
                    patientName = patient.getFullName();
                } else {
                    patientName = "Không rõ";
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.tvPatientName.setText(Html.fromHtml(patientLabel + patientName, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    binding.tvPatientName.setText(Html.fromHtml(patientLabel + patientName));
                }
                // ⭐️ KẾT THÚC SỬA ⭐️
            });
        }
    }
}