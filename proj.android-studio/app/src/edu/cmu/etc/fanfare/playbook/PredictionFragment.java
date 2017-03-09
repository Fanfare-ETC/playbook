package edu.cmu.etc.fanfare.playbook;

import android.util.Log;

public class PredictionFragment extends Cocos2dxFragment {
    private static final String TAG = "PredictionFragment";

    @Override
    public void onResume() {
        super.onResume();
        if (Cocos2dxBridge.didFinishLaunching()) {
            Log.v(TAG, "Loading Prediction scene");
            Cocos2dxBridge.loadScene("Prediction");
        }
    }

    @Override
    public void onApplicationDidFinishLaunching() {
        super.onApplicationDidFinishLaunching();
        Cocos2dxBridge.loadScene("Prediction");
    }
}
