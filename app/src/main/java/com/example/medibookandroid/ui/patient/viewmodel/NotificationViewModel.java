package com.example.medibookandroid.ui.patient.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.data.repository.NotificationRepository;
import com.example.medibookandroid.data.repository.OnOperationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationViewModel extends ViewModel {

    private NotificationRepository repository;
    private String currentUserId;

    // Trạng thái tải danh sách (cho ProgressBar)
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    // Trạng thái đang Xóa (cho LoadingDialog)
    private final MutableLiveData<Boolean> _isDeleting = new MutableLiveData<>(false);
    public LiveData<Boolean> isDeleting() {
        return _isDeleting;
    }

    // Dữ liệu (Danh sách thông báo)
    private final MutableLiveData<List<Notification>> _notifications = new MutableLiveData<>();
    public LiveData<List<Notification>> getNotifications() {
        return _notifications;
    }

    // Dữ liệu (Số lượng chưa đọc)
    private final MutableLiveData<Integer> _unreadCount = new MutableLiveData<>(0);
    public LiveData<Integer> getUnreadCount() {
        return _unreadCount;
    }

    // Thông báo (Toast)
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> getToastMessage() {
        _toastMessage.setValue(null); // Clear sau khi đọc
        return _toastMessage;
    }

    public NotificationViewModel() {
        repository = new NotificationRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            // Bắt đầu lắng nghe ngay khi ViewModel được tạo
            listenForNotifications(currentUserId);
        } else {
            currentUserId = "ERROR_NO_USER";
            Log.e("NotificationViewModel", "User is null, cannot fetch notifications.");
        }

        // Tự động đếm số lượng chưa đọc BẤT CỨ KHI NÀO danh sách thay đổi
        _notifications.observeForever(notifications -> {
            if (notifications == null) {
                _unreadCount.setValue(0);
                return;
            }
            // Đếm các thông báo có isRead = false
            long count = notifications.stream().filter(n -> !n.isRead()).count();
            _unreadCount.setValue((int) count);
        });
    }

    /**
     * ⭐️ SỬA: Bắt đầu lắng nghe (chỉ gọi 1 lần)
     */
    private void listenForNotifications(String userId) {
        if (userId.equals("ERROR_NO_USER")) return;
        repository.listenForNotifications(userId, _notifications, _isLoading);
    }

    /**
     * Đánh dấu tất cả là đã đọc
     * (Sẽ được gọi bởi PatientNotificationsFragment khi mở lên)
     */
    public void markAllAsRead() {
        if (currentUserId.equals("ERROR_NO_USER")) return;

        // Chỉ gọi nếu thực sự có thông báo chưa đọc
        if (_unreadCount.getValue() != null && _unreadCount.getValue() > 0) {
            repository.markAllAsRead(currentUserId, success -> {
                if (success) {
                    Log.d("NotificationViewModel", "Đã đánh dấu tất cả là đã đọc.");
                } else {
                    Log.e("NotificationViewModel", "Lỗi khi đánh dấu đã đọc.");
                }
                // Snapshot listener sẽ tự động cập nhật list và count
            });
        }
    }

    /**
     * Đánh dấu MỘT thông báo là đã đọc
     * (Được gọi khi người dùng click vào 1 item)
     */
    public void markNotificationAsRead(Notification notification) {
        if (notification.isRead()) return; // Đã đọc rồi thì không làm gì
        if (notification.getDocumentId() == null) {
            Log.e("NotificationViewModel", "Cannot mark as read, Document ID is null");
            return;
        }

        repository.markNotificationAsRead(notification.getDocumentId(), success -> {
            if (success) {
                Log.d("NotificationViewModel", "Đã đánh dấu (1) là đã đọc.");
            } else {
                Log.e("NotificationViewModel", "Lỗi khi đánh dấu (1) là đã đọc.");
            }
            // Snapshot listener sẽ tự động cập nhật
        });
    }

    /**
     * Xóa một thông báo
     */
    public void deleteNotification(String notificationId) {
        _isDeleting.setValue(true); // Bật dialog loading
        repository.deleteNotification(notificationId, success -> {
            _isDeleting.setValue(false); // Tắt dialog loading
            if (success) {
                _toastMessage.setValue("Đã xóa thông báo");
            } else {
                _toastMessage.setValue("Lỗi khi xóa thông báo");
            }
            // Snapshot listener sẽ tự động tải lại
        });
    }

    /**
     * Xóa tất cả thông báo
     */
    public void deleteAllNotifications() {
        if (currentUserId.equals("ERROR_NO_USER")) return;
        _isDeleting.setValue(true); // Bật dialog loading
        repository.deleteAllNotifications(currentUserId, success -> {
            _isDeleting.setValue(false); // Tắt dialog loading
            if (success) {
                _toastMessage.setValue("Đã xóa tất cả thông báo");
            } else {
                _toastMessage.setValue("Lỗi khi xóa");
            }
            // Snapshot listener sẽ tự động tải lại
        });
    }
}