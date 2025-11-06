package com.example.medibookandroid.ui.adapter; // ⭐️ SỬA PACKAGE NẾU CẦN

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibookandroid.data.model.Patient; // ⭐️ THÊM
import com.example.medibookandroid.databinding.ItemDoctorAppointmentRequestBinding;
import com.example.medibookandroid.data.model.Appointment; // ⭐️ SỬA
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorRequestsViewModel; // ⭐️ THÊM

import java.util.List;

public class DoctorRequestAdapter extends RecyclerView.Adapter<DoctorRequestAdapter.RequestViewHolder> {

    private List<Appointment> appointments;
    private final OnRequestInteractionListener listener;
    private final DoctorRequestsViewModel viewModel; // ⭐️ THÊM

    public interface OnRequestInteractionListener {
        void onAccept(Appointment appointment);
        void onDecline(Appointment appointment);
    }

    public DoctorRequestAdapter(List<Appointment> appointments, DoctorRequestsViewModel viewModel, OnRequestInteractionListener listener) {
        this.appointments = appointments;
        this.viewModel = viewModel; // ⭐️ THÊM
        this.listener = listener;
    }

    // ⭐️ THÊM: Hàm cập nhật dữ liệu
    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorAppointmentRequestBinding binding = ItemDoctorAppointmentRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestViewHolder(binding, viewModel); // ⭐️ SỬA
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener, (LifecycleOwner) holder.itemView.getContext()); // ⭐️ SỬA
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorAppointmentRequestBinding binding;
        private final DoctorRequestsViewModel viewModel; // ⭐️ THÊM

        public RequestViewHolder(ItemDoctorAppointmentRequestBinding binding, DoctorRequestsViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel; // ⭐️ THÊM
        }

        public void bind(final Appointment appointment, final OnRequestInteractionListener listener, LifecycleOwner owner) {

            // 1. Tải tên bệnh nhân bất đồng bộ
            binding.tvPatientName.setText("Đang tải..."); // Tên mặc định
            MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
            viewModel.loadPatientInfo(appointment.getPatientId(), patientLiveData);

            patientLiveData.observe(owner, patient -> {
                if (patient != null) {
                    binding.tvPatientName.setText(patient.getFullName());
                } else {
                    binding.tvPatientName.setText("Không rõ bệnh nhân");
                }
            });

            // 2. Hiển thị dữ liệu thật từ Appointment
            String dateTime = appointment.getDate() + ", " + appointment.getTime();
            binding.tvRequestedDatetime.setText(dateTime);
            binding.tvSymptomsDescription.setText(appointment.getDescription());

            // 3. Gán listener
            binding.btnAccept.setOnClickListener(v -> listener.onAccept(appointment));
            binding.btnDecline.setOnClickListener(v -> listener.onDecline(appointment));
        }
    }
}
