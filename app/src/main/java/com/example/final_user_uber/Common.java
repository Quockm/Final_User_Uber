package com.example.final_user_uber;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.final_user_uber.model.DriverInfoModel;

public class Common {
        public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
        public static final String DRIVER_LOCATION_REFERENCE = "DriverLocation";
        public static final String TOKEN_REFERENCE = "Token";
        public static final String NOTI_TITLE = "Title";
        public static final String NOTI_CONTENT = "body" ;

        public static DriverInfoModel currentUser;

        public static String buildWelcomeMessage() {
            if (Common.currentUser != null) {
                return new StringBuilder("Welcome ")
                        .append(Common.currentUser.getFisrtnasme())
                        .append(" ")
                        .append(Common.currentUser.getLastname()).toString();
            } else {
                return "";
            }
        }

        public static void ShowNofication(Context context, int id, String title, String body, Intent intent) {
            PendingIntent pendingIntent = null;
            if(intent != null)
                pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            String NOFICATION_CHANNEL_ID = "DemoUber";
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
            {
                //Setting Vibrate and lights when notification come out
                NotificationChannel notificationChannel = new NotificationChannel(NOFICATION_CHANNEL_ID,
                        "Uber",NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("Uber");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
                notificationChannel.enableVibration(true);

                notificationManager.createNotificationChannel(notificationChannel);
            }
            // setting style when notification come out
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_directions_car_24));
            if(pendingIntent != null)
            {
                builder.setContentIntent(pendingIntent);
            }
            Notification notification = builder.build();
            notificationManager.notify(id,notification);
        }
}
