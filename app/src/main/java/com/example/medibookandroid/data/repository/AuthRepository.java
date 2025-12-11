package com.example.medibookandroid.data.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.medibookandroid.data.model.User;
import com.example.medibookandroid.data.model.Patient; // ⭐️ THÊM IMPORT
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
    // Thêm LiveData để báo kết quả xóa
    public final MutableLiveData<Boolean> deleteAccountSuccess = new MutableLiveData<>();

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

    /**
     * ⭐️ CHỨC NĂNG MỚI: Xóa tài khoản
     * @param password Mật khẩu để xác thực lại trước khi xóa
     * @param role Vai trò để biết xóa ở bảng patients hay doctors
     */
    public void deleteAccount(String password, String role) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            errorMessage.postValue("Người dùng không hợp lệ");
            deleteAccountSuccess.postValue(false);
            return;
        }

        // 1. Tạo Credential từ email và mật khẩu nhập vào
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        // 2. Xác thực lại (Re-authenticate)
        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                Log.d(TAG, "Re-authenticated successfully. Deleting data...");
                deleteFirestoreDataAndUser(user, role);
            } else {
                Log.e(TAG, "Re-authentication failed", reauthTask.getException());
                errorMessage.postValue("Mật khẩu không đúng, vui lòng thử lại.");
                deleteAccountSuccess.postValue(false);
            }
        });
    }

    private void deleteFirestoreDataAndUser(FirebaseUser user, String role) {
        String uid = user.getUid();
        WriteBatch batch = db.batch();

        // 3. Chuẩn bị xóa dữ liệu Firestore
        // Xóa trong collection 'users'
        DocumentReference userRef = db.collection("users").document(uid);
        batch.delete(userRef);

        // Xóa trong collection profile ('patients' hoặc 'doctors')
        String collectionName = "doctor".equalsIgnoreCase(role) ? "doctors" : "patients";
        DocumentReference profileRef = db.collection(collectionName).document(uid);
        batch.delete(profileRef);

        // 4. Thực thi xóa Firestore
        batch.commit().addOnCompleteListener(batchTask -> {
            if (batchTask.isSuccessful()) {
                Log.d(TAG, "Firestore data deleted. Deleting Auth user...");

                // 5. Cuối cùng: Xóa User khỏi Firebase Auth
                user.delete().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        Log.d(TAG, "User account deleted.");
                        deleteAccountSuccess.postValue(true);
                    } else {
                        Log.e(TAG, "Failed to delete auth user", deleteTask.getException());
                        errorMessage.postValue("Lỗi xóa tài khoản Auth: " + deleteTask.getException().getMessage());
                        deleteAccountSuccess.postValue(false);
                    }
                });
            } else {
                Log.e(TAG, "Failed to delete Firestore data", batchTask.getException());
                errorMessage.postValue("Lỗi xóa dữ liệu: " + batchTask.getException().getMessage());
                deleteAccountSuccess.postValue(false);
            }
        });
    }
}