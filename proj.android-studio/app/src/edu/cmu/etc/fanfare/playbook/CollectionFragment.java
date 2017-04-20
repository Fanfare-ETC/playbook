package edu.cmu.etc.fanfare.playbook;

import android.app.Activity;
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

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by yqi1 on 4/14/2017.
 */

public class CollectionFragment extends PlaybookFragment {

    private static final String TAG = CollectionFragment.class.getSimpleName();
    private static final String WEB_VIEW_PACKAGE_NAME = "com.google.android.webview";
    private static final String WEB_VIEW_PACKAGE_NAME_ALT = "com.android.webview";

    private boolean mIsRunning;
    // WebView 42 is the minimum that supports ES6 classes.
    private static final int MIN_WEB_VIEW_VERSION = 42;
    private boolean mWebViewIsCompatible = false;
    private WebView mWebView;
    private JSONObject mGameState;
    private Queue<JSONObject> mPendingEvents = new LinkedList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We use SharedPreference because the savedInstanceState doesn't work
        // if the fragment doesn't have an ID.
        try {
            SharedPreferences prefs = getActivity().getSharedPreferences("collection", Context.MODE_PRIVATE);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mWebView = new WebView(getActivity());
        checkWebViewVersion();
        mIsRunning = true;

        if (mWebViewIsCompatible) {
            WebView.setWebContentsDebuggingEnabled(true);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
            mWebView.addJavascriptInterface(new CollectionFragment.JavaScriptInterface(), "PlaybookBridge");
            mWebView.setWebChromeClient(new CollectionFragment.PlaybookWebChromeClient());
            mWebView.loadUrl("file:///android_asset/collection/index.html");
            Log.i(TAG, "url loaded");
        }

        return mWebView;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Save game state to preferences.
        SharedPreferences prefs = getActivity().getSharedPreferences("collection", Context.MODE_PRIVATE);
        prefs.edit().putString("gameState", mGameState.toString()).apply();
        Log.d(TAG, "Saved gameState to preferences");

        mIsRunning = false;
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    @Override
    public void onWebSocketMessageReceived(Activity context, JSONObject s) {
        super.onWebSocketMessageReceived(context, s);
        if (!mIsRunning) {
            Log.d(TAG, "Received event while fragment is not attached, adding to queue");
            mPendingEvents.add(s);
        }
    }

    private JSONObject createInitialGameState() throws JSONException {
        JSONObject state = new JSONObject();
        JSONArray cardSlots = new JSONArray();
        for (int i = 0; i < 5; i++) {
            JSONObject slot = new JSONObject();
            slot.put("present", false);
            slot.put("card", JSONObject.NULL);
            cardSlots.put(slot);
        }

        state.put("cardSlots", cardSlots);
        state.put("goal", JSONObject.NULL);
        state.put("score", 0);
        state.put("selectedGoal", JSONObject.NULL);
        return state;
    }

    private Integer getWebViewMajorVersion() {
        PackageManager pm = getActivity().getPackageManager();
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

    private void showWebViewNeedsUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void showWebViewNotInstalledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    private void sendMessage(final JSONObject jsonObject) {
        String quoted = JSONObject.quote(jsonObject.toString());
        StringBuilder builder = new StringBuilder();
        String js = builder
                .append("window.dispatchEvent(\n")
                .append("new MessageEvent('message', { data: JSON.parse(")
                .append(quoted)
                .append(") })\n")
                .append(");")
                .toString();
        Log.d(TAG, js);
        mWebView.evaluateJavascript(js, null);
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
        public String getSectionAPIUrl() {
            return "http://" + BuildConfig.PLAYBOOK_SECTION_API_HOST + ":" +
                    BuildConfig.PLAYBOOK_SECTION_API_PORT;
        }

        @JavascriptInterface
        public String getPlayerID() {
            return Cocos2dxBridge.getPlayerID();
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
                        CollectionFragment.this.sendMessage(jsonObject);

                        // Send down any pending events that were received when we weren't in
                        // the foreground.
                        while (!mPendingEvents.isEmpty()) {
                            jsonObject = new JSONObject();
                            jsonObject.put("action", "HANDLE_MESSAGE");
                            jsonObject.put("payload", mPendingEvents.poll());
                            CollectionFragment.this.sendMessage(jsonObject);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
