package edu.cmu.etc.fanfare.playbook;

import android.app.Activity;
import android.support.v4.app.Fragment;

import org.json.JSONObject;

public abstract class PlaybookFragment extends Fragment {
    public void onWebSocketMessageReceived(Activity context, JSONObject s) {
        // Empty default implementation.
    }
}
