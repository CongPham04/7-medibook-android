package com.example.medibookandroid.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.databinding.ItemNotificationBinding;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private final OnNotificationDeleteListener deleteListener;

    public interface OnNotificationDeleteListener {
        void onDeleteClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationDeleteListener deleteListener) {
        this.notifications = notifications;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, deleteListener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;
        private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Notification notification, final OnNotificationDeleteListener deleteListener) {
            binding.tvNotificationTitle.setText(notification.getTitle());
            binding.tvNotificationBody.setText(notification.getMessage());

            // (Hiển thị thêm thời gian nếu muốn)
            // if (notification.getCreatedAt() != null) {
            //    binding.tvNotificationTimestamp.setText(sdf.format(notification.getCreatedAt()));
            // }

            binding.ibDeleteNotification.setOnClickListener(v -> deleteListener.onDeleteClick(notification));
        }
    }
}
