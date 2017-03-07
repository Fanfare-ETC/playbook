package edu.cmu.etc.fanfare.playbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SeatSelectFragment extends Cocos2dxFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Cocos2dxBridge.loadScene("SectionSelection");
        return view;
    }
}
