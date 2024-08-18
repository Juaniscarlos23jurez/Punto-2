package com.example.punto2.adaptadores;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.punto2.R;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationHelper {
    private static final String CHANNEL_ID = "your_channel_id";
    private static final int NOTIFICATION_ID = 1;  // Un ID único para cada notificación
    private Context context;
    private int badgeCount;
    public NotificationHelper(Context context, int badgeCount) {
        this.context = context;
        this.badgeCount = badgeCount;
    }
    public void createNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Crear un canal de notificación si es necesario (Android 8.0 o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Mi Canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Descripción del canal");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logot)  // Cambia esto al ícono de tu app
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);  // La notificación se elimina al hacer clic en ella

        Notification notification = builder.build();

        // Mostrar la notificación
        notificationManager.notify(NOTIFICATION_ID, notification);

        // Actualizar el badge con el número de notificaciones no leídas
        ShortcutBadger.applyCount(context, badgeCount);
    }
    public void clearBadge() {
        // Elimina el contador del badge
        ShortcutBadger.removeCount(context);
    }
}