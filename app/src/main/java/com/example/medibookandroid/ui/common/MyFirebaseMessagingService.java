package com.example.medibookandroid.ui.common; // Nên để trong package service

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.medibookandroid.data.local.SharedPrefHelper;
import com.example.medibookandroid.data.repository.NotificationHelper; // Import Helper của bạn
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
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
        Log.d("FCM", "Token refreshed: " + token);

        // 1. Kiểm tra xem người dùng đã đăng nhập chưa
        String uid = FirebaseAuth.getInstance().getUid();

        // --- TRƯỜNG HỢP A: CHƯA ĐĂNG NHẬP ---
        if (uid == null) {
            // Chỉ Log báo hiệu, không làm gì cả để tránh lỗi Permission
            // Token này sẽ được cập nhật SAU khi user đăng nhập (nhờ code trong LoginFragment)
            Log.w("FCM", "User chưa đăng nhập, bỏ qua việc lưu token lên Server.");
            return;
        }

        // --- TRƯỜNG HỢP B: ĐÃ ĐĂNG NHẬP (VD: Token thay đổi khi đang dùng app) ---
        SharedPrefHelper pref = new SharedPrefHelper(getApplicationContext());
        String role = pref.getString("user_role");

        // Nếu vì lý do nào đó mà mất Role (VD: Xóa cache), ta dừng lại để an toàn
        if (role == null) {
            Log.w("FCM", "Không tìm thấy User Role, không thể xác định bảng để lưu.");
            return;
        }

        // Xác định bảng (Collection)
        String collection = "doctor".equalsIgnoreCase(role) ? "doctors" : "patients";

        // Chuẩn bị dữ liệu
        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", token);

        // Cập nhật lên Firestore
        FirebaseFirestore.getInstance()
                .collection(collection)
                .document(uid)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Đã cập nhật Token mới lên Server thành công."))
                .addOnFailureListener(e -> Log.e("FCM", "Lỗi cập nhật token lên Server", e));
    }

}