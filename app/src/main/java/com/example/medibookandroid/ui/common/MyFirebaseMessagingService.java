package com.example.medibookandroid.ui.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.medibookandroid.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "doctor_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        SharedPreferences prefs = getSharedPreferences("notification_settings", MODE_PRIVATE);

        boolean isGeneralEnabled = prefs.getBoolean("general", true);
        boolean soundEnabled = prefs.getBoolean("sound", true);
        boolean vibrateEnabled = prefs.getBoolean("vibrate", false);

        if (!isGeneralEnabled) {
            return; // tắt hết thông báo
        }

        createNotificationChannel(soundEnabled, vibrateEnabled);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setAutoCancel(true);

        if (soundEnabled) {
            builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_sound));
        }

        if (vibrateEnabled) {
            builder.setVibrate(new long[]{0, 300, 200, 300});
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private void createNotificationChannel(boolean sound, boolean vibrate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo bác sĩ",
                    NotificationManager.IMPORTANCE_HIGH
            );

            if (sound) {
                Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_sound);
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(soundUri, attrs);
            } else {
                channel.setSound(null, null);
            }

            channel.enableVibration(vibrate);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }
}
