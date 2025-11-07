package com.example.medibookandroid.ui.doctor.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.ui.adapter.DoctorRequestAdapter;
import com.example.medibookandroid.databinding.FragmentDoctorRequestsBinding;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorRequestsViewModel;

import java.util.ArrayList;

public class DoctorRequestsFragment extends Fragment {

    private FragmentDoctorRequestsBinding binding;
    private DoctorRequestsViewModel viewModel;
    private DoctorRequestAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. ⭐️ SỬA: Lấy ViewModel được chia sẻ từ Activity
        viewModel = new ViewModelProvider(requireActivity()).get(DoctorRequestsViewModel.class);

        // 2. Setup RecyclerView
        setupRecyclerView();

        // 3. Setup Observers
        setupObservers();
    }

    // ⭐️ XÓA: Hàm onResume() (ViewModel tự tải)

    private void setupRecyclerView() {
        adapter = new DoctorRequestAdapter(new ArrayList<>(), viewModel, new DoctorRequestAdapter.OnRequestInteractionListener() {
            @Override
            public void onAccept(Appointment appointment) {
                // Chỉ gọi ViewModel
                viewModel.acceptAppointment(appointment);
            }

            @Override
            public void onDecline(Appointment appointment) {
                // Chỉ gọi ViewModel
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
            }
            // Logic ẩn/hiện view "trống" được chuyển vào observer 2
        });

        // 2. ⭐️ THÊM: Quan sát trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;

            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvAppointmentRequests.setVisibility(View.GONE);
                binding.tvNoRequests.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                // Kiểm tra lại list sau khi tải xong
                if (adapter.getItemCount() == 0) {
                    binding.rvAppointmentRequests.setVisibility(View.GONE);
                    binding.tvNoRequests.setVisibility(View.VISIBLE);
                } else {
                    binding.rvAppointmentRequests.setVisibility(View.VISIBLE);
                    binding.tvNoRequests.setVisibility(View.GONE);
                }
            }
        });

        // 3. Quan sát thông báo
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ⭐️ XÓA: Hàm updateNoRequestsView() (đã gộp vào observer)

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}