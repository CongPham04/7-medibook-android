package com.example.medibookandroid.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medibookandroid.data.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

// ⭐️ THÊM IMPORT NÀY (Nếu bạn chưa có)
import com.example.medibookandroid.data.repository.OnOperationCompleteListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for handling all data operations related to Notifications.
 */
public class NotificationRepository {

    private static final String TAG = "NotificationRepository";
    private static final String NOTIFICATION_COLLECTION = "notifications";
    private FirebaseFirestore db;

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new notification in Firestore.
     * (Hàm này không cần callback vì nó "bắn và quên")
     */
    public void createNotification(Notification notification) {
        db.collection(NOTIFICATION_COLLECTION).add(notification)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Notification created with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating notification", e));
    }

    /**
     * Lắng nghe (real-time) các thông báo của người dùng.
     * Tự động sắp xếp và cập nhật LiveData.
     */
    public void listenForNotifications(String userId, MutableLiveData<List<Notification>> notificationsLiveData, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true); // Bật loading
        db.collection(NOTIFICATION_COLLECTION)
                .whereEqualTo("userId", userId)
                // Dùng addSnapshotListener thay vì .get()
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for notifications", e);
                        notificationsLiveData.setValue(new ArrayList<>()); // Trả list rỗng
                        loadingLiveData.setValue(false); // Tắt loading
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            notifications.add(document.toObject(Notification.class));
                        }
                        // Tự sắp xếp bằng Java (vì không dùng orderBy của Firestore)
                        notifications.sort((o1, o2) -> {
                            if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                                return o2.getCreatedAt().compareTo(o1.getCreatedAt()); // Mới nhất lên đầu
                            }
                            return 0;
                        });
                        notificationsLiveData.setValue(notifications);
                    }
                    loadingLiveData.setValue(false); // Tắt loading
                });
    }

    /**
     * Marks a specific notification as read.
     */
    public void markNotificationAsRead(String notificationId, OnOperationCompleteListener listener) {
        // ⭐️ SỬA LỖI: Tên trường là "read" (viết thường)
        db.collection(NOTIFICATION_COLLECTION).document(notificationId).update("read", true)
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    /**
     * Đánh dấu TẤT CẢ thông báo chưa đọc (isRead: false) thành đã đọc (isRead: true)
     */
    public void markAllAsRead(String userId, OnOperationCompleteListener listener) {
        db.collection(NOTIFICATION_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false) // ⭐️ SỬA LỖI: Tên trường là "read"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Không có gì để đánh dấu, coi như thành công
                        listener.onComplete(true);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // ⭐️ SỬA LỖI: Tên trường là "read"
                        batch.update(document.getReference(), "read", true);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> listener.onComplete(true))
                            .addOnFailureListener(e -> listener.onComplete(false));
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    /**
     * Deletes a notification from Firestore.
     */
    public void deleteNotification(String notificationId, OnOperationCompleteListener listener) {
        db.collection(NOTIFICATION_COLLECTION).document(notificationId).delete()
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    /**
     * Deletes all notifications for a specific user.
     */
    public void deleteAllNotifications(String userId, OnOperationCompleteListener listener) {
        db.collection(NOTIFICATION_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onComplete(true); // Không có gì để xóa, coi như thành công
                        return;
                    }
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> listener.onComplete(true))
                            .addOnFailureListener(e -> listener.onComplete(false));
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }
}