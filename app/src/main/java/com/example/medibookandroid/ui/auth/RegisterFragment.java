package com.example.medibookandroid.ui.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
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
import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentRegisterBinding;
import com.example.medibookandroid.ui.common.LoadingDialog;
import com.example.medibookandroid.utils.JavaMailAPI; // Import class gửi mail

import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;
    private LoadingDialog loadingDialog;

    private String generatedOTP;
    private CountDownTimer countDownTimer;
    private String tempName, tempEmail, tempPhone, tempPassword;

    private static final Pattern UPPER_CASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_].*");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        loadingDialog = new LoadingDialog();

        binding.cbTerms.setOnCheckedChangeListener((b, c) -> binding.btnRegister.setEnabled(c));
        binding.btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                tempName = binding.tilFullName.getEditText().getText().toString().trim();
                tempEmail = binding.tilEmail.getEditText().getText().toString().trim();
                tempPhone = binding.tilPhone.getEditText().getText().toString().trim();
                tempPassword = binding.tilPassword.getEditText().getText().toString().trim();

                sendOtpToEmail(tempEmail);
            }
        });
        binding.btnConfirmOtp.setOnClickListener(v -> handleVerifyOtpAndRegister());
        binding.tvCancelOtp.setOnClickListener(v -> {
            binding.layoutOtpOverlay.setVisibility(View.GONE);
            if(countDownTimer != null) countDownTimer.cancel();
        });
        binding.tvResendOtp.setOnClickListener(v -> resendOtp());
        setupObservers();
        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
        binding.tvLogin.setOnClickListener(v -> navController.navigate(R.id.action_registerFragment_to_loginFragment));
    }

    private void sendOtpToEmail(String email) {
        if (!loadingDialog.isAdded()) loadingDialog.show(getChildFragmentManager(), "sending_otp");
        int randomPin = (int) (Math.random() * 900000) + 100000;
        generatedOTP = String.valueOf(randomPin);

        String subject = "Mã xác thực đăng ký MediBook";
        String body = "Mã OTP xác thực đăng ký tài khoản của bạn là: " + generatedOTP +
                "\n\nVui lòng không chia sẻ mã này cho bất kỳ ai.";
        new Thread(() -> {
            try {
                JavaMailAPI.sendMail(email, subject, body);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(loadingDialog.isAdded()) loadingDialog.dismiss();
                        Toast.makeText(getContext(), "Đã gửi mã xác thực tới " + email, Toast.LENGTH_SHORT).show();
                        showOtpOverlay();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(loadingDialog.isAdded()) loadingDialog.dismiss();
                        Toast.makeText(getContext(), "Lỗi gửi mail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }
    private void showOtpOverlay() {
        binding.layoutOtpOverlay.setVisibility(View.VISIBLE);
        binding.otpView.setText("");
        binding.tvOtpMessage.setText("Mã xác thực đã gửi tới: " + tempEmail);
        startCountDownTimer();
    }
    private void startCountDownTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        binding.tvResendOtp.setEnabled(false);
        binding.tvResendOtp.setAlpha(0.5f);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvOtpTimer.setText((millisUntilFinished / 1000) + "s");
            }
            @Override
            public void onFinish() {
                binding.tvOtpTimer.setText("Mã hết hạn");
                binding.tvResendOtp.setEnabled(true);
                binding.tvResendOtp.setAlpha(1.0f);
            }
        }.start();
    }
    private void resendOtp() {
        sendOtpToEmail(tempEmail);
    }
    private void handleVerifyOtpAndRegister() {
        String inputOtp = binding.otpView.getText() != null ? binding.otpView.getText().toString() : "";
        if (inputOtp.length() < 6) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!inputOtp.equals(generatedOTP)) {
            Toast.makeText(getContext(), "Mã OTP không chính xác!", Toast.LENGTH_SHORT).show();
            binding.otpView.setText("");
            return;
        }
        binding.btnConfirmOtp.setEnabled(false);
        String formattedPhone = formatPhoneNumber(tempPhone);
        viewModel.register(tempEmail, tempPassword, tempName, formattedPhone);
    }

    private void setupObservers() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                if (!loadingDialog.isAdded() && getChildFragmentManager() != null) {
                    loadingDialog.show(getChildFragmentManager(), "loading_register");
                }
            } else {
                if (loadingDialog.isAdded()) {
                    loadingDialog.dismiss();
                }
                binding.btnConfirmOtp.setEnabled(true);
            }
        });
        viewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;

            if (Boolean.TRUE.equals(success)) {
                binding.layoutOtpOverlay.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                navController.navigate(R.id.action_registerFragment_to_loginFragment);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                if (msg.contains("already in use") || msg.contains("tồn tại")) {
                    binding.layoutOtpOverlay.setVisibility(View.GONE);
                }
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        boolean valid = true;

        String name = binding.tilFullName.getEditText().getText().toString().trim();
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        String phone = binding.tilPhone.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString();

        if (name.isEmpty()) {
            binding.tilFullName.setError("Họ tên không được để trống"); valid = false;
        } else if (name.length() < 5) {
            binding.tilFullName.setError("Họ tên phải dài ít nhất 5 ký tự"); valid = false;
        } else if (!Character.isLetter(name.charAt(0)) || !Character.isUpperCase(name.charAt(0))) {
            binding.tilFullName.setError("Họ tên phải bắt đầu bằng một chữ cái viết hoa"); valid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email không được để trống"); valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không hợp lệ"); valid = false;
        }

        if (phone.isEmpty()) {
            binding.tilPhone.setError("Số điện thoại không được để trống"); valid = false;
        } else if (formatPhoneNumber(phone) == null) {
            binding.tilPhone.setError("Số điện thoại không hợp lệ"); valid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Mật khẩu không được để trống"); valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự"); valid = false;
        } else if (!UPPER_CASE_PATTERN.matcher(password).matches()) {
            binding.tilPassword.setError("Mật khẩu phải chứa ít nhất 1 chữ in hoa"); valid = false;
        } else if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            binding.tilPassword.setError("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt"); valid = false;
        }

        return valid;
    }

    private String formatPhoneNumber(String phone) {
        phone = phone.replaceAll("\\s+", "").replaceAll("[^0-9]", "");
        if (phone.startsWith("0") && phone.length() == 10) return "+84" + phone.substring(1);
        if (phone.startsWith("84") && phone.length() == 11) return "+" + phone;
        if (phone.startsWith("+84") && phone.length() == 12) return phone;
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        binding = null;
    }
}