package edu.cmu.etc.fanfare.playbook;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class GcmListener extends FirebaseMessagingService {
    private static final String TAG = GcmListener.class.getSimpleName();

    private static final int REQUEST_CODE_PLAYS_CREATED = 0;
    private static final int REQUEST_CODE_LOCK_PREDICTIONS = 1;
    private static final int REQUEST_CODE_CLEAR_PREDICTIONS = 2;

    private static final int NOTIFICATION_ID_PLAYS_CREATED = 0;
    private static final int NOTIFICATION_ID_LOCK_PREDICTIONS = 1;
    private static final int NOTIFICATION_ID_CLEAR_PREDICTIONS = 2;

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
        } else if (from.startsWith("/topics/lockPredictions")) {
            handleLockPredictions(message);
        } else if (from.startsWith("/topics/clearPredictions")) {
            handleClearPredictions(message);
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
        if (AppActivity.isInForeground &&
            AppActivity.selectedItem == DrawerItemAdapter.DRAWER_ITEM_PREDICTION ||
            AppActivity.selectedItem == DrawerItemAdapter.DRAWER_ITEM_COLLECTION) {
            return;
        }

        // Show a notification for the play created.
        Log.d(TAG, "handlePlaysCreated");
        Intent intent = new Intent(this, AppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AppActivity.INTENT_EXTRA_GCM_PLAYS_CREATED, message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                REQUEST_CODE_PLAYS_CREATED, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("Something interesting happened!")
                .setContentText("Check to see if you got a prediction right.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID_PLAYS_CREATED, notificationBuilder.build());

    }

    private void handleLockPredictions(String message) {
        if (AppActivity.isInForeground &&
                AppActivity.selectedItem == DrawerItemAdapter.DRAWER_ITEM_PREDICTION) {
            return;
        }

        // Discard the message stuff has started.
        Log.d(TAG, message);

        // Show a notification that the predictions will be locked soon.
        Log.d(TAG, "handleLockPredictions");
        Intent intent = new Intent(this, AppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_PREDICTION);
        intent.putExtra(AppActivity.INTENT_EXTRA_GCM_LOCK_PREDICTIONS, message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                REQUEST_CODE_LOCK_PREDICTIONS, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("Half-inning is starting soon")
                .setContentText("Predictions will be locked in 10 seconds.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID_LOCK_PREDICTIONS, notificationBuilder.build());
    }

    private void handleClearPredictions(String message) {
        if (AppActivity.isInForeground &&
            AppActivity.selectedItem == DrawerItemAdapter.DRAWER_ITEM_PREDICTION) {
            return;
        }

        // Show a notification that the predictions have been cleared.
        Log.d(TAG, "handleClearPredictions");
        Intent intent = new Intent(this, AppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_PREDICTION);
        intent.putExtra(AppActivity.INTENT_EXTRA_GCM_CLEAR_PREDICTIONS, message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                REQUEST_CODE_CLEAR_PREDICTIONS, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("Half-inning has ended")
                .setContentText("Predictions have been cleared.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID_CLEAR_PREDICTIONS, notificationBuilder.build());
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
