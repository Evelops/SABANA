package com.example.sabana.fcmsendservice;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.sabana.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FcmTokenService extends FirebaseMessagingService {
    private final static String CHANNEL_ID = "CHAT";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        System.out.println("새메시지 도착");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        NotificationCompat.Builder builder = createNotification(title, body);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "채팅" , importance);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel);
        mNotificationManager.notify(1, builder.build());
    }

    @SuppressLint("WrongConstant")
    private NotificationCompat.Builder createNotification(String title, String body){
      //  Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher/*스와이프 전 아이콘*/)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }
}