package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.PatientAppointmentAdapter;
import com.example.medibookandroid.databinding.FragmentAppointmentsListBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.StorageRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentsListFragment extends Fragment {

    private static final String ARG_STATUS = "status";
    private FragmentAppointmentsListBinding binding;
    private StorageRepository storageRepository;
    private String status;

    public static AppointmentsListFragment newInstance(String status) {
        AppointmentsListFragment fragment = new AppointmentsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }
        storageRepository = StorageRepository.getInstance(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAppointmentsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Appointment> filteredAppointments = storageRepository.appointments.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        PatientAppointmentAdapter adapter = new PatientAppointmentAdapter(filteredAppointments, appointment -> {
            appointment.setStatus("Canceled");
            storageRepository.saveAppointments();
            // In a real app, you would notify the adapter of the change.
        });

        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointments.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
