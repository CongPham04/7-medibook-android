package com.example.medibookandroid;

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
import com.example.medibookandroid.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // --- Bắt đầu thay đổi ---

        // 1. Vô hiệu hóa nút Đăng ký ban đầu (cũng được set trong XML)
        binding.btnRegister.setEnabled(false);

        // 2. Thêm listener cho CheckBox
        binding.cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Bật/tắt nút Đăng ký dựa trên trạng thái CheckBox
            binding.btnRegister.setEnabled(isChecked);
        });

        // --- Kết thúc thay đổi ---

        binding.btnRegister.setOnClickListener(v -> {
            // Lấy dữ liệu từ các trường nhập liệu
            String fullName = binding.tilFullName.getEditText().getText().toString().trim();
            String phone = binding.tilPhone.getEditText().getText().toString().trim();
            String password = binding.tilPassword.getEditText().getText().toString(); // Mật khẩu không cần trim

            // 1. Xóa lỗi cũ (nếu có)
            binding.tilFullName.setError(null);
            binding.tilPhone.setError(null);
            binding.tilPassword.setError(null);

            // 2. Validate input
            boolean isValid = true; // Biến kiểm tra

            if (fullName.isEmpty()) {
                binding.tilFullName.setError("Họ tên không được để trống");
                isValid = false;
            }

            if (phone.isEmpty()) {
                binding.tilPhone.setError("Số điện thoại không được để trống");
                isValid = false;
            }
            // TODO: Thêm các kiểm tra khác cho số điện thoại nếu cần (định dạng, độ dài...)

            if (password.isEmpty()) {
                binding.tilPassword.setError("Mật khẩu không được để trống");
                isValid = false;
            }
            // TODO: Thêm các kiểm tra khác cho mật khẩu nếu cần (độ dài tối thiểu...)


            // 3. Chỉ thực hiện đăng ký nếu input hợp lệ
            if (isValid) {
                // TODO: Thêm logic đăng ký thực tế ở đây (gọi API, lưu vào Firebase...)

                // Hiển thị thông báo thành công (Demo)
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                }
                navController.popBackStack(); // Quay lại màn hình trước đó (Login)
            }
        });

        // Xử lý nút quay lại trên Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());

        // Xử lý click vào text "Đăng nhập"
        binding.tvLogin.setOnClickListener(v -> {
            // Điều hướng đến RegisterFragment (Đảm bảo action ID này tồn tại trong nav graph)
            try {
                navController.navigate(R.id.action_registerFragment_to_loginFragment); // Giả sử ID action là đây
            } catch (IllegalArgumentException e) {
                // Xử lý nếu action chưa được định nghĩa
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi điều hướng đăng ký", Toast.LENGTH_SHORT).show();
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
