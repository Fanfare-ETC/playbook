package edu.cmu.etc.fanfare.playbook;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
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

public class CollectionFragment extends WebViewFragment {
    private static final String TAG = CollectionFragment.class.getSimpleName();

    private static final String PREF_NAME = "collection";
    private static final String PREF_KEY_GAME_STATE = "gameState";
    private static final String PREF_KEY_TUTORIAL_SHOWN = "tutorialShown";

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
            showTutorial();
        }

        // Declare that we have an options menu.
        setHasOptionsMenu(true);

        // Mark ourselves as running.
        mIsAttached = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        WebView webView = getWebView();

        if (isWebViewCompatible()) {
            webView.addJavascriptInterface(new JavaScriptInterface(), "PlaybookBridge");
            webView.loadUrl("file:///android_asset/collection/index.html");
        }

        return webView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttached = false;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save game state to preferences.
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_KEY_GAME_STATE, mGameState.toString()).apply();
        Log.d(TAG, "Saved gameState to preferences");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_collection, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_collection_tutorial:
                showTutorial();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        state.put("activeCard", JSONObject.NULL);
        state.put("incomingCards", new JSONArray());
        state.put("goal", JSONObject.NULL);
        state.put("cardSlots", cardSlots);
        state.put("cardsMatchingSelectedGoal", new JSONArray());
        state.put("selectedGoal", JSONObject.NULL);
        state.put("score", 0);
        return state;
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
        getWebView().evaluateJavascript(js, null);
    }

    private void showTutorial() {
        DialogFragment dialog = (DialogFragment) DialogFragment.instantiate(getActivity(), TutorialDialogFragment.class.getName());
        dialog.show(getFragmentManager(), null);
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
            para1.setLineSpacing(0, 1.25f);
            para1.setTypeface(typeface);
            para2.setLineSpacing(0, 1.25f);
            para2.setTypeface(typeface);
            para3.setLineSpacing(0, 1.25f);
            para3.setTypeface(typeface);

            // Append an arrow after the paragraph.
            SpannableString lastPara = new SpannableString(para3.getText() + " \u22b2");
            lastPara.setSpan(new ForegroundColorSpan(
                            ContextCompat.getColor(getActivity(), R.color.primary)),
                    para3.getText().length() + 1,
                    para3.getText().length() + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            para3.setText(lastPara, TextView.BufferType.SPANNABLE);

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

    public static class TrophyAcquiredDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("You got a trophy!")
                    .setMessage("Do you want to see it in the Trophy Case?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), AppActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_TROPHY);
                            startActivity(intent);
                            dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .create();
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
            return PlaybookApplication.getPlayerID();
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
        public void notifyLoaded(String state) {
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

        @JavascriptInterface
        public void goToLeaderboard() {
            Intent intent = new Intent(getActivity(), AppActivity.class);
            intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_LEADERBOARD);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        @JavascriptInterface
        public void goToTrophyCase() {
            Intent intent = new Intent(getActivity(), AppActivity.class);
            intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_TROPHY);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

}
