package edu.cmu.etc.fanfare.playbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ramya on 3/3/17.
 */

public class CollectionFragment extends Cocos2dxFragment {
    public void onResume() {
         super.onResume();
        Cocos2dxBridge.loadScene("CollectionScreen");
    }
}
