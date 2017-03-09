package edu.cmu.etc.fanfare.playbook;

import android.util.Log;

public class SeatSelectFragment extends Cocos2dxFragment {
    private static final String TAG = "SeatSelectFragment";

    @Override
    public void onResume() {
        super.onResume();
        if (Cocos2dxBridge.didFinishLaunching()) {
            Log.v(TAG, "Loading SectionSelection scene");
            Cocos2dxBridge.loadScene("SectionSelection");
        }
    }

    @Override
    public void onApplicationDidFinishLaunching() {
        super.onApplicationDidFinishLaunching();
        Cocos2dxBridge.loadScene("SectionSelection");
    }
}
