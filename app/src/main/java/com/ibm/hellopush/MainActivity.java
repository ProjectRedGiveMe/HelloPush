package com.ibm.hellopush;
/**
 * Copyright 2015, 2016 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.messaging.RemoteMessage;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationOptions;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    static final String TAG_DONOR = "Donor";

    private MFPPush push; // Push client
    private MFPPushNotificationListener notificationListener; // Notification listener to handle a push sent to the phone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize core SDK with IBM Bluemix application Region, TODO: Update region if not using Bluemix US SOUTH
        BMSClient.getInstance().initialize(this, BMSClient.REGION_US_SOUTH);

        // Try sound
        MFPPushNotificationOptions options = new MFPPushNotificationOptions();

        options.setSound("whistle.wav");
        // Grabs push client sdk instance
        push = MFPPush.getInstance();
        // Initialize Push client
        // You can find your App Guid and Client Secret by navigating to the Configure section of your Push dashboard, click Mobile Options (Upper Right Hand Corner)
        // TODO: Please replace <APP_GUID> and <CLIENT_SECRET> with a valid App GUID and Client Secret from the Push dashboard Mobile Options
        push.initialize(this, "28da4c7b-3f6d-48cb-8e9c-5c64d8f1da36", "85d142d8-68d7-4726-baa6-a6bfc73e383e", options);

        Log.i(TAG, "***********************************************");

//        startService(new Intent(getApplicationContext(),BGTagService.class));

        // Create notification listener and enable pop up notification when a message is received
        notificationListener = new MFPPushNotificationListener() {
            @Override
            public void onReceive(final MFPSimplePushNotification message) {
                Log.i(TAG, "Received a Push Notification: " + message.toString());
             /*   runOnUiThread(new Runnable() {
                    public void run() {
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("Yay Notification")
                                .setMessage(message.getAlert())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                    }
                });*/

                /*ServiceConnection mConnection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName className,
                                                   IBinder binder) {
                        ((KillNotificationsService.KillBinder) binder).service.startService(new Intent(
                                MainActivity.this, KillNotificationsService.class));
                        RemoteMessage.Notification notification = new RemoteMessage.Notification(
                                R.drawable.ic_launcher, "Text",
                                System.currentTimeMillis());
                        Intent notificationIntent = new Intent(MainActivity.this,
                                Place.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(
                                MainActivity.this, 0, notificationIntent, 0);
                        notification.(getApplicationContext(),
                                "Text", "Text", contentIntent);
                        NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNM.notify(KillNotificationsService.NOTIFICATION_ID,
                                notification);
                    }

                    public void onServiceDisconnected(ComponentName className) {
                    }

                };
                bindService(new Intent(MainActivity.this,
                                KillNotificationsService.class), mConnection,
                        Context.BIND_AUTO_CREATE);
            */


            }
        };

    }


    /**
     * Called when the register device button is pressed.
     * Attempts to register the device with your push service on Bluemix.
     * If successful, the push client sdk begins listening to the notification listener.
     * Also includes the example option of UserID association with the registration for very targeted Push notifications.
     *
     * @param view the button pressed
     */
    public void registerDevice(View view) {

        // Checks for null in case registration has failed previously
        if(push==null){
            push = MFPPush.getInstance();
        }

        // Make register button unclickable during registration and show registering text
        TextView buttonText = (TextView) findViewById(R.id.button_text);
        buttonText.setClickable(false);
        TextView responseText = (TextView) findViewById(R.id.response_text);
        responseText.setText(R.string.Registering);
        Log.i(TAG, "Registering for notifications");

        // Creates response listener to handle the response when a device is registered.
        MFPPushResponseListener registrationResponselistener = new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                // Split response and convert to JSON object to display User ID confirmation from the backend
                String[] resp = response.split("Text: ");
                try {
                    JSONObject responseJSON = new JSONObject(resp[1]);
                    setStatus("Device Registered Successfully with USER ID " + responseJSON.getString("userId"), true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Successfully registered for push notifications, " + response);
                // Start listening to notification listener now that registration has succeeded
                push.listen(notificationListener);

                startService(new Intent(getApplicationContext(),BGTagService.class));

                // Subscribe for tag at push notification
                push.subscribe(TAG_DONOR, new MFPPushResponseListener<String>() {
                    @Override
                    public void onFailure(MFPPushException ex) {
//                        updateTextView("Failure" + ex.getMessage());
                    }

                    @Override
                    public void onSuccess(String response) {
                     startService(new Intent(getApplicationContext(),BGTagService.class));

//                        updateTextView(Success: "+ response);
                    }
                });
            }

            @Override
            public void onFailure(MFPPushException exception) {
                String errLog = "Error registering for push notifications: ";
                String errMessage = exception.getErrorMessage();
                int statusCode = exception.getStatusCode();

                // Set error log based on response code and error message
                if(statusCode == 401){
                    errLog += "Cannot authenticate successfully with Bluemix Push instance, ensure your CLIENT SECRET was set correctly.";
                } else if(statusCode == 404 && errMessage.contains("Push GCM Configuration")){
                    errLog += "Push GCM Configuration does not exist, ensure you have configured GCM Push credentials on your Bluemix Push dashboard correctly.";
                } else if(statusCode == 404 && errMessage.contains("PushApplication")){
                    errLog += "Cannot find Bluemix Push instance, ensure your APPLICATION ID was set correctly and your phone can successfully connect to the internet.";
                } else if(statusCode >= 500){
                    errLog += "Bluemix and/or your Push instance seem to be having problems, please try again later.";
                }

                setStatus(errLog, false);
                Log.e(TAG,errLog);
                // make push null since registration failed
                push = null;
            }
        };

        // Attempt to register device using response listener created above
        // Include unique sample user Id instead of Sample UserId in order to send targeted push notifications to specific users
        push.registerDeviceWithUserId("Sample UserID",registrationResponselistener);
    }

    // If the device has been registered previously, hold push notifications when the app is paused
    @Override
    protected void onPause() {
        super.onPause();

        if (push != null) {
            push.hold();
        }
    }

    // If the device has been registered previously, ensure the client sdk is still using the notification listener from onCreate when app is resumed
    @Override
    protected void onResume() {
        super.onResume();
        if (push != null) {
            push.listen(notificationListener);
        }
    }

    /**
     * Manipulates text fields in the UI based on initialization and registration events
     * @param messageText String main text view
     * @param wasSuccessful Boolean dictates top 2 text view texts
     */
    private void setStatus(final String messageText, boolean wasSuccessful){
        final TextView responseText = (TextView) findViewById(R.id.response_text);
        final TextView topText = (TextView) findViewById(R.id.top_text);
        final TextView bottomText = (TextView) findViewById(R.id.bottom_text);
        final TextView buttonText = (TextView) findViewById(R.id.button_text);
        final String topStatus = wasSuccessful ? "Yay!" : "Bummer";
        final String bottomStatus = wasSuccessful ? "You Are Connected" : "Something Went Wrong";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonText.setClickable(true);
                responseText.setText(messageText);
                topText.setText(topStatus);
                bottomText.setText(bottomStatus);
            }
        });
    }


    /*// Once the app is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unsubscibe from the given tag ,that to which the device is subscribed.
        push.unsubscribe(TAG_DONOR, new MFPPushResponseListener<String>() {

            @Override
            public void onSuccess(String s) {
                System.out.println("Successfully unsubscribed from tag . "+ TAG_DONOR);
            }

            @Override
            public void onFailure(MFPPushException e) {
                System.out.println("Error while unsubscribing from tags. "+ e.getMessage());
            }
        });
    }*/
}
