package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.TimeSlotAdapter;
import com.example.medibookandroid.databinding.FragmentPatientDoctorDetailBinding;
import com.example.medibookandroid.model.Doctor;
import com.example.medibookandroid.model.StorageRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Arrays;
import java.util.List;

public class PatientDoctorDetailFragment extends Fragment {

    private FragmentPatientDoctorDetailBinding binding;
    private StorageRepository storageRepository;
    private Doctor selectedDoctor;
    private String selectedTimeSlot = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientDoctorDetailBinding.inflate(inflater, container, false);
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

        int doctorId = getArguments().getInt("doctorId");
        selectedDoctor = storageRepository.findDoctorById(doctorId);

        if (selectedDoctor != null) {
            binding.tvDoctorName.setText(selectedDoctor.getName());
            binding.tvDoctorSpecialty.setText(selectedDoctor.getSpecialty());
            // In a real app, you'd load more details.
        }

        // Mock time slots
        List<String> timeSlots = Arrays.asList("10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM");
        TimeSlotAdapter adapter = new TimeSlotAdapter(timeSlots, time -> {
            selectedTimeSlot = time;
        });
        binding.rvTimeSlots.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTimeSlots.setAdapter(adapter);

        binding.btnBookAppointment.setOnClickListener(v -> {
            if (selectedTimeSlot != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("doctor", selectedDoctor);
                bundle.putString("time", selectedTimeSlot);
                navController.navigate(R.id.action_patientDoctorDetailFragment_to_bookingSummaryFragment, bundle);
            } else {
                Toast.makeText(getContext(), "Please select a time slot", Toast.LENGTH_SHORT).show();
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
