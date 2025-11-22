package com.example.medibookandroid;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.medibookandroid.ui.auth.AuthViewModel;
import com.example.medibookandroid.data.local.SharedPrefHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Kiểm tra Firebase đã khởi tạo chưa
        FirebaseApp.initializeApp(this);

        // 1. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        // --- ⭐️ BẮT ĐẦU LOGIC CẬP NHẬT TOKEN ---
        // Kiểm tra nếu user đã đăng nhập
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // 1. Khởi tạo Helper
            SharedPrefHelper prefHelper = new SharedPrefHelper(this);

            // 2. Lấy role đã lưu lúc đăng nhập
            String role = prefHelper.getString("user_role");

            // 3. Kiểm tra và cập nhật vào đúng bảng
            if (role != null) {
                String collectionName = role.equals("doctor") ? "doctors" : "patients";
                Log.d("MainActivity", "Auto-login: Detected role " + role + " -> Updating " + collectionName);
                authViewModel.updateFCMToken(collectionName);
            } else {
                // Trường hợp hiếm: Đã login firebase nhưng mất SharedPreferences (ví dụ xóa cache)
                // Ta có thể cập nhật thử cả 2 bảng để chắc ăn (Safety net)
                Log.w("MainActivity", "Lost role in Prefs. Trying safe update...");
                authViewModel.updateFCMToken("patients");
                authViewModel.updateFCMToken("doctors");
            }
        }
        // --- KẾT THÚC LOGIC ---

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_nav_host_fragment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}