package com.example.medibookandroid.ui.patient.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ⭐️ THÊM IMPORT
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.adapter.DoctorAdapter;
import com.example.medibookandroid.data.model.Doctor; // ⭐️ THÊM IMPORT
import com.example.medibookandroid.databinding.FragmentPatientHomeBinding;
import com.example.medibookandroid.ui.patient.viewmodel.PatientViewModel; // ⭐️ THÊM IMPORT
import com.google.firebase.auth.FirebaseAuth; // ⭐️ THÊM IMPORT
import com.google.firebase.auth.FirebaseUser; // ⭐️ THÊM IMPORT

import java.util.ArrayList; // ⭐️ THÊM IMPORT

public class PatientHomeFragment extends Fragment {

    private FragmentPatientHomeBinding binding;
    private PatientViewModel viewModel; // ⭐️ SỬA
    private DoctorAdapter doctorAdapter; // ⭐️ SỬA
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientHomeBinding.inflate(inflater, container, false);
        // ⭐️ XÓA: storageRepository
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        loadData();
    }

    private void setupRecyclerView() {
        // Khởi tạo Adapter với danh sách rỗng
        doctorAdapter = new DoctorAdapter(new ArrayList<>(), doctor -> {
            Bundle bundle = new Bundle();
            // ⭐️ SỬA: Gửi String ID, không phải Int
            bundle.putString("doctorId", doctor.getDoctorId());
            navController.navigate(R.id.action_patientHomeFragment_to_patientDoctorDetailFragment, bundle);
        });
        binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDoctorList.setAdapter(doctorAdapter);
    }

    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String patientId = user.getUid();
            // 1. Tải thông tin bệnh nhân (để chào)
            viewModel.loadPatient(patientId);
            // 2. Tải danh sách bác sĩ (lần đầu)
            viewModel.setSearchQuery(""); // Truy vấn rỗng để tải tất cả
        } else {
            Toast.makeText(getContext(), "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
            // (Thêm logic điều hướng về Login nếu cần)
        }
    }

    private void setupObservers() {
        // 1. Quan sát thông tin bệnh nhân
        viewModel.getPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null && patient.getFullName() != null) {
                binding.tvWelcomeUser.setText("Chào, " + patient.getFullName() + "!");
                // (Thêm code tải Avatar cho binding.ivUserAvatar nếu muốn)
            } else {
                binding.tvWelcomeUser.setText("Chào bạn!");
            }
        });

        // 2. Quan sát danh sách bác sĩ (bao gồm cả kết quả tìm kiếm)
        viewModel.getDoctors().observe(getViewLifecycleOwner(), doctors -> {
            if (doctors != null) {
                doctorAdapter.updateData(doctors);
            }
        });
    }

    private void setupListeners() {
        // 3. Listener cho thanh tìm kiếm
        binding.tilSearch.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                viewModel.setSearchQuery(query);
            }
        });

        // (Thêm listener cho icon setting, notification...)
        // binding.ibSettings.setOnClickListener(...);
        // binding.ibNotifications.setOnClickListener(...);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}