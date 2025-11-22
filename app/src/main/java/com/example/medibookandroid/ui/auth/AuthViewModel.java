package com.example.medibookandroid.ui.auth;

// KHÔNG cần import Activity
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medibookandroid.data.model.User;
import com.example.medibookandroid.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }
    public AuthViewModel() {
        repository = new AuthRepository();
        repository.loginUser.observeForever(user -> _isLoading.setValue(false));
        repository.registerSuccess.observeForever(success -> _isLoading.setValue(false));
        repository.errorMessage.observeForever(error -> _isLoading.setValue(false));
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
        _isLoading.setValue(true); // ⭐️ Bật loading
        repository.register(email, password, fullName, phone);
    }

    /**
     * Hàm gọi đăng nhập
     */
    public void login(String email, String password) {
        _isLoading.setValue(true); // ⭐️ Bật loading
        repository.login(email, password);
    }

    /**
     * Hàm gọi đăng xuất
     */
    public void logout() {
        repository.logout();
    }
    /**
     * ⭐️ THÊM MỚI: Hàm lấy và cập nhật FCM Token
     * @param collectionName Tên bảng ("patients" hoặc "doctors")
     */
    public void updateFCMToken(String collectionName) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Lỗi lấy FCM token", task.getException());
                        return;
                    }

                    // 1. Lấy Token mới
                    String token = task.getResult();
                    Log.d("FCM", "Token: " + token);

                    // 2. Lưu vào Firestore
                    String userId = FirebaseAuth.getInstance().getUid();
                    if (userId != null) {
                        Map<String, Object> tokenData = new HashMap<>();
                        tokenData.put("fcmToken", token);

                        FirebaseFirestore.getInstance()
                                .collection(collectionName) // "patients" hoặc "doctors"
                                .document(userId)
                                .set(tokenData, SetOptions.merge()) // Dùng merge để không ghi đè các trường khác
                                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token đã lưu vào Firestore"))
                                .addOnFailureListener(e -> Log.e("FCM", "Lỗi lưu token", e));
                    }
                });
    }
}