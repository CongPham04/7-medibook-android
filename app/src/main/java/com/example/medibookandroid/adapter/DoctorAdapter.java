package com.example.medibookandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.databinding.ItemPatientDoctorCardBinding;
import com.example.medibookandroid.model.Doctor;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private final List<Doctor> doctors;
    private final OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientDoctorCardBinding binding = ItemPatientDoctorCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DoctorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor, listener);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientDoctorCardBinding binding;

        public DoctorViewHolder(ItemPatientDoctorCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Doctor doctor, final OnDoctorClickListener listener) {
            binding.tvDoctorName.setText(doctor.getName());
            binding.tvDoctorSpecialty.setText(doctor.getSpecialty());
            itemView.setOnClickListener(v -> listener.onDoctorClick(doctor));
        }
    }
}
