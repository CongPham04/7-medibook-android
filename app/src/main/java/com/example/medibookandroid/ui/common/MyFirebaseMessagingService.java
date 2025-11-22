package com.example.medibookandroid.ui.common; // Nên để trong package service

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.medibookandroid.data.repository.NotificationHelper; // Import Helper của bạn
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Hàm này được gọi khi có tin nhắn đến từ FCM
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 1. Xử lý tin nhắn chứa Data (Thường dùng khi Backend gửi)
        // Dạng này giúp App xử lý ngầm được ngay cả khi User đang dùng App
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // 2. Xử lý tin nhắn chứa Notification (Thường dùng khi test từ Firebase Console)
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // ⭐️ QUAN TRỌNG: Gọi lại Helper để dùng chung logic Âm thanh/Rung/Channel
            NotificationHelper.showBookingNotification(getApplicationContext(), title, body);
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title"); // Key do Backend quy định
        String body = data.get("body");

        // Nếu không có title/body thì đặt mặc định
        if (title == null) title = "Thông báo mới";
        if (body == null) body = "Bạn có tin nhắn mới từ MediBook";

        // Gọi Helper hiển thị
        NotificationHelper.showBookingNotification(getApplicationContext(), title, body);
    }

    /**
     * Hàm này được gọi khi Token thay đổi (ví dụ cài lại app)
     * Cần cập nhật Token mới lên Firestore để server biết đường gửi
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            // Lưu token vào document của user hiện tại
            // Lưu ý: Bạn cần xác định collection là "patients" hay "doctors"
            // Ở đây ví dụ lưu vào patients
            FirebaseFirestore.getInstance().collection("patients")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnFailureListener(e -> Log.e(TAG, "Không thể cập nhật Token lên Server", e));
        }
    }
}