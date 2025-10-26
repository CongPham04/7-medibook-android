package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.medibookandroid.databinding.FragmentBookingSummaryBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.Doctor;
import com.example.medibookandroid.model.StorageRepository;
import java.util.Date;

import android.os.Build;

import com.example.medibookandroid.model.StorageRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BookingSummaryFragment extends Fragment {

    private FragmentBookingSummaryBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingSummaryBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        Doctor doctor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            doctor = getArguments().getSerializable("doctor", Doctor.class);
        } else {
            @SuppressWarnings("deprecation")
            Doctor doctor_old = (Doctor) getArguments().getSerializable("doctor");
            doctor = doctor_old;
        }
        String time = getArguments().getString("time");

        binding.tvDoctorNameSummary.setText(doctor.getName());
        binding.tvAppointmentTimeSummary.setText(time);

        binding.btnConfirmBooking.setOnClickListener(v -> {
            String symptoms = binding.tilSymptoms.getEditText().getText().toString();
            Appointment newAppointment = new Appointment(
                    storageRepository.appointments.size() + 1,
                    1, // Mock patient ID
                    doctor.getId(),
                    new Date(), // In a real app, you'd parse the date and time
                    "Pending",
                    symptoms
            );
            storageRepository.appointments.add(newAppointment);
            storageRepository.saveAppointments();
            navController.navigate(R.id.action_bookingSummaryFragment_to_bookingSuccessFragment);
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
