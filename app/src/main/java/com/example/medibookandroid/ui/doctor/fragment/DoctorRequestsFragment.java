package com.example.medibookandroid.ui.doctor.fragment; // ⭐️ SỬA PACKAGE NẾU CẦN

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // ⭐️ THÊM
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ⭐️ THÊM
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.ui.adapter.DoctorRequestAdapter; // ⭐️ SỬA
import com.example.medibookandroid.databinding.FragmentDoctorRequestsBinding;
import com.example.medibookandroid.data.model.Appointment; // ⭐️ SỬA
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorRequestsViewModel; // ⭐️ THÊM

import java.util.ArrayList; // ⭐️ THÊM
import java.util.List;

public class DoctorRequestsFragment extends Fragment {

    private FragmentDoctorRequestsBinding binding;
    private DoctorRequestsViewModel viewModel; // ⭐️ SỬA
    private DoctorRequestAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorRequestsBinding.inflate(inflater, container, false);
        // ⭐️ XÓA: StorageRepository
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(DoctorRequestsViewModel.class);

        // 2. Setup RecyclerView
        setupRecyclerView();

        // 3. Setup Observers
        setupObservers();
    }

    // ⭐️ SỬA: Tải dữ liệu trong onResume để luôn cập nhật
    @Override
    public void onResume() {
        super.onResume();
        // Tải (hoặc tải lại) danh sách "Pending"
        if (viewModel != null) {
            viewModel.loadPendingRequests();
        }
    }

    private void setupRecyclerView() {
        adapter = new DoctorRequestAdapter(new ArrayList<>(), viewModel, new DoctorRequestAdapter.OnRequestInteractionListener() {
            @Override
            public void onAccept(Appointment appointment) {
                // ⭐️ SỬA: Chỉ gọi ViewModel
                viewModel.acceptAppointment(appointment);
            }

            @Override
            public void onDecline(Appointment appointment) {
                // ⭐️ SỬA: Chỉ gọi ViewModel
                viewModel.declineAppointment(appointment);
            }
        });

        binding.rvAppointmentRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointmentRequests.setAdapter(adapter);
    }

    private void setupObservers() {
        // 1. Quan sát danh sách yêu cầu
        viewModel.getPendingRequests().observe(getViewLifecycleOwner(), pendingList -> {
            if (pendingList != null) {
                adapter.updateData(pendingList);
                updateNoRequestsView(pendingList.isEmpty());
            }
        });

        // 2. Quan sát thông báo
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNoRequestsView(boolean isEmpty) {
        if (isEmpty) {
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

