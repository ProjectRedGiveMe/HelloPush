package com.ibm.hellopush;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;


public class BGTagService extends Service {
    // Thread - We use to active our service in the background
    private Thread thread;
    private MFPPush push = MFPPush.getInstance();
    private String tag = MainActivity.TAG_DONOR;
    private static final String TAG = MainActivity.class.getSimpleName();



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*if(thread == null){ // Lazy loading
            thread = new Thread(this);
            thread.start();
        }*/
        Log.i(TAG, " ********************* In Service ************************ " + tag );

        return super.onStartCommand(intent, flags, startId);
    }
/*
    @Override
    public void run() {
       *//* // unsubscibe from the given tag ,that to which the device is subscribed.
        push.unsubscribe(TAG_DONOR, new MFPPushResponseListener<String>() {

            @Override
            public void onSuccess(String s) {
                System.out.println("Successfully unsubscribed from tag . "+ TAG_DONOR);
            }

            @Override
            public void onFailure(MFPPushException e) {
                System.out.println("Error while unsubscribing from tags. "+ e.getMessage());
            }
        });*//*
    }*/

    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
        Log.i(TAG, " ********************* In task remove " + tag + " ************************ " );
        //unregister listeners
        //do any other cleanup if required
        // unsubscibe from the given tag ,that to which the device is subscribed.
        push.unsubscribe(tag, new MFPPushResponseListener<String>() {

            @Override
            public void onSuccess(String s) {
                System.out.println("Successfully unsubscribed from tag . "+ tag);
                Log.i(TAG, " ********************* Successfully unsubscribed from tag "+ tag + " ************************ " );

            }

            @Override
            public void onFailure(MFPPushException e) {
                System.out.println("Error while unsubscribing from tags. "+ e.getMessage());
            }
        });



        //stop service
        stopSelf();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
