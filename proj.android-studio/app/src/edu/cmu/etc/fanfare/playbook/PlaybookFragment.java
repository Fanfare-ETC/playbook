package edu.cmu.etc.fanfare.playbook;

import android.support.v4.app.Fragment;

import org.json.JSONObject;

public abstract class PlaybookFragment extends Fragment {
    public void onWebSocketMessageReceived(JSONObject s) {
        // Empty default implementation.
    }
}
