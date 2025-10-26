package com.example.medibookandroid.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.databinding.ItemDoctorAppointmentRequestBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.StorageRepository;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DoctorRequestAdapter extends RecyclerView.Adapter<DoctorRequestAdapter.RequestViewHolder> {

    private final List<Appointment> appointments;
    private final OnRequestInteractionListener listener;

    public interface OnRequestInteractionListener {
        void onAccept(Appointment appointment);
        void onDecline(Appointment appointment);
    }

    public DoctorRequestAdapter(List<Appointment> appointments, OnRequestInteractionListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorAppointmentRequestBinding binding = ItemDoctorAppointmentRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorAppointmentRequestBinding binding;

        public RequestViewHolder(ItemDoctorAppointmentRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Appointment appointment, final OnRequestInteractionListener listener) {
            // In a real app, you'd fetch patient details based on appointment.getPatientId()
            binding.tvPatientName.setText("Jane Doe");

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, MMMM dd, yyyy", Locale.getDefault());
            binding.tvRequestedDatetime.setText(sdf.format(appointment.getAppointmentDate()));
            binding.tvSymptomsDescription.setText(appointment.getSymptoms());

            binding.btnAccept.setOnClickListener(v -> listener.onAccept(appointment));
            binding.btnDecline.setOnClickListener(v -> listener.onDecline(appointment));
        }
    }
}
