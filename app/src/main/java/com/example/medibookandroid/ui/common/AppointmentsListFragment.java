package com.example.medibookandroid.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.databinding.FragmentAppointmentsListBinding;
import com.example.medibookandroid.ui.adapter.PatientAppointmentAdapter;
import com.example.medibookandroid.ui.patient.dialog.RateDoctorDialogFragment;
import com.example.medibookandroid.ui.patient.viewmodel.PatientAppointmentsViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsListFragment extends Fragment {

    private static final String ARG_STATUS_TYPE = "status_type";
    private FragmentAppointmentsListBinding binding;
    private PatientAppointmentsViewModel viewModel;
    private PatientAppointmentAdapter adapter;
    private List<Appointment> filteredAppointments;
    private String statusType; // "Upcoming", "History", hoặc "Canceled"

    public static AppointmentsListFragment newInstance(String statusType) {
        AppointmentsListFragment fragment = new AppointmentsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS_TYPE, statusType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusType = getArguments().getString(ARG_STATUS_TYPE);
        }

        try {
            viewModel = new ViewModelProvider(requireParentFragment()).get(PatientAppointmentsViewModel.class);
        } catch (IllegalStateException e) {
            viewModel = new ViewModelProvider(requireActivity()).get(PatientAppointmentsViewModel.class);
        }
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

        filteredAppointments = new ArrayList<>();

        adapter = new PatientAppointmentAdapter(filteredAppointments, viewModel, new PatientAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onCancelClick(Appointment appointment) {
                showCancelConfirmationDialog(appointment);
            }

            @Override
            public void onRateClick(Appointment appointment) {
                showRateDialog(appointment);
            }
        }, getViewLifecycleOwner());

        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointments.setAdapter(adapter);

        setupObservers();
    }

    private void setupObservers() {
        if (viewModel == null) return;

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvAppointments.setVisibility(View.GONE);
                binding.tvNoAppointments.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                updateEmptyView();
            }
        });

        viewModel.getAllAppointments().observe(getViewLifecycleOwner(), allAppointments -> {
            if (allAppointments != null) {
                filterAndDisplay(allAppointments);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplay(List<Appointment> allAppointments) {
        if (allAppointments == null || binding == null) return;

        List<Appointment> tempList = new ArrayList<>();

        for (Appointment appt : allAppointments) {
            if (appt.getStatus() == null) continue;

            String status = appt.getStatus().toLowerCase();
            boolean isPast = isAppointmentInPast(appt);

            switch (statusType) {
                case "Upcoming":
                    if ((status.equals("pending") || status.equals("confirmed")) && !isPast) {
                        tempList.add(appt);
                    }
                    break;

                case "History":
                    if (status.equals("completed") || (status.equals("confirmed") && isPast)) {
                        tempList.add(appt);
                    }
                    break;

                case "Canceled":
                    if (status.equals("cancelled") || status.equals("rejected")) {
                        tempList.add(appt);
                    }
                    break;
            }
        }

        filteredAppointments.clear();
        filteredAppointments.addAll(tempList);
        adapter.notifyDataSetChanged();

        if (viewModel.isLoading().getValue() != null && !viewModel.isLoading().getValue()) {
            updateEmptyView();
        }
    }

    private void updateEmptyView() {
        if (filteredAppointments.isEmpty()) {
            binding.tvNoAppointments.setVisibility(View.VISIBLE);
            binding.rvAppointments.setVisibility(View.GONE);
        } else {
            binding.tvNoAppointments.setVisibility(View.GONE);
            binding.rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    private void showCancelConfirmationDialog(Appointment appointment) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy lịch")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này không?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    viewModel.cancelAppointment(appointment);
                    dialog.dismiss();
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRateDialog(Appointment appointment) {
        viewModel.getDoctorById(appointment.getDoctorId()).observe(getViewLifecycleOwner(), doctor -> {
            String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";

            RateDoctorDialogFragment dialog = RateDoctorDialogFragment.newInstance(appointment.getDoctorId(), doctorName);
            dialog.setListener((doctorId, dName, rating, comment) -> {
                viewModel.submitReview(appointment.getAppointmentId(), doctorId, rating, comment);
            });

            dialog.show(getParentFragmentManager(), "RateDoctorDialog");
        });
    }

    private boolean isAppointmentInPast(Appointment appt) {
        if (appt.getDate() == null || appt.getTime() == null) return false;
        String dateTimeString = appt.getDate() + " " + appt.getTime();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());

        try {
            java.util.Date appointmentDate = sdf.parse(dateTimeString);
            java.util.Date now = new java.util.Date();
            return appointmentDate != null && appointmentDate.before(now);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}