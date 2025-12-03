package com.example.medibookandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cloudinary.android.MediaManager;
import com.example.medibookandroid.data.local.SharedPrefHelper;
import com.example.medibookandroid.ui.auth.AuthViewModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Track trạng thái init Cloudinary, chỉ init 1 lần
    private static boolean cloudinaryInitDone = false;
    private AuthViewModel authViewModel; // ⭐️ Khai báo ViewModel
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        Log.d("FirebaseCheck", "Firebase initialized: " + FirebaseApp.getInstance().getName());

        // 1. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. ⭐️ LOGIC QUAN TRỌNG: Auto-update FCM Token khi mở app
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            SharedPrefHelper prefHelper = new SharedPrefHelper(this);
            String role = prefHelper.getString("user_role"); // Lấy role đã lưu lúc login

            if (role != null) {
                String collectionName = role.equalsIgnoreCase("doctor") ? "doctors" : "patients";
                Log.d("MainActivity", "Auto-update Token for: " + collectionName);
                authViewModel.updateFCMToken(collectionName);
            } else {
                Log.w("MainActivity", "User logged in but role not found in Prefs.");
            }
        }
        if (!cloudinaryInitDone) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dqkhy4odr");
            MediaManager.init(this, config);
            cloudinaryInitDone = true;
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_nav_host_fragment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}