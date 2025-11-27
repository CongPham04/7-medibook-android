package com.example.medibookandroid;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Track trạng thái init Cloudinary, chỉ init 1 lần
    private static boolean cloudinaryInitDone = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Kiểm tra Firebase đã khởi tạo chưa
        FirebaseApp.initializeApp(this);
        Log.d("FirebaseCheck", "Firebase initialized: " + FirebaseApp.getInstance().getName());
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
    }
}