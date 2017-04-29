package edu.cmu.etc.fanfare.playbook;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

public class PlaybookApplication extends Application {
    private static final String TAG = PlaybookApplication.class.getSimpleName();

    public static final String PREF_KEY_GCM_SENT_TOKEN = "gcmSentToken";
    public static final String PREF_KEY_GCM_MESSAGE_QUEUE = "gcmMessageQueue";
    public static final String PREF_KEY_IS_ONBOARDING_COMPLETE = "isOnboardingComplete";
    public static final String PREF_KEY_FIRST_TIME_SHOW_DRAWER = "firstTimeShowDrawer";

    public static final int NOTIFICATION_ID_PLAYS_CREATED = 0;
    public static final int NOTIFICATION_ID_NOTIFY_LOCK_PREDICTIONS = 1;
    public static final int NOTIFICATION_ID_CLEAR_PREDICTIONS = 2;

    private static String mPlayerName;
    private static String mPlayerID;

    @Override
    public void onCreate() {
        super.onCreate();
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e) {
                Log.e(TAG, "Forcefully continuing despite exception: ", e);
            }
        }
    }

    /**
     * For LoginActivity to set the player name and ID.
     */
    public static void setPlayerName(String name) { mPlayerName = name; }
    public static String getPlayerName() { return mPlayerName; }
    public static void setPlayerID(String id) { mPlayerID = id; }
    public static String getPlayerID() { return mPlayerID; }
}
