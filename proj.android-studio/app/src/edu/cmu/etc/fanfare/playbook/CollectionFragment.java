package edu.cmu.etc.fanfare.playbook;

import android.util.Log;

/**
 * Created by ramya on 3/3/17.
 */

public class CollectionFragment extends Cocos2dxFragment {
    private static final String TAG = "CollectionFragment";

    @Override
    public void onResume() {
        super.onResume();
        if (Cocos2dxBridge.didFinishLaunching()) {
            Log.v(TAG, "Loading CollectionScreen scene");
            Cocos2dxBridge.loadScene("CollectionScreen");
        }
    }

    @Override
    public void onApplicationDidFinishLaunching() {
        super.onApplicationDidFinishLaunching();
        Cocos2dxBridge.loadScene("CollectionScreen");
    }
}
