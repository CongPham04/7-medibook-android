package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.DoctorAppointmentAdapter;
import com.example.medibookandroid.databinding.DialogAddScheduleSlotBinding;
import com.example.medibookandroid.databinding.FragmentDoctorScheduleBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.MockDataRepository;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorScheduleFragment extends Fragment {

    private FragmentDoctorScheduleBinding binding;
    private MockDataRepository mockDataRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorScheduleBinding.inflate(inflater, container, false);
        mockDataRepository = MockDataRepository.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            updateAppointmentsForDate(selectedDate);
        });

        binding.fabAddSlot.setOnClickListener(v -> showAddSlotDialog());

        // Initial load for today
        updateAppointmentsForDate(Calendar.getInstance());
    }

    private void updateAppointmentsForDate(Calendar selectedDate) {
        List<Appointment> confirmedAppointments = mockDataRepository.appointments.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase("Confirmed"))
                .filter(a -> {
                    Calendar appointmentDate = Calendar.getInstance();
                    appointmentDate.setTime(a.getAppointmentDate());
                    return selectedDate.get(Calendar.YEAR) == appointmentDate.get(Calendar.YEAR) &&
                           selectedDate.get(Calendar.MONTH) == appointmentDate.get(Calendar.MONTH) &&
                           selectedDate.get(Calendar.DAY_OF_MONTH) == appointmentDate.get(Calendar.DAY_OF_MONTH);
                })
                .collect(Collectors.toList());

        DoctorAppointmentAdapter adapter = new DoctorAppointmentAdapter(confirmedAppointments);
        binding.rvConfirmedAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConfirmedAppointments.setAdapter(adapter);
    }

    private void showAddSlotDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogAddScheduleSlotBinding dialogBinding = DialogAddScheduleSlotBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();

        dialogBinding.btnSaveSlot.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Slot saved", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogBinding.toolbar.setNavigationOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
