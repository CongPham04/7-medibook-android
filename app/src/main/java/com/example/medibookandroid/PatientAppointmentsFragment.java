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

import com.example.medibookandroid.adapter.AppointmentsPagerAdapter;
import com.example.medibookandroid.databinding.FragmentPatientAppointmentsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class PatientAppointmentsFragment extends Fragment {

    private FragmentPatientAppointmentsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- SỬA ĐỔI: Phải dùng 'requireActivity()' (FragmentActivity)
        // vì constructor của AppointmentsPagerAdapter yêu cầu nó ---
        AppointmentsPagerAdapter adapter = new AppointmentsPagerAdapter(requireActivity());
        binding.viewPager.setAdapter(adapter);

        // Thiết lập liên kết giữa TabLayout và ViewPager
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
                            tab.setText("Đã huỷ"); // (Canceled)
                            break;
                    }
                }).attach();

        // --- THÊM MỚI: Xử lý nút "Thêm lịch khám" ---
        binding.ibAddAppointment.setOnClickListener(v -> {
            // Giả sử bệnh nhân quay lại Home (patientHomeFragment) để chọn bác sĩ
            if (getActivity() != null) {
                try {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.patient_nav_host_fragment);
                    navController.navigate(R.id.patientHomeFragment);
                } catch (Exception e) {
                    // Xử lý lỗi nếu không tìm thấy NavController
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

