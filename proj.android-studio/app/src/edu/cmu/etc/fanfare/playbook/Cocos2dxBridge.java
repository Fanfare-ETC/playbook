package edu.cmu.etc.fanfare.playbook;

import java.util.HashMap;
import java.util.Map;

public class Cocos2dxBridge {
    private static final String TAG = "Cocos2dxBridge";
    private static boolean mDidFinishLaunching = false;
    private static String mPlayerName;
    private static String mPlayerID;

    private static Map<Integer, Integer> mPredictionCounts = new HashMap<>();

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
}
