package com.example.medibookandroid.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R;
import com.example.medibookandroid.data.local.SharedPrefHelper;
import com.example.medibookandroid.databinding.FragmentLoginBinding;
import com.example.medibookandroid.ui.common.LoadingDialog;
import com.example.medibookandroid.ui.doctor.DoctorMainActivity;
import com.example.medibookandroid.ui.patient.PatientMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;

    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        loadingDialog = new LoadingDialog();

        binding.btnLogin.setOnClickListener(v -> {
            if (validateInput()) {
                String email = binding.tilEmail.getEditText().getText().toString().trim();
                String password = binding.tilPassword.getEditText().getText().toString();

                viewModel.login(email, password);
            }
        });

        setupObservers();

        binding.tvSignUp.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_registerFragment));
        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        TextInputLayout tilEmailForgot = dialogView.findViewById(R.id.til_email_forgot);
        MaterialButton btnSend = dialogView.findViewById(R.id.btn_send_reset);
        TextView tvCancel = dialogView.findViewById(R.id.tv_cancel_forgot);
        btnSend.setOnClickListener(v -> {
            String email = tilEmailForgot.getEditText().getText().toString().trim();

            if (email.isEmpty()) {
                tilEmailForgot.setError("Vui lòng nhập email");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmailForgot.setError("Email không hợp lệ");
                return;
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Link đặt lại mật khẩu đã được gửi tới email của bạn!", Toast.LENGTH_LONG).show();
                            dialog.dismiss(); // Tắt dialog
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                            tilEmailForgot.setError("Lỗi: " + error);
                        }
                    });
        });

        tvCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean validateInput() {
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString();
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email không được để trống");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không hợp lệ");
            return false;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Mật khẩu không được để trống");
            return false;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        return true;
    }
    private void setupObservers() {
        // Observer cho trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                if (!loadingDialog.isAdded() && getChildFragmentManager() != null) {
                    loadingDialog.show(getChildFragmentManager(), "loading");
                }
            } else {
                if (loadingDialog.isAdded()) {
                    loadingDialog.dismiss();
                }
            }
        });

        // Observer cho kết quả đăng nhập (thành công)
        viewModel.getLoginUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Toast.makeText(getContext(),
                        "Đăng nhập thành công với vai trò: " + user.getRole(),
                        Toast.LENGTH_SHORT).show();

                // --- ⭐️ BỔ SUNG 1: LƯU ROLE VÀO SHAREDPREFS ---
                // Để sau này mở app lên (Auto Login) còn biết user là ai
                SharedPrefHelper prefHelper = new SharedPrefHelper(requireContext());
                prefHelper.putString("user_role", user.getRole());

                // --- ⭐️ BỔ SUNG 2: CẬP NHẬT FCM TOKEN LÊN FIREBASE ---
                // Xác định lưu vào bảng "doctors" hay "patients" dựa trên role
                String collectionName = "doctor".equalsIgnoreCase(user.getRole()) ? "doctors" : "patients";
                viewModel.updateFCMToken(collectionName);

                // --- 3. ĐIỀU HƯỚNG (Giữ nguyên logic của bạn) ---
                if ("patient".equalsIgnoreCase(user.getRole())) {
                    startActivity(new Intent(getContext(), PatientMainActivity.class));
                    requireActivity().finish();
                } else if ("doctor".equalsIgnoreCase(user.getRole())) {
                    startActivity(new Intent(getContext(), DoctorMainActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Role không xác định", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observer cho lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}