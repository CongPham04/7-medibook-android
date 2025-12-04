package com.example.medibookandroid.ui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentNotificationSettingsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NotificationSettingsFragment extends Fragment {

    private FragmentNotificationSettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Xử lý nút Back
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // 2. Khởi tạo SharedPreferences
        prefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);

        // 3. Load trạng thái cũ
        loadSavedSettings();

        // 4. Lắng nghe sự kiện
        setupListeners();
    }

    private void loadSavedSettings() {
        binding.switchGeneralNotification.setChecked(prefs.getBoolean("general", true));
        binding.switchSound.setChecked(prefs.getBoolean("sound", true));
        binding.switchVibrate.setChecked(prefs.getBoolean("vibrate", false));
    }

    private void setupListeners() {
        // Switch Thông báo chung
        binding.switchGeneralNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("general", isChecked).apply();
        });

        // Switch Âm thanh
        binding.switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sound", isChecked).apply();
            if (isChecked) {
                // ⭐️ GỌI HÀM PHÁT NHẠC
                playCustomSound();
                showToast("Đã bật Âm thanh");
            }
        });

        // Switch Rung
        binding.switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("vibrate", isChecked).apply();
            if (isChecked) {
                triggerVibration();
                showToast("Đã bật Rung");
            }
        });
    }

    // --- HÀM PHÁT NHẠC (CUSTOM SOUND) ---
    private void playCustomSound() {
        if (getContext() == null) return;
        try {
            // Đảm bảo file notification_sound.mp3 nằm trong res/raw
            MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.notification_sound);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                // Giải phóng bộ nhớ ngay khi phát xong
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            } else {
                Log.e("SoundCheck", "MediaPlayer null -> Không tìm thấy file nhạc");
            }
        } catch (Exception e) {
            Log.e("SoundCheck", "Lỗi phát nhạc: " + e.getMessage());
        }
    }

    // --- HÀM RUNG ---
    private void triggerVibration() {
        if (getContext() == null) return;
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(400);
            }
        }
    }

    // --- LOGIC ẨN/HIỆN BOTTOM NAV (Xử lý thông minh cho cả Doctor & Patient) ---

    @Override
    public void onResume() {
        super.onResume();
        setBottomNavVisibility(View.GONE); // Ẩn khi vào
    }

    @Override
    public void onPause() {
        super.onPause();
        setBottomNavVisibility(View.VISIBLE); // Hiện khi ra
    }

    /**
     * Hàm này tự động tìm xem đang ở màn hình Bác sĩ hay Bệnh nhân để ẩn đúng menu
     */
    private void setBottomNavVisibility(int visibility) {
        if (getActivity() != null) {
            // Thử tìm Menu của Bệnh nhân
            BottomNavigationView patientNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (patientNav != null) {
                patientNav.setVisibility(visibility);
            }

            // Thử tìm Menu của Bác sĩ
            BottomNavigationView doctorNav = getActivity().findViewById(R.id.doctor_bottom_nav);
            if (doctorNav != null) {
                doctorNav.setVisibility(visibility);
            }
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}