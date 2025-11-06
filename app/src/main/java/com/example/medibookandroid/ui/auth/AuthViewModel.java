package com.example.medibookandroid.ui.auth;

// KHÔNG cần import Activity
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.medibookandroid.data.model.User;
import com.example.medibookandroid.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;

    public AuthViewModel() {
        repository = new AuthRepository();
    }

    // LiveData cho Đăng Ký
    public LiveData<Boolean> getRegisterSuccess() {
        return repository.registerSuccess;
    }

    // LiveData cho Đăng Nhập
    public LiveData<User> getLoginUser() {
        return repository.loginUser;
    }

    // LiveData cho lỗi
    public LiveData<String> getErrorMessage() {
        return repository.errorMessage;
    }

    /**
     * Hàm gọi đăng ký
     */
    public void register(String email, String password, String fullName, String phone) {
        repository.register(email, password, fullName, phone);
    }

    /**
     * Hàm gọi đăng nhập
     */
    public void login(String email, String password) {
        repository.login(email, password);
    }

    /**
     * Hàm gọi đăng xuất
     */
    public void logout() {
        repository.logout();
    }
}