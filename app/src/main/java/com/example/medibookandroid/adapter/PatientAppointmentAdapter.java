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

public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointments;
    private final OnAppointmentCancelListener listener;

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

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientAppointmentCardBinding binding;

        public AppointmentViewHolder(ItemPatientAppointmentCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Appointment appointment, final OnAppointmentCancelListener listener) {
            StorageRepository repository = StorageRepository.getInstance(itemView.getContext());
            Doctor doctor = repository.findDoctorById(appointment.getDoctorId());
            if (doctor != null) {
                binding.tvDoctorName.setText(doctor.getName());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, MMMM dd, yyyy", Locale.getDefault());
            binding.tvAppointmentDatetime.setText(sdf.format(appointment.getAppointmentDate()));

            if (appointment.getStatus().equals("Upcoming")) {
                binding.btnCancelOrChange.setVisibility(View.VISIBLE);
                binding.btnCancelOrChange.setOnClickListener(v -> listener.onCancelClick(appointment));
            } else {
                binding.btnCancelOrChange.setVisibility(View.GONE);
            }
        }
    }
}
