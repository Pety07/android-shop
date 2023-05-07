package com.example.product_ordering;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class MyNotificationManager {
    private static final String CHANNEL_ID = "product_ordering_app_notification_channel";
    private final int NOTIFICATION_ID = 0;

    private NotificationManager mNotificationManager;
    private Context mContext;

    public MyNotificationManager(Context context) {
        this.mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Product Ordering értesítés",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Értesités!!!");
        this.mNotificationManager.createNotificationChannel(channel);
    }

    public void send(String message) {
        Intent intent = new Intent(mContext, ProductListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("Product Ordering értesítés")
                .setContentText(message)
                .setSmallIcon(R.drawable.cart_icon)
                .setContentIntent(pendingIntent);
        this.mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void cancel() {
        this.mNotificationManager.cancel(NOTIFICATION_ID);
    }

}
