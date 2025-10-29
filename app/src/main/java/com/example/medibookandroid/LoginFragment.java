package com.example.medibookandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.medibookandroid.databinding.FragmentLoginBinding;
import com.example.medibookandroid.model.StorageRepository;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.tilPhone.getEditText().getText().toString().trim(); // Thêm trim()
            String password = binding.tilPassword.getEditText().getText().toString(); // Mật khẩu không cần trim

            // 1. Xóa lỗi cũ (nếu có)
            binding.tilPhone.setError(null);
            binding.tilPassword.setError(null);

            // 2. Validate input
            boolean isValid = true; // Biến kiểm tra

            if (phone.isEmpty()) {
                binding.tilPhone.setError("Số điện thoại không được để trống");
                isValid = false;
            }

            if (password.isEmpty()) {
                binding.tilPassword.setError("Mật khẩu không được để trống");
                isValid = false;
            }

            // 3. Chỉ thực hiện login nếu input hợp lệ
            if (isValid) {
                // Mock patient login
                if (phone.equals("0987654321") && password.equals("123456")) {
                    storageRepository.loginUser("PATIENT");
                    Intent intent = new Intent(getActivity(), PatientMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // getActivity().finish(); // Cân nhắc thêm dòng này nếu không muốn quay lại màn hình Login
                }
                // Mock doctor login
                else if (phone.equals("0989898989") && password.equals("doctor123")) {
                    storageRepository.loginUser("DOCTOR");
                    Intent intent = new Intent(getActivity(), DoctorMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // getActivity().finish();
                } else {
                    // Hiển thị lỗi chung nếu validate thành công nhưng sai thông tin
                    binding.tilPassword.setError("Số điện thoại hoặc mật khẩu không đúng");
                    // Hoặc dùng Toast như cũ nếu muốn
                    // Toast.makeText(getContext(), "Thông tin đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý nút quay lại trên Toolbar
        binding.toolbar.setNavigationOnClickListener(v ->
        {
            try {
                navController.navigate(R.id.action_loginFragment_to_welcomeFragment); // Giả sử ID action là đây
            } catch (IllegalArgumentException e) {
                // Xử lý nếu action chưa được định nghĩa
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi điều hướng đăng ký", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý click vào text "Đăng ký"
        binding.tvSignUp.setOnClickListener(v -> {
            // Điều hướng đến RegisterFragment (Đảm bảo action ID này tồn tại trong nav graph)
            try {
                navController.navigate(R.id.action_loginFragment_to_registerFragment); // Giả sử ID action là đây
            } catch (IllegalArgumentException e) {
                // Xử lý nếu action chưa được định nghĩa
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi điều hướng đăng ký", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý click vào text "Quên mật khẩu?" (Tạm thời hiển thị Toast)
        binding.tvForgotPassword.setOnClickListener(v -> {
            if(getContext() != null) {
                Toast.makeText(getContext(), "Chức năng Quên mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
            }
            // TODO: Điều hướng đến màn hình Quên mật khẩu
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
