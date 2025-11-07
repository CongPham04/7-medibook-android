package com.example.medibookandroid.ui.patient.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.adapter.DoctorAdapter;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.databinding.FragmentPatientHomeBinding;
import com.example.medibookandroid.ui.patient.viewmodel.NotificationViewModel;
import com.example.medibookandroid.ui.patient.viewmodel.PatientViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class PatientHomeFragment extends Fragment {

    private FragmentPatientHomeBinding binding;
    private PatientViewModel viewModel;
    private NotificationViewModel notificationViewModel;
    private DoctorAdapter doctorAdapter;

    // Logic cho Slider
    private Handler sliderHandler;
    private Runnable sliderRunnable;
    private int[] bannerImages = {
            R.drawable.medibook_banner, // Ảnh 1 (bạn đã có)
            R.drawable.medibook_banner2,      // (Bạn cần thêm ảnh này vào res/drawable)
            R.drawable.medibook_banner3       // (Bạn cần thêm ảnh này vào res/drawable)
    };
    private int currentBannerIndex = 0;
    private final long SLIDER_DELAY_MS = 4000; // 3 giây (Thời gian chờ)
    private final long SLIDER_ANIM_DURATION = 500; // 0.5 giây (Thời gian trượt)

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        sliderHandler = new Handler(Looper.getMainLooper());

        setupRecyclerView();
        setupObservers();
        setupListeners();

        loadData();
    }

    // ⭐️ BẮT ĐẦU SỬA: Logic Animation ⭐️
    private void startBannerSlider() {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && getContext() != null) {

                    // 1. Animate OUT (Trượt sang trái)
                    binding.ivHospitalBanner.animate()
                            .translationX(-binding.ivHospitalBanner.getWidth()) // Di chuyển X sang âm (ra khỏi màn hình bên trái)
                            .alpha(0.5f) // Hơi mờ đi
                            .setDuration(SLIDER_ANIM_DURATION) // 0.5 giây
                            .withEndAction(() -> {
                                // 2. Khi đã khuất (animated out):
                                if (binding != null) {
                                    // Đổi ảnh
                                    currentBannerIndex = (currentBannerIndex + 1) % bannerImages.length;
                                    binding.ivHospitalBanner.setImageResource(bannerImages[currentBannerIndex]);

                                    // Đặt lại vị trí (ngoài màn hình, bên phải)
                                    binding.ivHospitalBanner.setTranslationX(binding.ivHospitalBanner.getWidth());

                                    // 3. Animate IN (Trượt từ phải vào)
                                    binding.ivHospitalBanner.animate()
                                            .translationX(0) // Về vị trí cũ (0)
                                            .alpha(1.0f) // Rõ nét
                                            .setDuration(SLIDER_ANIM_DURATION)
                                            .start(); // Bắt đầu animation "IN"
                                }
                            }).start(); // Bắt đầu animation "OUT"

                    // 4. Lặp lại
                    sliderHandler.postDelayed(this, SLIDER_DELAY_MS);
                }
            }
        };
        // Bắt đầu chạy lần đầu
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);
    }
    // ⭐️ KẾT THÚC SỬA ⭐️

    private void stopBannerSlider() {
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBannerSlider(); // Bắt đầu chạy khi quay lại fragment
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerSlider(); // Dừng lại khi rời fragment (rất quan trọng)
    }


    private void setupRecyclerView() {
        doctorAdapter = new DoctorAdapter(new ArrayList<>(), doctor -> {
            Bundle bundle = new Bundle();
            bundle.putString("doctorId", doctor.getDoctorId());
            if (getActivity() != null) {
                try {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.patient_nav_host_fragment);
                    navController.navigate(R.id.action_patientHomeFragment_to_patientDoctorDetailFragment, bundle);
                } catch (Exception e) {
                    Log.e("PatientHomeFragment", "Lỗi điều hướng", e);
                }
            }
        });
        binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDoctorList.setAdapter(doctorAdapter);
    }

    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String patientId = user.getUid();
            viewModel.loadPatient(patientId);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvDoctorList.setVisibility(View.GONE);
            binding.tvNoData.setVisibility(View.GONE);
            viewModel.setSearchQuery("");
        } else {
            Toast.makeText(getContext(), "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        // 1. Quan sát thông tin bệnh nhân (cho lời chào và avatar)
        viewModel.getPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null && patient.getFullName() != null) {
                binding.tvWelcomeUser.setText("👋 Chào, " + patient.getFullName() + "!");
                if (patient.getAvatarUrl() != null && !patient.getAvatarUrl().isEmpty() && getContext() != null) {
                    Glide.with(getContext())
                            .load(patient.getAvatarUrl())
                            .placeholder(R.drawable.logo2)
                            .circleCrop()
                            .into(binding.ivUserAvatar);
                }
            } else {
                binding.tvWelcomeUser.setText("Chào bạn!");
            }
        });

        // 2. Quan sát danh sách bác sĩ (để hiển thị loading/list/empty)
        viewModel.getDoctors().observe(getViewLifecycleOwner(), doctors -> {
            binding.progressBar.setVisibility(View.GONE);
            if (doctors != null && !doctors.isEmpty()) {
                binding.rvDoctorList.setVisibility(View.VISIBLE);
                binding.tvNoData.setVisibility(View.GONE);
                doctorAdapter.updateData(doctors);
            } else {
                binding.rvDoctorList.setVisibility(View.GONE);
                binding.tvNoData.setVisibility(View.VISIBLE);
            }
        });

        // 3. Quan sát số lượng thông báo chưa đọc (từ ViewModel của Activity)
        notificationViewModel.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            if (count == null) return;

            if (count > 0) {
                binding.tvNotificationBadge.setText(String.valueOf(count));
                binding.tvNotificationBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvNotificationBadge.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        // 3. Listener cho thanh tìm kiếm
        binding.tilSearch.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvDoctorList.setVisibility(View.GONE);
                binding.tvNoData.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                viewModel.setSearchQuery(query);
            }
        });

        // 4. Listener cho icon Settings (Điều hướng "con")
        binding.ibSettings.setOnClickListener(v -> {
            if (getActivity() != null) {
                try {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.patient_nav_host_fragment);
                    navController.navigate(R.id.action_patientHomeFragment_to_settingsFragment);
                } catch (Exception e) {
                    Log.e("PatientHomeFragment", "Lỗi điều hướng sang Settings", e);
                }
            }
        });

        // 5. Listener cho icon Notifications (Chuyển "Tab")
        binding.flNotificationsIcon.setOnClickListener(v -> {
            if (getActivity() != null) {
                try {
                    // Tìm BottomNav trong Activity
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
                    if (bottomNav != null) {
                        // "Chọn" tab Notifications
                        bottomNav.setSelectedItemId(R.id.patientNotificationsFragment);
                    }
                } catch (Exception e) {
                    Log.e("PatientHomeFragment", "Lỗi chuyển tab Notifications", e);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        stopBannerSlider(); // Dừng hẳn vòng lặp
    }
}