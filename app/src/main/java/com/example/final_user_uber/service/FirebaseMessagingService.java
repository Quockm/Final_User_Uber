package com.example.final_user_uber.service;

import androidx.annotation.NonNull;

import com.example.final_user_uber.Common.Common;
import com.example.final_user_uber.model.EvenBus.DriverRequestReceived;
import com.example.final_user_uber.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    //generate Token to catch exceptions
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            UserUtils.updateToken(this, s);
        }
    }

    // Recived Messsage thorugh getting token
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dataRev = remoteMessage.getData();
        if (dataRev != null) {
            {
                if (dataRev.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_TITLE)) {
                    EventBus.getDefault().postSticky(new DriverRequestReceived(
                            dataRev.get(Common.RIDER_KEY),
                            dataRev.get(Common.RIDER_PICKUP_LOCATION)

                    ));
                }
            }
        }
        Common.ShowNofication(this, new Random().nextInt(),
                dataRev.get(Common.NOTI_TITLE),
                dataRev.get(Common.NOTI_CONTENT),
                null);
    }
}