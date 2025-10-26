package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.DoctorRequestAdapter;
import com.example.medibookandroid.databinding.FragmentDoctorRequestsBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.StorageRepository;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorRequestsFragment extends Fragment {

    private FragmentDoctorRequestsBinding binding;
    private StorageRepository storageRepository;
    private DoctorRequestAdapter adapter;
    private List<Appointment> pendingAppointments;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorRequestsBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pendingAppointments = storageRepository.appointments.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());

        adapter = new DoctorRequestAdapter(pendingAppointments, new DoctorRequestAdapter.OnRequestInteractionListener() {
            @Override
            public void onAccept(Appointment appointment) {
                appointment.setStatus("Confirmed");
                storageRepository.saveAppointments();
                int position = pendingAppointments.indexOf(appointment);
                pendingAppointments.remove(position);
                adapter.notifyItemRemoved(position);
                updateNoRequestsView();
            }

            @Override
            public void onDecline(Appointment appointment) {
                appointment.setStatus("Canceled");
                storageRepository.saveAppointments();
                int position = pendingAppointments.indexOf(appointment);
                pendingAppointments.remove(position);
                adapter.notifyItemRemoved(position);
                updateNoRequestsView();
            }
        });

        binding.rvAppointmentRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointmentRequests.setAdapter(adapter);

        updateNoRequestsView();
    }

    private void updateNoRequestsView() {
        if (pendingAppointments.isEmpty()) {
            binding.tvNoRequests.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoRequests.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
