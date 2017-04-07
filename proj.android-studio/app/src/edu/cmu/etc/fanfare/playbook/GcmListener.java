package edu.cmu.etc.fanfare.playbook;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class GcmListener extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";
    private static final int REQUEST_CODE_PREDICTION_SCORED = 0;

    /**
     * Called when message is received.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map data = remoteMessage.getData();
        String message = (String) data.get("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/global")) {
            // message received from some topic.
            sendNotification(message);
        } else if (from.startsWith("/topics/playsCreated")) {
            handlePlaysCreated(message);
        } else {
            Log.d(TAG, "Not a topic message?");
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
       // sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    private void handlePlaysCreated(String message) {
        try {
            if (AppActivity.isInForeground && AppActivity.selectedItem == AppActivity.DRAWER_PREDICTION_FRAGMENT) {
                return;
            }

            ArrayList<Integer> correctPredictions = new ArrayList<>();
            JSONArray playsArray = new JSONArray(message);
            for (int i = 0; i < playsArray.length(); i++) {
                int event = playsArray.getInt(i);
                if (Cocos2dxBridge.getPredictionCount(event) > 0) {
                    correctPredictions.add(event);
                }
            }

            if (correctPredictions.size() > 0) {
                Intent intent = new Intent(this, AppActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putIntegerArrayListExtra(AppActivity.INTENT_EXTRA_PREDICTIONS_SCORED, correctPredictions);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        REQUEST_CODE_PREDICTION_SCORED, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle("Bravo!")
                        .setContentText("You got a prediction right.")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, AppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("GCM Message: ")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
