package edu.cmu.etc.fanfare.playbook;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

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

    private static final String PREF_NAME = "collection";
    private static final String PREF_KEY_GAME_STATE = "gameState";
    private static final String PREF_KEY_TUTORIAL_SHOWN = "tutorialShown";

    // WebView 42 is the minimum that supports ES6 classes.
    private static final int MIN_WEB_VIEW_VERSION = 42;
    private boolean mWebViewIsCompatible = false;
    private WebView mWebView;
    private JSONObject mGameState;
    private boolean mIsAttached;
    private Queue<JSONObject> mPendingEvents = new LinkedList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // We use SharedPreference because the savedInstanceState doesn't work
        // if the fragment doesn't have an ID.
        try {
            prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String gameState = prefs.getString(PREF_KEY_GAME_STATE, null);
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

        // Show tutorial for the first time.
        boolean isTutorialShown = prefs.getBoolean(PREF_KEY_TUTORIAL_SHOWN, false);
        if (!isTutorialShown) {
            DialogFragment dialog = (DialogFragment) DialogFragment.instantiate(getActivity(), TutorialDialogFragment.class.getName());
            dialog.show(getFragmentManager(), null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mWebView = new WebView(getActivity());
        checkWebViewVersion();
        mIsAttached = true;

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
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_KEY_GAME_STATE, mGameState.toString()).apply();
        Log.d(TAG, "Saved gameState to preferences");

        mIsAttached = false;
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    @Override
    public void onWebSocketMessageReceived(Activity context, JSONObject s) {
        super.onWebSocketMessageReceived(context, s);
        if (!mIsAttached) {
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
        builder.setMessage(R.string.web_view_needs_update)
                .setTitle(R.string.update_web_view)
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
        builder.setMessage(R.string.web_view_not_installed)
                .setTitle(R.string.incompatible_device)
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

    public static class TutorialDialogFragment extends DialogFragment {
        @Override
        public void onStart() {
            super.onStart();
            if (getDialog() == null) {
                return;
            }

            // This is in dp unit.
            DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            float targetWidth = Math.min(382, dpWidth - 16);

            // For some reason, Android doesn't honor layout parameters in the layout file.
            getDialog().getWindow().setLayout(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetWidth, getResources().getDisplayMetrics()),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawable(null);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.collection_fragment_tutorial_dialog, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TutorialDialogFragment.this.dismiss();
                }
            });

            // Set custom fonts for our dialog.
            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "nova3.ttf");
            TextView para1 = (TextView) view.findViewById(R.id.collection_tutorial_para_1);
            TextView para2 = (TextView) view.findViewById(R.id.collection_tutorial_para_2);
            TextView para3 = (TextView) view.findViewById(R.id.collection_tutorial_para_3);
            TextView para4 = (TextView) view.findViewById(R.id.collection_tutorial_para_4);
            para1.setLineSpacing(0, 1.25f);
            para1.setTypeface(typeface);
            para2.setLineSpacing(0, 1.25f);
            para2.setTypeface(typeface);
            para3.setLineSpacing(0, 1.25f);
            para3.setTypeface(typeface);
            para4.setLineSpacing(0, 1.25f);
            para4.setTypeface(typeface);

            // Append an arrow after the paragraph.
            SpannableString lastPara = new SpannableString(para4.getText() + " \u22b2");
            lastPara.setSpan(new ForegroundColorSpan(
                            ContextCompat.getColor(getActivity(), R.color.primary)),
                    para4.getText().length() + 1,
                    para4.getText().length() + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            para4.setText(lastPara, TextView.BufferType.SPANNABLE);

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view);
            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(PREF_KEY_TUTORIAL_SHOWN, true).apply();
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
