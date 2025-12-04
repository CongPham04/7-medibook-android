package com.example.medibookandroid.ui.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.data.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final NotificationRepository repository;
    private final String currentUserId;

    // 1. LiveData danh sách thông báo
    private final MutableLiveData<List<Notification>> _notifications = new MutableLiveData<>();
    public LiveData<List<Notification>> getNotifications() {
        return _notifications;
    }

    // 2. LiveData số lượng chưa đọc (Tự động tính toán)
    public LiveData<Integer> getUnreadCount() {
        return Transformations.map(_notifications, notifications -> {
            if (notifications == null) return 0;
            int count = 0;
            for (Notification n : notifications) {
                if (!n.isRead()) count++;
            }
            return count;
        });
    }

    // 3. Trạng thái tải danh sách (Loading ban đầu)
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    // 4. ⭐️ ĐÃ THÊM LẠI: Trạng thái đang Xóa (để hiện Dialog quay quay)
    private final MutableLiveData<Boolean> _isDeleting = new MutableLiveData<>(false);
    public LiveData<Boolean> isDeleting() {
        return _isDeleting;
    }

    // 5. Toast thông báo
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> getToastMessage() {
        _toastMessage.setValue(null);
        return _toastMessage;
    }

    public NotificationViewModel() {
        repository = new NotificationRepository();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
            repository.listenForNotifications(currentUserId, _notifications, _isLoading);
        } else {
            currentUserId = null;
        }
    }

    // --- ACTIONS ---

    public void markNotificationAsRead(Notification notification) {
        if (notification.isRead() || currentUserId == null) return;

        repository.markNotificationAsRead(notification.getDocumentId(), success -> {
            if (!success) _toastMessage.setValue("Lỗi kết nối");
        });
    }

    public void markAllAsRead() {
        if (currentUserId == null) return;
        repository.markAllAsRead(currentUserId, success -> {
            if (success) _toastMessage.setValue("Đã đọc tất cả");
            else _toastMessage.setValue("Lỗi khi cập nhật");
        });
    }

    /**
     * ⭐️ ĐÃ SỬA: Nhận vào String notificationId thay vì Object Notification
     * Để khớp với lỗi "cannot be applied to (java.lang.String)"
     */
    public void deleteNotification(String notificationId) {
        if (currentUserId == null || notificationId == null) return;

        _isDeleting.setValue(true); // Bật loading xóa

        repository.deleteNotification(notificationId, success -> {
            _isDeleting.setValue(false); // Tắt loading xóa

            if (success) _toastMessage.setValue("Đã xóa thông báo");
            else _toastMessage.setValue("Lỗi khi xóa");
        });
    }

    public void deleteAllNotifications() {
        if (currentUserId == null) return;

        _isDeleting.setValue(true); // Bật loading xóa

        repository.deleteAllNotifications(currentUserId, success -> {
            _isDeleting.setValue(false); // Tắt loading xóa

            if (success) _toastMessage.setValue("Đã xóa tất cả");
            else _toastMessage.setValue("Lỗi khi xóa");
        });
    }
}