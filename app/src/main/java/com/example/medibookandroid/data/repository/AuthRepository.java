package com.example.medibookandroid.data.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.medibookandroid.data.model.User;
import com.example.medibookandroid.data.model.Patient; // ⭐️ THÊM IMPORT
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference; // ⭐️ THÊM IMPORT
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch; // ⭐️ THÊM IMPORT

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    // (Các LiveData khác giữ nguyên)
    public final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    public final MutableLiveData<User> loginUser = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * 1. Đăng ký người dùng (Không thay đổi)
     */
    public void register(String email, String password, String fullName, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 2. Gọi hàm saveUser (đã được sửa)
                            saveUserAndPatientToFirestore(user.getUid(), fullName, phone, email);
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        errorMessage.postValue("Đăng ký thất bại: " + task.getException().getMessage());
                        registerSuccess.postValue(false);
                    }
                });
    }

    /**
     * ⭐️ 2. HÀM NÀY ĐÃ ĐƯỢC SỬA LẠI (Tên mới + Dùng Batch) ⭐️
     * Lưu thông tin vào 2 collection 'users' và 'patients'
     */
    private void saveUserAndPatientToFirestore(String uid, String fullName, String phone, String email) {
        WriteBatch batch = db.batch();

        // 1. Chuẩn bị 'users' (Chỉ email và role)
        DocumentReference userRef = db.collection("users").document(uid);
        User user = new User(email, "patient"); // ⭐️ SỬA ⭐️
        batch.set(userRef, user);

        // 2. Chuẩn bị 'patients' (Chứa fullName, phone, và các trường null)
        DocumentReference patientRef = db.collection("patients").document(uid);
        Patient patient = new Patient(uid, fullName, phone, null, null, null, null); // Giữ nguyên
        batch.set(patientRef, patient);

        // 3. Thực thi batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User & Patient profiles created successfully!");
                    registerSuccess.postValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error writing batch documents", e);
                    errorMessage.postValue("Lỗi khi lưu hồ sơ: " + e.getMessage());
                    registerSuccess.postValue(false);
                });
    }

    /**
     * 3. Đăng nhập (Không thay đổi)
     */
    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        String uid = mAuth.getCurrentUser().getUid();
                        fetchUserRole(uid);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        errorMessage.postValue("Email hoặc mật khẩu không đúng");
                        loginUser.postValue(null);
                    }
                });
    }

    /**
     * 4. Lấy Role (Không thay đổi)
     */
    private void fetchUserRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        loginUser.postValue(user);
                    } else {
                        errorMessage.postValue("Không tìm thấy hồ sơ người dùng");
                        loginUser.postValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Lỗi lấy hồ sơ: " + e.getMessage());
                    loginUser.postValue(null);
                });
    }

    /**
     * 5. Đăng xuất người dùng khỏi Firebase Authentication
     */
    public void logout() {
        mAuth.signOut();
    }
}