package edu.cmu.etc.fanfare.playbook;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static edu.cmu.etc.fanfare.playbook.PlaybookApplication.NOTIFICATION_ID_CLEAR_PREDICTIONS;
import static edu.cmu.etc.fanfare.playbook.PlaybookApplication.NOTIFICATION_ID_NOTIFY_LOCK_PREDICTIONS;
import static edu.cmu.etc.fanfare.playbook.PlaybookApplication.NOTIFICATION_ID_PLAYS_CREATED;

public class GcmListener extends FirebaseMessagingService {
    private static final String TAG = GcmListener.class.getSimpleName();

    /**
     * Called when message is received.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map data = remoteMessage.getData();
        String message = (String) data.get("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        // Persist this message so that AppActivity picks it up on next run.
        if (!AppActivity.isInForeground) {
            persist(message);
        }

        if (from.startsWith("/topics/playsCreated")) {
            handlePlaysCreated(message);
        } else if (from.startsWith("/topics/notifyLockPredictions")) {
            handleNotifyLockPredictions(message);
        } else if (from.startsWith("/topics/clearPredictions")) {
            handleClearPredictions(message);
        } else {
            Log.d(TAG, "Not a topic message?");
        }
    }

    private void persist(String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            JSONArray queueJSON;
            if (prefs.contains(PlaybookApplication.PREF_KEY_GCM_MESSAGE_QUEUE)) {
                queueJSON = new JSONArray(prefs.getString(PlaybookApplication.PREF_KEY_GCM_MESSAGE_QUEUE, "[]"));
            } else {
                queueJSON = new JSONArray();
            }
            JSONObject messageJSON = new JSONObject(message);
            queueJSON.put(messageJSON);
            prefs.edit().putString(PlaybookApplication.PREF_KEY_GCM_MESSAGE_QUEUE, queueJSON.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlePlaysCreated(String message) {
        if (AppActivity.isInForeground) {
            return;
        }

        // Process the play.
        try {
            JSONObject messageJSON = new JSONObject(message);
            JSONArray playsJSON = messageJSON.getJSONArray("data");
            for (int i = 0; i < playsJSON.length(); i++) {
                // TODO: Retrieve the name of the play.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Show a notification for the play created.
        Log.d(TAG, "handlePlaysCreated: " + message);
        PendingIntent predictionIntent = createPendingIntent(DrawerItemAdapter.DRAWER_ITEM_PREDICTION);
        PendingIntent collectionIntent = createPendingIntent(DrawerItemAdapter.DRAWER_ITEM_COLLECTION);
        NotificationCompat.Builder builder = buildNotification()
                .setContentTitle("A play happened!")
                .setContentText("Check it out by choosing one of the options.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_betting_board_32dp, "Check Predictions", predictionIntent)
                .addAction(R.drawable.ic_catch_the_play_game_32dp, "Collect", collectionIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID_PLAYS_CREATED, builder.build());
    }

    private void handleNotifyLockPredictions(String message) {
        if (AppActivity.isInForeground &&
            AppActivity.selectedItem == DrawerItemAdapter.DRAWER_ITEM_PREDICTION) {
            return;
        }

        // Show a notification that the predictions will be locked soon.
        Log.d(TAG, "handleNotifyLockPredictions");
        PendingIntent intent = createPendingIntent(DrawerItemAdapter.DRAWER_ITEM_PREDICTION);
        NotificationCompat.Builder builder = buildNotification()
                .setContentTitle("Half-inning is starting soon")
                .setContentText("Predictions will be locked in 10 seconds.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(intent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID_NOTIFY_LOCK_PREDICTIONS, builder.build());
    }

    private void handleClearPredictions(String message) {
        if (AppActivity.isInForeground) {
            return;
        }

        // Show a notification that the predictions have been cleared.
        Log.d(TAG, "handleClearPredictions");
        PendingIntent intent = createPendingIntent(DrawerItemAdapter.DRAWER_ITEM_PREDICTION);
        NotificationCompat.Builder builder = buildNotification()
                .setContentTitle("Half-inning has ended")
                .setContentText("Predictions have been cleared.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(intent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID_CLEAR_PREDICTIONS, builder.build());
    }

    private PendingIntent createPendingIntent(int drawerItem) {
        Intent intent = new Intent(this, AppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, drawerItem);
        return PendingIntent.getActivity(this,
                drawerItem, intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    private NotificationCompat.Builder buildNotification() {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
    }
}
