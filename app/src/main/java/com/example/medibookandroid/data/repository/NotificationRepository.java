package com.example.medibookandroid.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.medibookandroid.data.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private static final String TAG = "NotificationRepository";
    private static final String NOTIFICATION_COLLECTION = "notifications";
    private final FirebaseFirestore db;

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // --- REALTIME LISTENER ---
    public void listenForNotifications(String userId, MutableLiveData<List<Notification>> notificationsLiveData, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true);

        db.collection(NOTIFICATION_COLLECTION)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        loadingLiveData.setValue(false);
                        return;
                    }

                    List<Notification> notifications = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Notification notification = doc.toObject(Notification.class);
                            notification.setDocumentId(doc.getId()); // ⭐️ QUAN TRỌNG: Lưu ID để sau này còn xóa/update
                            notifications.add(notification);
                        }

                        // Sắp xếp: Mới nhất lên đầu
                        notifications.sort((o1, o2) -> {
                            if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
                            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                        });
                    }

                    notificationsLiveData.setValue(notifications);
                    loadingLiveData.setValue(false);
                });
    }

    // --- CÁC HÀM CRUD CƠ BẢN ---

    public void createNotification(Notification notification) {
        db.collection(NOTIFICATION_COLLECTION).add(notification);
    }

    public void markNotificationAsRead(String documentId, OnOperationCompleteListener listener) {
        if (documentId == null) return;
        db.collection(NOTIFICATION_COLLECTION).document(documentId)
                .update("read", true) // Đảm bảo trường trên Firestore là "read"
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public void markAllAsRead(String userId, OnOperationCompleteListener listener) {
        db.collection(NOTIFICATION_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        listener.onComplete(true);
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        batch.update(doc.getReference(), "read", true);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> listener.onComplete(true))
                            .addOnFailureListener(e -> listener.onComplete(false));
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public void deleteNotification(String documentId, OnOperationCompleteListener listener) {
        if (documentId == null) return;
        db.collection(NOTIFICATION_COLLECTION).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public void deleteAllNotifications(String userId, OnOperationCompleteListener listener) {
        db.collection(NOTIFICATION_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> listener.onComplete(true))
                            .addOnFailureListener(e -> listener.onComplete(false));
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }
}