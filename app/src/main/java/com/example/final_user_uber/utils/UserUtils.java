package com.example.final_user_uber.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.final_user_uber.Common.Common;
import com.example.final_user_uber.R;
import com.example.final_user_uber.Remote.IFCMService;
import com.example.final_user_uber.Remote.RetrofitFCMClient;
import com.example.final_user_uber.model.EvenBus.NotifytoRiderEvent;
import com.example.final_user_uber.model.FCMSendData;
import com.example.final_user_uber.model.TokenModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserUtils {
    public static void updateUser(View view, Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> Snackbar.make(view, "Update information Successfully!", Snackbar.LENGTH_SHORT).show());
    }

    public static void updateToken(Context context, String token) {
        TokenModel tokenModel = new TokenModel(token);
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> {

                });

    }

    public static void sendDeclineRequest(View view, Context context, String key) {


        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);


        //get token
        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_RIDER_REFERENCE)
                //.getReference(Common.TOKEN_REFERENCE)
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                            Map<String, String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_DECLINE);
                            notificationData.put(Common.NOTI_CONTENT, "This message represent for driver decline user request");
                            notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());
                            notificationData.put(Common.RIDER_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());


                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        if (fcmResponse.getSuccess() == 0) {
                                            compositeDisposable.clear();
                                            Snackbar.make(view, context.getString(R.string.decline_failed)
                                                    , Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Snackbar.make(view, context.getString(R.string.decline_succesed)
                                                    , Snackbar.LENGTH_LONG).show();
                                        }

                                    }, throwable -> {
                                        compositeDisposable.clear();
                                        Snackbar.make(view, throwable.getMessage()
                                                , Snackbar.LENGTH_LONG).show();

                                    }));


                        } else {
                            compositeDisposable.clear();
                            Snackbar.make(view, context.getString(R.string.token_not_found)
                                    , Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        compositeDisposable.clear();
                        Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    public static void sendAcceptRequestToRider(View view, Context context, String key, String tripNumberID) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);


        //get token
        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_RIDER_REFERENCE)
                //.getReference(Common.TOKEN_REFERENCE)

                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                            Map<String, String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_ACCEPT);
                            notificationData.put(Common.NOTI_CONTENT, "This message represent for driver accept user request");
                            notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());
                            notificationData.put(Common.TRIP_KEY, tripNumberID);


                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        if (fcmResponse.getSuccess() == 0) {
                                            compositeDisposable.clear();
                                            Snackbar.make(view, context.getString(R.string.accept_failed)
                                                    , Snackbar.LENGTH_LONG).show();

                                        }
//                                        else
//                                            Snackbar.make(view, context.getString(R.string.accept_success)
//                                                    , Snackbar.LENGTH_LONG).show();

                                    }, throwable -> {
                                        compositeDisposable.clear();
                                        Snackbar.make(view, throwable.getMessage()
                                                , Snackbar.LENGTH_LONG).show();

                                    }));


                        } else {
                            compositeDisposable.clear();
                            Snackbar.make(view, context.getString(R.string.token_not_found)
                                    , Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        compositeDisposable.clear();
                        Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    public static void sendNotifyToRider(Context context, View view, String key) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);


        //get token
        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Log.d("QuocDev", "NOtiFY");
                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);

                            Map<String, String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE, context.getString(R.string.driver_arrived));
                            notificationData.put(Common.NOTI_CONTENT, context.getString(R.string.your_driver_arrived));
                            notificationData.put(Common.DRIVER_KEY, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            notificationData.put(Common.RIDER_KEY, key);


                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        if (fcmResponse.getSuccess() == 0) {
                                            compositeDisposable.clear();
                                            Snackbar.make(view, context.getString(R.string.accept_failed)
                                                    , Snackbar.LENGTH_LONG).show();

                                        } else
                                            Log.d("QuocDev", "Notify driver apprived");
                                        EventBus.getDefault().postSticky(new NotifytoRiderEvent());

                                    }, throwable -> {
                                        compositeDisposable.clear();
                                        Snackbar.make(view, throwable.getMessage()
                                                , Snackbar.LENGTH_LONG).show();

                                    }));


                        } else {
                            compositeDisposable.clear();
                            Snackbar.make(view, context.getString(R.string.token_not_found)
                                    , Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        compositeDisposable.clear();
                        Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

    }

    public static void sendDeclineAndRemoveTripRequest(View view, Context context, String key, String tripNumberID) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //Firstly, Remove trip from FirebaseDatabase
        FirebaseDatabase.getInstance()
                .getReference(Common.Trip)
                .child(tripNumberID)
                .removeValue()
                .addOnFailureListener(e ->
                        Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show())
                .addOnSuccessListener(unused -> {
                    //delete success, send notification to Rider App

                    //get token
                    FirebaseDatabase
                            .getInstance()
                            //.getReference(Common.TOKEN_RIDER_REFERENCE)
                            .getReference(Common.TOKEN_REFERENCE)
                            .child(key)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                                        Map<String, String> notificationData = new HashMap<>();
                                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_DECLINE_AND_REMOVE);
                                        notificationData.put(Common.NOTI_CONTENT, "This message represent for driver decline user request");
                                        notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        notificationData.put(Common.RIDER_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());


                                        FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationData);

                                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(fcmResponse -> {
                                                    if (fcmResponse.getSuccess() == 0) {
                                                        compositeDisposable.clear();
                                                        Snackbar.make(view, context.getString(R.string.decline_failed)
                                                                , Snackbar.LENGTH_LONG).show();
                                                    } else {
                                                        Snackbar.make(view, context.getString(R.string.decline_succesed)
                                                                , Snackbar.LENGTH_LONG).show();
                                                    }

                                                }, throwable -> {
                                                    compositeDisposable.clear();
                                                    Snackbar.make(view, throwable.getMessage()
                                                            , Snackbar.LENGTH_LONG).show();

                                                }));


                                    } else {
                                        compositeDisposable.clear();
                                        Snackbar.make(view, context.getString(R.string.token_not_found)
                                                , Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    compositeDisposable.clear();
                                    Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                });
    }
}
