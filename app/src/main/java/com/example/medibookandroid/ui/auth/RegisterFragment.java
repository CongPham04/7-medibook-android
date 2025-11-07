package com.example.medibookandroid.ui.auth;

import android.os.Bundle;
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

// ⭐️ THÊM: Import thư viện Regex
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;
    private LoadingDialog loadingDialog;

    // ⭐️ THÊM: Định nghĩa các mẫu (Pattern) Regex cho mật khẩu
    // (?=.*[A-Z]) -> Yêu cầu ít nhất 1 chữ hoa
    private static final Pattern UPPER_CASE_PATTERN = Pattern.compile(".*[A-Z].*");
    // (?=.*[!@#$%^&*()_]) -> Yêu cầu ít nhất 1 ký tự đặc biệt
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
                String name = binding.tilFullName.getEditText().getText().toString().trim();
                String email = binding.tilEmail.getEditText().getText().toString().trim();
                String phone = binding.tilPhone.getEditText().getText().toString().trim();
                String password = binding.tilPassword.getEditText().getText().toString();

                String formattedPhone = formatPhoneNumber(phone);

                viewModel.register(email, password, name, formattedPhone);
            }
        });

        setupObservers();

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
        binding.tvLogin.setOnClickListener(v -> navController.navigate(R.id.action_registerFragment_to_loginFragment));
    }

    /**
     * Cài đặt các listener quan sát LiveData từ ViewModel
     */
    private void setupObservers() {
        // 1. Quan sát trạng thái Loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                if (!loadingDialog.isAdded() && getChildFragmentManager() != null) {
                    loadingDialog.show(getChildFragmentManager(), "loading_register");
                }
            } else {
                if (loadingDialog.isAdded()) {
                    loadingDialog.dismiss();
                }
            }
        });

        // 2. Quan sát kết quả Đăng ký
        viewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return; // Bỏ qua giá trị null (reset)

            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                navController.navigate(R.id.action_registerFragment_to_loginFragment);
            }
            // Không cần 'else' vì 'errorMessage' sẽ xử lý lỗi
        });

        // 3. Quan sát lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ⭐️ SỬA: Kiểm tra toàn bộ input (phiên bản nâng cao) ⭐️
     */
    private boolean validateInput() {
        // Reset lỗi
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        boolean valid = true;

        String name = binding.tilFullName.getEditText().getText().toString().trim();
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        String phone = binding.tilPhone.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString();

        // 1. Kiểm tra Tên
        if (name.isEmpty()) {
            binding.tilFullName.setError("Họ tên không được để trống");
            valid = false;
        } else if (name.length() < 5) {
            binding.tilFullName.setError("Họ tên phải dài ít nhất 5 ký tự");
            valid = false;
        } else if (!Character.isLetter(name.charAt(0)) || !Character.isUpperCase(name.charAt(0))) {
            // Kiểm tra ký tự đầu tiên có phải là chữ cái VÀ có viết hoa không
            binding.tilFullName.setError("Họ tên phải bắt đầu bằng một chữ cái viết hoa");
            valid = false;
        }

        // 2. Kiểm tra Email
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email không được để trống");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không hợp lệ");
            valid = false;
        }

        // 3. Kiểm tra SĐT
        if (phone.isEmpty()) {
            binding.tilPhone.setError("Số điện thoại không được để trống");
            valid = false;
        } else if (formatPhoneNumber(phone) == null) {
            binding.tilPhone.setError("Số điện thoại không hợp lệ (VD: 0912345678)");
            valid = false;
        }

        // 4. Kiểm tra Mật khẩu
        if (password.isEmpty()) {
            binding.tilPassword.setError("Mật khẩu không được để trống");
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            valid = false;
        } else if (!UPPER_CASE_PATTERN.matcher(password).matches()) {
            binding.tilPassword.setError("Mật khẩu phải chứa ít nhất 1 chữ in hoa");
            valid = false;
        } else if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            binding.tilPassword.setError("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (ví dụ: !@#$%)");
            valid = false;
        }

        return valid;
    }

    /**
     * Hàm chuẩn hóa SĐT
     */
    private String formatPhoneNumber(String phone) {
        phone = phone.replaceAll("\\s+", "").replaceAll("[^0-9]", "");
        if (phone.startsWith("0") && phone.length() == 10) return "+84" + phone.substring(1);
        if (phone.startsWith("84") && phone.length() == 11) return "+" + phone;
        if (phone.startsWith("+84") && phone.length() == 12) return phone;
        return null; // Không hợp lệ
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}