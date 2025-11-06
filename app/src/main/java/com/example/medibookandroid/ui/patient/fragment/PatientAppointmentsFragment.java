package com.example.medibookandroid.ui.patient.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ⭐️ THÊM
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R; // ⭐️ THÊM
import com.example.medibookandroid.ui.adapter.AppointmentsPagerAdapter; // ⭐️ SỬA
import com.example.medibookandroid.databinding.FragmentPatientAppointmentsBinding;
import com.example.medibookandroid.ui.patient.viewmodel.PatientAppointmentsViewModel; // ⭐️ THÊM
import com.google.android.material.tabs.TabLayoutMediator;

public class PatientAppointmentsFragment extends Fragment {

    private FragmentPatientAppointmentsBinding binding;
    private PatientAppointmentsViewModel viewModel; // ⭐️ THÊM

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. ⭐️ Khởi tạo ViewModel (để chia sẻ cho các fragment con)
        viewModel = new ViewModelProvider(this).get(PatientAppointmentsViewModel.class);

        // 2. ⭐️ Tải dữ liệu MỘT LẦN ở đây
        viewModel.loadAppointments();

        // 3. Setup ViewPager (Giữ nguyên)
        AppointmentsPagerAdapter adapter = new AppointmentsPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // 4. Setup Tabs (Giữ nguyên)
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Sắp tới"); // (Pending, Confirmed)
                            break;
                        case 1:
                            tab.setText("Lịch sử"); // (Completed)
                            break;
                        case 2:
                            tab.setText("Đã huỷ"); // (Cancelled)
                            break;
                    }
                }).attach();

        // 5. Setup nút "Thêm" (Giữ nguyên)
        binding.ibAddAppointment.setOnClickListener(v -> {
            if (getActivity() != null) {
                try {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.patient_nav_host_fragment);
                    navController.navigate(R.id.patientHomeFragment);
                } catch (Exception e) {
                    // Xử lý lỗi
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
