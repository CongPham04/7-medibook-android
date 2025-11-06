package com.example.medibookandroid.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medibookandroid.data.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

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
     * Fetches all notifications for a specific user.
     *
     * @param userId The ID of the user.
     * @return LiveData containing the list of notifications, or null on failure.
     */
    public LiveData<List<Notification>> getNotificationsForUser(String userId) {
        MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
        db.collection(NOTIFICATION_COLLECTION)
                .whereEqualTo("userId", userId)
                // ⭐️ SỬA: Xóa .orderBy("createdAt", ...) để tránh lỗi Index
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        notifications.add(document.toObject(Notification.class));
                    }
                    // ⭐️ THÊM: Tự sắp xếp bằng Java
                    notifications.sort((o1, o2) -> {
                        if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                            return o2.getCreatedAt().compareTo(o1.getCreatedAt()); // Mới nhất lên đầu
                        }
                        return 0;
                    });
                    notificationsLiveData.setValue(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting notifications for user", e);
                    notificationsLiveData.setValue(null);
                });
        return notificationsLiveData;
    }

    /**
     * Marks a specific notification as read.
     */
    public void markNotificationAsRead(String notificationId, OnOperationCompleteListener listener) {
        db.collection(NOTIFICATION_COLLECTION).document(notificationId).update("read", true)
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
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
