package edu.cmu.etc.fanfare.playbook;

import android.util.Log;

public class CatchPlayFragment extends Cocos2dxFragment {
    private static final String TAG = "CatchPlayFragment";

    @Override
    public void onResume() {
        super.onResume();
        if (Cocos2dxBridge.didFinishLaunching()) {
            Log.v(TAG, "Loading HelloWorld scene");
            Cocos2dxBridge.loadScene("HelloWorld");
        }
    }

    @Override
    public void onApplicationDidFinishLaunching() {
        super.onApplicationDidFinishLaunching();
        Cocos2dxBridge.loadScene("HelloWorld");
    }
}
