package com.example.badmintonbooking.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.badmintonbooking.R;
import com.example.badmintonbooking.ui.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";
    private static final String CHANNEL_ID = "badminton_booking_channel";

    // Hàm này chạy khi thiết bị nhận được một thông báo từ Firebase
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Kiểm tra xem tin nhắn có chứa payload thông báo không
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Gọi hàm hiển thị thông báo lên màn hình điện thoại
            sendNotification(title, body);
        }
    }

    // Hàm này chạy khi ứng dụng được cài đặt mới hoặc token bị thay đổi
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Token mới của thiết bị: " + token);
        // TODO: Sau này bạn có thể lưu token này lên Firebase Realtime Database của User
        // để gửi thông báo đích danh đến chính user đó.
    }

    // Hàm xây dựng và hiển thị thông báo chuẩn Material Design 3
    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 (Oreo) trở lên yêu cầu phải có Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo Đặt Sân",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo các hoạt động đặt sân và ghép kèo");
            notificationManager.createNotificationChannel(channel);
        }

        // Thiết kế giao diện thông báo đồng bộ với Design System (Xanh lá Primary #16A34A)
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Bạn có thể thay bằng icon quả cầu lông của app
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#16A34A")) // Màu xanh lá chủ đạo
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Hiển thị thông báo (Số 0 là ID của thông báo, bạn có thể tạo số ngẫu nhiên nếu muốn hiện nhiều thông báo cùng lúc)
        notificationManager.notify(0, notificationBuilder.build());
    }
}
