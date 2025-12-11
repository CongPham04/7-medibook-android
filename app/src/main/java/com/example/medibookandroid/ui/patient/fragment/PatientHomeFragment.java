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
            R.drawable.medibook_banner,
            R.drawable.medibook_banner2,
            R.drawable.medibook_banner3
    };
    private int currentBannerIndex = 0;
    private final long SLIDER_DELAY_MS = 4000;
    private final long SLIDER_ANIM_DURATION = 500;

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
        setupListeners(); // ⭐️ Đã sửa logic tìm kiếm ở trong này

        loadData();
    }

    private void startBannerSlider() {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && getContext() != null) {
                    binding.ivHospitalBanner.animate()
                            .translationX(-binding.ivHospitalBanner.getWidth())
                            .alpha(0.5f)
                            .setDuration(SLIDER_ANIM_DURATION)
                            .withEndAction(() -> {
                                if (binding != null) {
                                    currentBannerIndex = (currentBannerIndex + 1) % bannerImages.length;
                                    binding.ivHospitalBanner.setImageResource(bannerImages[currentBannerIndex]);
                                    binding.ivHospitalBanner.setTranslationX(binding.ivHospitalBanner.getWidth());
                                    binding.ivHospitalBanner.animate()
                                            .translationX(0)
                                            .alpha(1.0f)
                                            .setDuration(SLIDER_ANIM_DURATION)
                                            .start();
                                }
                            }).start();
                    sliderHandler.postDelayed(this, SLIDER_DELAY_MS);
                }
            }
        };
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);
    }

    private void stopBannerSlider() {
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBannerSlider();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerSlider();
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
            // Ban đầu chưa có dữ liệu tìm kiếm, để trắng query
            viewModel.setSearchQuery("");
        } else {
            Toast.makeText(getContext(), "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        // Quan sát thông tin bệnh nhân
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

        // Quan sát danh sách bác sĩ
        // Khi ViewModel nhận tín hiệu từ Repository -> displayedDoctors thay đổi -> RecyclerView tự update
        viewModel.getDoctors().observe(getViewLifecycleOwner(), doctors -> {
            binding.progressBar.setVisibility(View.GONE);
            if (doctors != null && !doctors.isEmpty()) {
                binding.rvDoctorList.setVisibility(View.VISIBLE);
                binding.tvNoData.setVisibility(View.GONE);

                // Adapter sẽ nhận list mới có số sao (rating) mới nhất
                doctorAdapter.updateData(doctors);
            } else {
                binding.rvDoctorList.setVisibility(View.GONE);
                binding.tvNoData.setVisibility(View.VISIBLE);
            }
        });

        // Quan sát thông báo
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
        // ⭐️ SỬA: Listener cho thanh tìm kiếm
        binding.tilSearch.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ⭐️ QUAN TRỌNG: Đã xóa code hiện ProgressBar và ẩn RecyclerView ở đây.
                // Lý do: Việc lọc trên RAM rất nhanh, nếu hiện loading sẽ gây nháy màn hình.
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString(); // Không cần trim() ngay để user có thể gõ dấu cách
                // Gọi hàm lọc bên ViewModel
                viewModel.setSearchQuery(query);
            }
        });

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

        binding.flNotificationsIcon.setOnClickListener(v -> {
            if (getActivity() != null) {
                try {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
                    if (bottomNav != null) {
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
        stopBannerSlider();
    }
}