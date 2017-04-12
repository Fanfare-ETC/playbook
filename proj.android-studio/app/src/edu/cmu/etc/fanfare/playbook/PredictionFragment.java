package edu.cmu.etc.fanfare.playbook;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PredictionFragment extends PlaybookFragment {
    private static final String TAG = PredictionFragment.class.getSimpleName();
    private static final String WEB_VIEW_PACKAGE_NAME = "com.google.android.webview";
    private static final String WEB_VIEW_PACKAGE_NAME_ALT = "com.android.webview";

    // WebView 42 is the minimum that supports ES6 classes.
    private static final int MIN_WEB_VIEW_VERSION = 42;

    private static final SparseArray<String> PLAYBOOK_EVENTS = new SparseArray<String>();

    private boolean mWebViewIsCompatible = false;
    private WebView mWebView;
    private JSONObject mGameState;
    private boolean mIsRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We use SharedPreference because the savedInstanceState doesn't work
        // if the fragment doesn't have an ID.
        try {
            SharedPreferences prefs = getContext().getSharedPreferences("prediction", Context.MODE_PRIVATE);
            String gameState = prefs.getString("gameState", null);
            if (gameState != null) {
                Log.d(TAG, "Restoring game state from bundle: " + gameState);
                mGameState = new JSONObject(gameState);
            } else {
                Log.d(TAG, "Game state doesn't exist, creating initial state");
                mGameState = createInitialGameState();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Populate the events.
        PLAYBOOK_EVENTS.append(0, "SHUTOUT_INNING");
        PLAYBOOK_EVENTS.append(1, "RUN_SCORED");
        PLAYBOOK_EVENTS.append(2, "FLY_OUT");
        PLAYBOOK_EVENTS.append(3, "TRIPLE_PLAY");
        PLAYBOOK_EVENTS.append(4, "DOUBLE_PLAY");
        PLAYBOOK_EVENTS.append(5, "GROUND_OUT");
        PLAYBOOK_EVENTS.append(6, "STEAL");
        PLAYBOOK_EVENTS.append(7, "PICK_OFF");
        PLAYBOOK_EVENTS.append(8, "WALK");
        PLAYBOOK_EVENTS.append(9, "BLOCKED_RUN");
        PLAYBOOK_EVENTS.append(10, "STRIKEOUT");
        PLAYBOOK_EVENTS.append(11, "HIT_BY_PITCH");
        PLAYBOOK_EVENTS.append(12, "HOME_RUN");
        PLAYBOOK_EVENTS.append(13, "PITCH_COUNT_16");
        PLAYBOOK_EVENTS.append(14, "PITCH_COUNT_17");
        PLAYBOOK_EVENTS.append(15, "SINGLE");
        PLAYBOOK_EVENTS.append(16, "DOUBLE");
        PLAYBOOK_EVENTS.append(17, "TRIPLE");
        PLAYBOOK_EVENTS.append(18, "BATTER_COUNT_4");
        PLAYBOOK_EVENTS.append(19, "BATTER_COUNT_5");
        PLAYBOOK_EVENTS.append(20, "MOST_IN_LEFT_OUTFIELD");
        PLAYBOOK_EVENTS.append(21, "MOST_IN_RIGHT_OUTFIELD");
        PLAYBOOK_EVENTS.append(22, "MOST_IN_INFIELD");
        PLAYBOOK_EVENTS.append(23, "UNKNOWN");

        // Mark ourselves as running.
        mIsRunning = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mWebView = new WebView(getContext());
        checkWebViewVersion();

        if (mWebViewIsCompatible) {
            WebView.setWebContentsDebuggingEnabled(true);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
            mWebView.addJavascriptInterface(new JavaScriptInterface(), "PlaybookBridge");
            mWebView.setWebChromeClient(new PlaybookWebChromeClient());
            mWebView.loadUrl("file:///android_asset/prediction/index.html");
        }

        return mWebView;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Save game state to preferences.
        SharedPreferences prefs = getContext().getSharedPreferences("prediction", Context.MODE_PRIVATE);
        prefs.edit().putString("gameState", mGameState.toString()).apply();
        Log.d(TAG, "Saved gameState to preferences");

        mIsRunning = false;
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    @Override
    public void onWebSocketMessageReceived(JSONObject s) {
        super.onWebSocketMessageReceived(s);
        try {
            if (!mIsRunning) {
                if (s.has("event")) {
                    String event = s.getString("event");
                    if (event.equals("server:playsCreated")) {
                        handlePlaysCreated(s);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject createInitialGameState() throws JSONException {
        JSONObject state = new JSONObject();
        JSONArray ballsSelectedTarget = new JSONArray();
        for (int i = 0; i < 5; i++) {
            ballsSelectedTarget.put(null);
        }

        state.put("stage", "INITIAL");
        state.put("predictionCounts", new JSONObject());
        state.put("ballsSelectedTarget", ballsSelectedTarget);
        return state;
    }

    private Integer getWebViewMajorVersion() {
        PackageManager pm = getContext().getPackageManager();
        PackageInfo info;
        try {
            info = pm.getPackageInfo(WEB_VIEW_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e1) {
            try {
                info = pm.getPackageInfo(WEB_VIEW_PACKAGE_NAME_ALT, 0);
            } catch (PackageManager.NameNotFoundException e2) {
                Log.e(TAG, "Android System WebView is not installed");
                return null;
            }
        }

        Log.d(TAG, "Android System WebView: version " + info.versionName);
        String[] parts = info.versionName.split("\\.");
        return Integer.parseInt(parts[0]);
    }

    private void checkWebViewVersion() {
        Integer version = getWebViewMajorVersion();
        if (version == null) {
            showWebViewNotInstalledDialog();
        } else if (version >= MIN_WEB_VIEW_VERSION) {
            mWebViewIsCompatible = true;
        } else {
            showWebViewNeedsUpdateDialog();
        }
    }

    private void showWebViewNotInstalledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.prediction_web_view_not_installed)
            .setTitle(R.string.prediction_incompatible_device)
            .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            })
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    getActivity().finish();
                }
            });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showWebViewNeedsUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.prediction_web_view_needs_update)
            .setTitle(R.string.prediction_update_web_view)
            .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + WEB_VIEW_PACKAGE_NAME)
                        ));
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + WEB_VIEW_PACKAGE_NAME)
                        ));
                    }
                }
            })
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    getActivity().finish();
                }
            });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendMessage(final JSONObject jsonObject) {
        String quoted = JSONObject.quote(jsonObject.toString());
        StringBuilder builder = new StringBuilder();
        String js = builder
                .append("const event = new MessageEvent('message', { data: JSON.parse(")
                .append(quoted)
                .append(") });\n")
                .append("window.dispatchEvent(event);\n")
                .toString();
        Log.d(TAG, js);
        mWebView.evaluateJavascript(js, null);
    }

    private void handlePlaysCreated(JSONObject s) throws JSONException {
        JSONArray data = s.getJSONArray("data");
        List<String> events = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            events.add(PLAYBOOK_EVENTS.valueAt(data.getInt(i)));
        }

        // Add it to the game state.
        Log.d(TAG, "Creating plays: " + events.toString());
        for (String event : events) {
            makePrediction(event);
        }
    }

    private void makePrediction(String event) {
        try {
            if (!mGameState.has("predictionCounts")) {
                mGameState.put("predictionCounts", new JSONObject());
            }

            JSONObject predictionCounts = mGameState.getJSONObject("predictionCounts");
            if (predictionCounts.has(event)) {
                int count = predictionCounts.getInt(event);
                predictionCounts.put(event, count + 1);
            } else {
                predictionCounts.put(event, 1);
            }

            // Find the
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class PlaybookWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            switch (cm.messageLevel()) {
                case DEBUG:
                case LOG:
                case TIP:
                    Log.d(TAG, cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
                    break;
                case WARNING:
                    Log.w(TAG, cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
                    break;
                case ERROR:
                    Log.e(TAG, cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
                    break;
            }
            return true;
        }
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public String getAPIUrl() {
            return "ws://" + BuildConfig.PLAYBOOK_API_HOST + ":" +
                    BuildConfig.PLAYBOOK_API_PORT;
        }

        @JavascriptInterface
        public void notifyGameState(String state) {
            try {
                Log.d(TAG, "Received game state: " + state);
                mGameState = new JSONObject(state);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void notifyLoaded() {
            Log.d(TAG, "The JavaScript world has arrived");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("action", "RESTORE_GAME_STATE");
                        jsonObject.put("payload", mGameState.toString());
                        PredictionFragment.this.sendMessage(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
