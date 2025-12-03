package com.example.medibookandroid.ui.doctor.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentDoctorPasswordManagerBinding;
import com.example.medibookandroid.utils.JavaMailAPI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.chaos.view.PinView;
import java.util.regex.Pattern;

public class DoctorPasswordManagerFragment extends Fragment {

    private FragmentDoctorPasswordManagerBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private String generatedOTP;
    private long otpGenerationTime;
    private static final long OTP_VALIDITY_DURATION = 60 * 1000;
    private CountDownTimer countDownTimer;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{6,}$");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorPasswordManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        final NavController navController = Navigation.findNavController(view);
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        binding.btnContinue.setOnClickListener(v -> {
            handleVerifyAndSendMail();
        });
        binding.btnConfirmOtp.setOnClickListener(v -> {
            handleVerifyOtpAndChangePass(navController);
        });
        binding.tvResendOtp.setOnClickListener(v -> {
            resendOtp();
        });
        binding.tvCancelOtp.setOnClickListener(v -> {
            binding.layoutOtpOverlay.setVisibility(View.GONE);
            if(countDownTimer != null) countDownTimer.cancel();
        });
    }

    private void handleVerifyAndSendMail() {
        String oldPassword = binding.tilCurrentPassword.getEditText().getText().toString().trim();
        String newPassword = binding.tilNewPassword.getEditText().getText().toString().trim();
        String confirmPassword = binding.tilConfirmNewPassword.getEditText().getText().toString().trim();

        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmNewPassword.setError(null);

        if (oldPassword.isEmpty()) {
            binding.tilCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            return;
        }
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.setError("Mật khẩu mới không được để trống");
            return;
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            binding.tilNewPassword.setError("Mật khẩu yếu: Cần 6 ký tự, 1 hoa, 1 thường, 1 số, 1 ký tự đặc biệt.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        progressDialog.show();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendOtpEmail(user.getEmail());
                } else {
                    progressDialog.dismiss();
                    binding.tilCurrentPassword.setError("Mật khẩu hiện tại không đúng!");
                }
            });
        }
    }

    private void sendOtpEmail(String email) {
        int randomPin = (int) (Math.random() * 900000) + 100000;
        generatedOTP = String.valueOf(randomPin);
        otpGenerationTime = System.currentTimeMillis();

        String subject = "Mã xác thực đổi mật khẩu";
        String body = "Mã OTP của bạn là: " + generatedOTP;

        new Thread(() -> {
            try {
                JavaMailAPI.sendMail(email, subject, body);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showToast("Đã gửi mã xác thực!");

                        showOtpOverlay();
                        startCountDownTimer();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showToast("Lỗi gửi mail: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private void showOtpOverlay() {
        binding.layoutOtpOverlay.setVisibility(View.VISIBLE);
        binding.otpView.setText("");
        binding.otpView.requestFocus();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            binding.tvOtpMessage.setText("Đã gửi mã 6 số tới: " + user.getEmail());
        }
    }

    private void startCountDownTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        binding.tvResendOtp.setEnabled(false);
        binding.tvResendOtp.setAlpha(0.5f);
        countDownTimer = new CountDownTimer(OTP_VALIDITY_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvOtpTimer.setText((millisUntilFinished / 1000) + "s");
            }
            @Override
            public void onFinish() {
                binding.tvOtpTimer.setText("0s");
                binding.tvResendOtp.setEnabled(true);
                binding.tvResendOtp.setAlpha(1.0f);
            }
        }.start();
    }
    private void resendOtp() {
        progressDialog.setMessage("Đang gửi lại mã...");
        progressDialog.show();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) sendOtpEmail(user.getEmail());
    }

    private void handleVerifyOtpAndChangePass(NavController navController) {
        String inputOtp = binding.otpView.getText() != null ? binding.otpView.getText().toString() : "";
        String newPassword = binding.tilNewPassword.getEditText().getText().toString().trim();

        if (inputOtp.length() < 6) {
            showToast("Vui lòng nhập đủ 6 số!");
            return;
        }
        if (!inputOtp.equals(generatedOTP)) {
            showToast("Mã OTP không đúng!");
            binding.otpView.setText("");
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - otpGenerationTime > OTP_VALIDITY_DURATION) {
            showToast("Mã OTP đã hết hạn. Hãy gửi lại!");
            return;
        }
        progressDialog.setMessage("Đang đổi mật khẩu...");
        progressDialog.show();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    showToast("Đổi mật khẩu thành công!");
                    navController.navigateUp();
                } else {
                    showToast("Lỗi: " + task.getException().getMessage());
                }
            });
        }
    }

    private void showToast(String msg) {
        if (getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.doctor_bottom_nav);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.doctor_bottom_nav);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }
    }
}