package com.example.medibookandroid.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.databinding.ItemNotificationBinding;
// THÊM IMPORT
import com.example.medibookandroid.model.Notification;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    // SỬA: Dùng List<Notification>
    private final List<Notification> notifications;
    private final OnDeleteListener listener;

    // Interface cho sự kiện xóa
    public interface OnDeleteListener {
        void onDelete(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnDeleteListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        // SỬA: Lấy đối tượng Notification
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // SỬA: Bind từ đối tượng Notification
        public void bind(Notification notification, OnDeleteListener listener) {
            binding.tvNotificationTitle.setText(notification.getTitle());
            binding.tvNotificationBody.setText(notification.getBody());

            // Xử lý nút xóa (từ item_notification.xml)
            binding.ibDeleteNotification.setOnClickListener(v -> {
                listener.onDelete(notification);
            });
        }
    }
}
