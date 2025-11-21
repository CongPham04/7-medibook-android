package com.example.medibookandroid.ui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

public abstract class BaseNotificationSettingsFragment extends Fragment {

    protected FragmentNotificationSettingsBinding binding;
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

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        prefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);

        loadSavedSettings();
        setupListeners();
    }
    private void loadSavedSettings() {
        binding.switchGeneralNotification.setChecked(prefs.getBoolean("general", true));
        binding.switchSound.setChecked(prefs.getBoolean("sound", true));
        binding.switchVibrate.setChecked(prefs.getBoolean("vibrate", false));
    }

    private void setupListeners() {
        binding.switchGeneralNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("general", isChecked).apply();
            if (isChecked) showToast("Đã BẬT nhận thông báo");
        });

        // Xử lý Âm thanh
        binding.switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sound", isChecked).apply();
            if (isChecked) {
                // ⭐️ GỌI HÀM PHÁT FILE MP3 CỦA BẠN
                playCustomSound();
                showToast("Đã bật Âm thanh");
            }
        });

        // Xử lý Rung
        binding.switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("vibrate", isChecked).apply();
            if (isChecked) {
                triggerVibration();
                showToast("Đã bật Rung");
            }
        });
    }

    /**
     * ⭐️ HÀM MỚI: Phát file notification_sound.mp3 từ thư mục raw
     */
    private void playCustomSound() {
        if (getContext() == null) return;

        try {
            // Tạo MediaPlayer trỏ tới file trong thư mục raw
            // Đảm bảo file notification_sound.mp3 đã nằm trong res/raw
            MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.notification_sound);

            if (mediaPlayer != null) {
                mediaPlayer.start();

                // Quan trọng: Giải phóng bộ nhớ sau khi phát xong để tránh leak
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: Nếu lỗi file thì dùng tiếng hệ thống
            // playSystemSound();
        }
    }

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
    // --- LOGIC ẨN/HIỆN BOTTOM NAV ---

    @Override
    public void onResume() {
        super.onResume();
        toggleBottomNav(false); // Ẩn
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleBottomNav(true); // Hiện
    }

    private void toggleBottomNav(boolean isVisible) {
        if (getActivity() != null) {
            // Gọi hàm abstract để lấy ID đúng
            View bottomNav = getActivity().findViewById(getBottomNavigationId());
            if (bottomNav != null) {
                bottomNav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // --- HÀM TRỪU TƯỢNG ---
    protected abstract int getBottomNavigationId();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}