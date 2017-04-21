package edu.cmu.etc.fanfare.playbook;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Stack;

import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.DRAWER_ITEM_COLLECTION;
import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.DRAWER_ITEM_LEADERBOARD;
import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.DRAWER_ITEM_PREDICTION;
import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.DRAWER_ITEM_TREASURE_HUNT;
import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.DRAWER_ITEM_TROPHY;
import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.getDrawerItems;

public class AppActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_DRAWER_ITEM = "intentExtraDrawerItem";
    public static final String INTENT_EXTRA_GCM_PLAYS_CREATED = "intentExtraGcmPlaysCreated";
    public static final String INTENT_EXTRA_GCM_LOCK_PREDICTIONS = "intentExtraGcmLockPredictions";
    public static final String INTENT_EXTRA_GCM_CLEAR_PREDICTIONS = "intentExtraGcmClearPredictions";

    private static final int FRAGMENT_PREDICTION = 0;
    private static final int FRAGMENT_COLLECTION = 1;
    private static final int FRAGMENT_TREASURE_HUNT = 2;
    private static final int FRAGMENT_LEADERBOARD = 3;
    private static final int FRAGMENT_TROPHY = 4;
    private static final int FRAGMENT_SEAT_SELECT = 5;

    private static final String TAG = "AppActivity";
    private static final String STATE_SELECTED_SECTION = "selected_section";
    private static final int DEFAULT_ITEM = FRAGMENT_PREDICTION;
    private static final String PLAYBOOK_API_URL = "ws://" +
            BuildConfig.PLAYBOOK_API_HOST + ":" +
            BuildConfig.PLAYBOOK_API_PORT;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar mToolbar;
    private OutlinedTextView mToolbarTextView;
    private Stack<Integer> mBackStack = new Stack<>();
    private int mLastSelectedItem = DEFAULT_ITEM;
    private SparseArray<PlaybookFragment> mFragments = new SparseArray<>();

    private int mSection = -1;
    public static boolean isInForeground = false;
    public static boolean sectionFlag = false;
    public static int selectedItem = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity);

        // Get the section ID
        if (savedInstanceState != null) {
            mSection = savedInstanceState.getInt(STATE_SELECTED_SECTION);
        }

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        DrawerItemAdapter.DrawerItem[] menuItems = getDrawerItems(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Setup the menu items
        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerItemAdapter(this,
                R.layout.drawer_list_item, menuItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Replace title with our own custom one.
        // We need to set a title, because the title TextView is lazily created.
        mToolbar.setTitle(getString(R.string.app_name));
        try {
            Field field = mToolbar.getClass().getDeclaredField("mTitleTextView");
            field.setAccessible(true);

            Field appearanceField = mToolbar.getClass().getDeclaredField("mTitleTextAppearance");
            appearanceField.setAccessible(true);

            AppCompatTextView titleView = (AppCompatTextView) field.get(mToolbar);
            mToolbarTextView = new OutlinedTextView(this);
            mToolbarTextView.setSingleLine();
            mToolbarTextView.setEllipsize(TextUtils.TruncateAt.END);
            mToolbarTextView.setTextAppearance(this, appearanceField.getInt(mToolbar));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mToolbarTextView.setLetterSpacing(0.12f);
            }

            Typeface typeface = Typeface.createFromAsset(getAssets(), "rockb.ttf");
            mToolbarTextView.setTypeface(typeface);
            mToolbarTextView.setAllCaps(true);

            field.set(mToolbar, mToolbarTextView);
            mToolbar.removeView(titleView);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, e.toString());
        }

        // Create the fragments.
        mFragments.put(FRAGMENT_PREDICTION, new PredictionFragment());
        mFragments.put(FRAGMENT_LEADERBOARD, new LeaderboardFragment());
        mFragments.put(FRAGMENT_COLLECTION, new CollectionFragment());
        mFragments.put(FRAGMENT_TROPHY, new TrophyFragment());
        mFragments.put(FRAGMENT_SEAT_SELECT, new SeatSelectFragment());
        mFragments.put(FRAGMENT_TREASURE_HUNT, new TreasureHuntFragment());

        // Set the home screen
        selectItem(DEFAULT_ITEM);

        // Initialize WebSocket connection.
        WebSocketHandler webSocketHandler = new WebSocketHandler();
        Intent intent = getIntent();

        if (intent.hasExtra(INTENT_EXTRA_GCM_PLAYS_CREATED)) {
            String message = intent.getStringExtra(INTENT_EXTRA_GCM_PLAYS_CREATED);
            webSocketHandler.onStringAvailable(message);
        }

        if (intent.hasExtra(INTENT_EXTRA_GCM_LOCK_PREDICTIONS)) {
            String message = intent.getStringExtra(INTENT_EXTRA_GCM_LOCK_PREDICTIONS);
            webSocketHandler.onStringAvailable(message);
        }

        if (intent.hasExtra(INTENT_EXTRA_GCM_CLEAR_PREDICTIONS)) {
            String message = intent.getStringExtra(INTENT_EXTRA_GCM_CLEAR_PREDICTIONS);
            webSocketHandler.onStringAvailable(message);
        }

        AsyncHttpClient.getDefaultInstance().websocket(PLAYBOOK_API_URL, null, webSocketHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        isInForeground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isInForeground = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_SECTION, mSection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (mBackStack.size() > 1) {
            int lastItem = mBackStack.pop();
            selectItem(lastItem, false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        selectItem(intent.getIntExtra(INTENT_EXTRA_DRAWER_ITEM, DEFAULT_ITEM));
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        selectItem(position, true);
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position, boolean addToBackStack) {
        // Create a new fragment based on selected position
        PlaybookFragment fragment;
        switch (position) {
            case DRAWER_ITEM_PREDICTION:
            case DRAWER_ITEM_COLLECTION:
            case DRAWER_ITEM_TROPHY:
                fragment = mFragments.get(position);
                break;
            case DRAWER_ITEM_LEADERBOARD:
                Log.i("test", "Entering new collection fragment");
                //fragment = new CollectionFragment();  //for test js collection
                fragment = mFragments.get(position);
                break;
            case DRAWER_ITEM_TREASURE_HUNT:
                if(!sectionFlag){
                    fragment = mFragments.get(FRAGMENT_SEAT_SELECT);
                    //sectionFlag = true;
                }
                else {
                    fragment = mFragments.get(FRAGMENT_TREASURE_HUNT);
                }
                Log.i("sectionFlag", Boolean.toString(sectionFlag));
                break;
            default:
                fragment = mFragments.get(DEFAULT_ITEM);
        };

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Add the previous selection to the stack.
        if (addToBackStack) {
            mBackStack.push(mLastSelectedItem);
            mLastSelectedItem = position;
        }

        // Close the drawer
        selectedItem = position;
        mDrawerLayout.closeDrawer(mDrawerList);
        updateUIForItem(position);
    }

    private void updateUIForItem(int position) {
        // Highlight the selected item in the drawer.
        mDrawerList.setItemChecked(position, true);

        // Set the name in the action bar.
        // We can't use mToolbar here: https://code.google.com/p/android/issues/detail?id=77763
        DrawerItemAdapter.DrawerItem[] drawerItems = getDrawerItems(this);
        getSupportActionBar().setTitle(drawerItems[position].fragmentTitle);

        // Change color.
        mToolbar.setBackgroundColor(drawerItems[position].fragmentColor);
        mToolbar.setTitleTextColor(drawerItems[position].titleTextColor);
        mToolbarTextView.setInnerStrokeColor(drawerItems[position].fragmentColor);
        mToolbarTextView.setOuterStrokeColor(drawerItems[position].titleTextColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(drawerItems[position].fragmentColorDark);
        }
    }

    private class WebSocketHandler implements AsyncHttpClient.WebSocketConnectCallback, WebSocket.StringCallback {
        @Override
        public void onCompleted(Exception e, WebSocket webSocket) {
            if (e != null) {
                e.printStackTrace();
                return;
            }

            Log.d(TAG, "Connected to WebSocket server at " + PLAYBOOK_API_URL);
            webSocket.setStringCallback(this);
        }

        @Override
        public void onStringAvailable(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                Log.d(TAG, jsonObject.getString("event"));
                for (int i = 0; i < mFragments.size(); i++) {
                    PlaybookFragment fragment = mFragments.valueAt(i);
                    fragment.onWebSocketMessageReceived(AppActivity.this, jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
