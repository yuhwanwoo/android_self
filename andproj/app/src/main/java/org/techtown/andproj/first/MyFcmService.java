package org.techtown.andproj.first;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFcmService extends FirebaseMessagingService {
    private static final String TAG = MyFcmService.class.getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG,"onMessageReceived ID: "+remoteMessage.getMessageId());
        Log.d(TAG,"onMessageReceived DATA: "+remoteMessage.getData());
    }
}
