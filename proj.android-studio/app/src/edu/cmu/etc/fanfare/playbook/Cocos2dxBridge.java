package edu.cmu.etc.fanfare.playbook;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cocos2dxBridge {
    private static final String TAG = "Cocos2dxBridge";
    private static boolean mDidFinishLaunching = false;
    private static List<Cocos2dxFragment> mRegisteredFragments = new ArrayList<>();
    private static String mPlayerName;
    private static String mPlayerID;

    private static Map<Integer, Integer> mPredictionCounts = new HashMap<>();

    public static native void loadScene(String sceneName);
    public static native int getSection();
    public static native int getPredictionScoreForEvent(int event);

    /**
     * Called by Cocos2dxFragment when the native library completes loading.
     * @param fragment
     */
    public static void register(Cocos2dxFragment fragment) {
        mRegisteredFragments.add(fragment);
    }

    /**
     * Called from native code (AppDelegate.cpp) when the application has
     * completed launching. Before this is done, we should not be running
     * any code that interacts with the Cocos2d-x threads.
     */
    public static void onApplicationDidFinishLaunching() {
        Log.d(TAG, "onApplicationDidFinishLaunching");
        mDidFinishLaunching = true;

        // Call the callbacks on registered fragments.
        for (Cocos2dxFragment fragment : mRegisteredFragments) {
            fragment.onApplicationDidFinishLaunching();
        }
    }

    /**
     * Allows listeners to check if we have finished launching.
     */
    public static boolean didFinishLaunching() {
        return Cocos2dxBridge.mDidFinishLaunching;
    }

    /**
     * For LoginActivity to set the player name and ID.
     */
    public static void setPlayerName(String name) { mPlayerName = name; }
    public static String getPlayerName() { return mPlayerName; }
    public static void setPlayerID(String id) { mPlayerID = id; }
    public static String getPlayerID() { return mPlayerID; }

    /**
     * For prediction screen to set predictions.
     */
    public static void addPrediction(int event, int count) {
        Log.d(TAG, "addPrediction: event: " + event + ", count: " + count);
        mPredictionCounts.put(event, count);
    }

    public static void clearPredictions() {
        Log.d(TAG, "clearPredictions");
        mPredictionCounts.clear();
    }

    public static int getPredictionCount(int event) {
        Log.d(TAG, "getPredictionCount: event: " + event);
        if (mPredictionCounts.containsKey(event)) {
            return mPredictionCounts.get(event);
        } else {
            return 0;
        }
    }
}
