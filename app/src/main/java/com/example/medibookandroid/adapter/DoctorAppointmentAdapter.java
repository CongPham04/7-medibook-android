package com.example.medibookandroid.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.databinding.ItemDoctorAppointmentCardBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.MockDataRepository;
import com.example.medibookandroid.model.Patient;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointments;

    public DoctorAppointmentAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorAppointmentCardBinding binding = ItemDoctorAppointmentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorAppointmentCardBinding binding;

        public AppointmentViewHolder(ItemDoctorAppointmentCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Appointment appointment) {
            // In a real app, you'd fetch patient details based on appointment.getPatientId()
            Patient patient = MockDataRepository.getInstance().currentPatient;
            binding.tvPatientName.setText(patient.getFullName());

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            binding.tvAppointmentTime.setText(sdf.format(appointment.getAppointmentDate()));
        }
    }
}
