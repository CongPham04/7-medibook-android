package com.example.medibookandroid.ui.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.data.repository.NotificationRepository;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private NotificationRepository notificationRepository;

    // ⭐️ THÊM: LiveData cho trạng thái (Xóa, Đọc)
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteStatus = new MutableLiveData<>();

    public NotificationViewModel() {
        notificationRepository = new NotificationRepository();
    }

    // --- Getters ---
    public LiveData<String> getToastMessage() {
        return toastMessage;
    }
    public LiveData<Boolean> getDeleteStatus() {
        deleteStatus.setValue(null); // Reset
        return deleteStatus;
    }

    public LiveData<List<Notification>> getNotificationsForUser(String userId) {
        return notificationRepository.getNotificationsForUser(userId);
    }

    // ⭐️ SỬA: Cập nhật các hàm
    public void markNotificationAsRead(String notificationId) {
        notificationRepository.markNotificationAsRead(notificationId, success -> {
            // (Không cần làm gì, chỉ đánh dấu đã đọc)
        });
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.deleteNotification(notificationId, success -> {
            if (success) {
                toastMessage.setValue("Đã xóa thông báo");
                deleteStatus.postValue(true); // Báo cho View biết để tải lại
            } else {
                toastMessage.setValue("Lỗi khi xóa");
                deleteStatus.postValue(false);
            }
        });
    }

    public void deleteAllNotifications(String userId) {
        notificationRepository.deleteAllNotifications(userId, success -> {
            if (success) {
                toastMessage.setValue("Đã xóa tất cả thông báo");
                deleteStatus.postValue(true); // Báo cho View biết để tải lại
            } else {
                toastMessage.setValue("Lỗi khi xóa");
                deleteStatus.postValue(false);
            }
        });
    }
}
