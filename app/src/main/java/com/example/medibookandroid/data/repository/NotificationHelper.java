package com.example.medibookandroid.data.repository;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.medibookandroid.MainActivity;
import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.doctor.DoctorMainActivity;
import com.example.medibookandroid.ui.patient.PatientMainActivity;
import com.example.medibookandroid.data.local.SharedPrefHelper; // Import Helper
public class NotificationHelper {

    // Đổi ID này nếu bạn muốn reset lại cài đặt âm thanh (quan trọng!)
    public static final String CHANNEL_ID = "medibook_booking_channel_v3";
    public static final String CHANNEL_NAME = "Thông báo đặt lịch";

    public static void showBookingNotification(Context context, String title, String message) {
        // 1. Kiểm tra Cài đặt của người dùng (SharedPreferences)
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        boolean isGeneralEnabled = prefs.getBoolean("general", true);
        boolean isSoundEnabled = prefs.getBoolean("sound", true);
        boolean isVibrateEnabled = prefs.getBoolean("vibrate", false);

        // Nếu người dùng tắt thông báo chung -> Không làm gì cả
        if (!isGeneralEnabled) return;

        // 2. Lấy đường dẫn file âm thanh custom
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification_sound);

        // Cập nhật
        SharedPrefHelper prefHelper = new SharedPrefHelper(context);
        String role = prefHelper.getString("user_role");

        Intent intent;
        if ("doctor".equals(role)) {
            intent = new Intent(context, DoctorMainActivity.class);
        } else {
            intent = new Intent(context, PatientMainActivity.class); // Mặc định là Patient
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        // 3. Tạo Notification Channel (Bắt buộc cho Android 8.0+)
        createNotificationChannel(context, soundUri, isSoundEnabled, isVibrateEnabled);

        // 5. Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell) // Đảm bảo bạn có icon này
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Logic cho các máy Android đời cũ (dưới 8.0)
        if (isSoundEnabled) {
            builder.setSound(soundUri);
        }
        if (isVibrateEnabled) {
            builder.setVibrate(new long[]{0, 500, 200, 500}); // Rung: nghỉ 0ms, rung 500ms...
        }

        // 6. Hiển thị thông báo
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            // ID = 1 (hoặc random) để hiển thị
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private static void createNotificationChannel(Context context, Uri soundUri, boolean isSoundEnabled, boolean isVibrateEnabled) {
        // Chỉ cần tạo channel trên Android 8.0 (API 26) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription("Thông báo trạng thái đặt lịch khám");

            // Cấu hình Âm thanh cho Channel
            if (isSoundEnabled) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(soundUri, audioAttributes);
            } else {
                channel.setSound(null, null); // Tắt tiếng
            }

            // Cấu hình Rung cho Channel
            channel.enableVibration(isVibrateEnabled);
            if (isVibrateEnabled) {
                channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            }

            // Đăng ký channel với hệ thống
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                // Lưu ý: Android không cho phép sửa âm thanh của Channel đã tồn tại.
                // Nếu muốn đổi setting, phải đổi CHANNEL_ID hoặc gỡ app cài lại.
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}