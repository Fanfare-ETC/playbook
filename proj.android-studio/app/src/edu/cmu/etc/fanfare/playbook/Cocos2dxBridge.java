package edu.cmu.etc.fanfare.playbook;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Cocos2dxBridge {
    private static final String TAG = "Cocos2dxBridge";
    private static boolean mDidFinishLaunching = false;
    private static List<Cocos2dxFragment> mRegisteredFragments = new ArrayList<>();

    public static native void loadScene(String sceneName);

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
}
