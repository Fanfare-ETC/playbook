package edu.cmu.etc.fanfare.playbook;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;

import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.DRAWER_ITEM_LEADERBOARD;
import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.DRAWER_ITEM_TROPHY;
import static edu.cmu.etc.fanfare.playbook.DrawerItemAdapter.getDrawerItems;

public class OnboardingActivity extends AppCompatActivity {
    private static final String TAG = OnboardingActivity.class.getSimpleName();
    public static final String PREF_KEY_IS_ONBOARDING_COMPLETE = "isOnboardingComplete";

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_activity);

        // Find our UI elements on screen.
        final ViewPager pager = (ViewPager) findViewById(R.id.onboarding_view_pager);
        Button skipBtn = (Button) findViewById(R.id.onboarding_navigation_btn_skip);
        Button nextBtn = (Button) findViewById(R.id.onboarding_navigation_btn_next);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.onboarding_toolbar);
        setSupportActionBar(toolbar);
        try {
            Field field = toolbar.getClass().getDeclaredField("mTitleTextView");
            field.setAccessible(true);

            Field appearanceField = toolbar.getClass().getDeclaredField("mTitleTextAppearance");
            appearanceField.setAccessible(true);

            TextView titleView = (TextView) field.get(toolbar);
            OutlinedTextView toolbarTextView = new OutlinedTextView(this);
            toolbarTextView.setSingleLine();
            toolbarTextView.setEllipsize(TextUtils.TruncateAt.END);
            toolbarTextView.setTextAppearance(this, appearanceField.getInt(toolbar));
            toolbarTextView.setTextColor(ContextCompat.getColor(this, android.R.color.primary_text_dark));
            toolbarTextView.setInnerStrokeColor(ContextCompat.getColor(this, R.color.tertiary));
            toolbarTextView.setOuterStrokeColor(ContextCompat.getColor(this, android.R.color.primary_text_dark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toolbarTextView.setLetterSpacing(0.12f);
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.tertiary_dark));
            }

            Typeface typeface = Typeface.createFromAsset(getAssets(), "rockb.ttf");
            toolbarTextView.setTypeface(typeface);
            toolbarTextView.setAllCaps(true);

            field.set(toolbar, toolbarTextView);
            toolbar.removeView(titleView);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, e.toString());
        }

        DrawerItemAdapter.DrawerItem[] menuItems = getDrawerItems(this);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
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
        drawerList.setAdapter(new DrawerItemAdapter(this,
                R.layout.drawer_list_item, menuItems));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set up the buttons.
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOnboarding();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pager.getCurrentItem() == 1) {
                    finishOnboarding();
                } else {
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                }
            }
        });

        // Set up the adapter that supplies the pager with fragments.
        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0: return new StepOneFragment();
                    case 1: return new StepTwoFragment();
                    default: return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };

        pager.setAdapter(adapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private void finishOnboarding() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(PREF_KEY_IS_ONBOARDING_COMPLETE, true).apply();

        // Launch the login activity.
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            finishOnboarding();
        }
    }

    public static class OverlayView extends View {
        private Rect mDrawerToggleRect;
        private Point mDrawerToggleParaPosition;
        private Point mLeaderboardItemPosition;
        private Point mLeaderboardParaPosition;
        private Point mTrophyItemPosition;
        private Point mTrophyParaPosition;
        private Paint mPaint;

        public OverlayView(Context context) {
            super(context);
            createPaint();
        }

        public OverlayView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            createPaint();
        }

        @Override
        public void onDraw(Canvas canvas) {
            // Circle the drawer toggle.
            canvas.drawCircle(
                    mDrawerToggleRect.centerX(),
                    mDrawerToggleRect.centerY(),
                    mDrawerToggleRect.width() / 2,
                    mPaint
            );

            // Point to the drawer toggle.
            canvas.drawLine(
                    mDrawerToggleParaPosition.x, mDrawerToggleParaPosition.y,
                    (float) (mDrawerToggleRect.centerX() + Math.sin(Math.toRadians(45.0f)) * mDrawerToggleRect.width() / 2.0f),
                    (float) (mDrawerToggleRect.centerY() + Math.cos(Math.toRadians(45.0f)) * mDrawerToggleRect.height() / 2.0f),
                    mPaint
            );

            // Point to the leaderboard button.
            canvas.drawLine(
                    mLeaderboardParaPosition.x, mLeaderboardParaPosition.y,
                    mLeaderboardItemPosition.x, mLeaderboardItemPosition.y,
                    mPaint
            );

            // Point to the trophy case button.
            canvas.drawLine(
                    mTrophyParaPosition.x, mTrophyParaPosition.y,
                    mTrophyItemPosition.x, mTrophyItemPosition.y,
                    mPaint
            );
        }

        public void setDrawerToggleRect(Rect rect) {
            mDrawerToggleRect = rect;
        }

        public void setDrawerToggleParaPosition(Point position) {
            mDrawerToggleParaPosition = position;
        }

        public void setLeaderboardItemPosition(Point position) {
            mLeaderboardItemPosition = position;
        }

        public void setLeaderboardParaPosition(Point position) {
            mLeaderboardParaPosition = position;
        }

        public void setTrophyItemPosition(Point position) {
            mTrophyItemPosition = position;
        }

        public void setTrophyParaPosition(Point position) {
            mTrophyParaPosition = position;
        }

        private void createPaint() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.secondary));
            mPaint.setStrokeWidth(3.0f * getResources().getDisplayMetrics().density);
        }
    }

    public static class StepOneFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.onboarding_step_1_fragment, container, false);

            // Sets the font for everything.
            TextView textView = (TextView) view.findViewById(R.id.onboarding_step_1_three_minigames);
            TextView prediction = (TextView) view.findViewById(R.id.onboarding_step_1_prediction);
            TextView collection = (TextView) view.findViewById(R.id.onboarding_step_1_collection);
            TextView hotCold = (TextView) view.findViewById(R.id.onboarding_step_1_hotcold);

            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "nova3.ttf");
            textView.setTypeface(typeface);
            prediction.setTypeface(typeface);
            collection.setTypeface(typeface);
            hotCold.setTypeface(typeface);

            return view;
        }
    }

    public static class StepTwoFragment extends Fragment {
        private ImageView mDrawerPreview;
        private ListView mCachedDrawerList;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.onboarding_step_2_fragment, container, false);

            // Sets the font for everything.
            final TextView para1 = (TextView) view.findViewById(R.id.onboarding_step_2_para_1);
            final TextView para2 = (TextView) view.findViewById(R.id.onboarding_step_2_para_2);
            final TextView para3 = (TextView) view.findViewById(R.id.onboarding_step_2_para_3);
            TextView para4 = (TextView) view.findViewById(R.id.onboarding_step_2_para_4);

            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "nova3.ttf");
            para1.setTypeface(typeface);
            para2.setTypeface(typeface);
            para3.setTypeface(typeface);
            para4.setTypeface(typeface);

            // Draw the drawer into a canvas.
            mDrawerPreview = (ImageView) view.findViewById(R.id.onboarding_drawer_preview);
            mDrawerPreview.setScaleType(ImageView.ScaleType.FIT_START);
            final Bitmap drawerBitmap = drawDrawerIntoBitmap();
            mDrawerPreview.setImageBitmap(drawerBitmap);

            // Get the overlay view.
            final OverlayView overlayView = (OverlayView) getActivity().findViewById(R.id.onboarding_overlay);

            // Get the location of the items we want to point to.
            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.onboarding_toolbar);
            final AppCompatImageButton toggle = (AppCompatImageButton) toolbar.getChildAt(0);

            // Get the ViewPager, change the state of the overlay on scroll.
            ViewPager pager = (ViewPager) getActivity().findViewById(R.id.onboarding_view_pager);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // We need to subtract the status bar height.
                    Rect statusBarRect = new Rect();
                    Window window = getActivity().getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(statusBarRect);

                    // Send the toggle location.
                    int toggleLocation[] = new int[2];
                    toggle.getLocationInWindow(toggleLocation);
                    overlayView.setDrawerToggleRect(new Rect(
                            toggleLocation[0], toggleLocation[1] - statusBarRect.top,
                            toggle.getWidth() + toggleLocation[0],
                            toggle.getHeight() + toggleLocation[1] - statusBarRect.top
                    ));

                    // Send the paragraph position corresponding to the toggle.
                    int toggleParaPosition[] = new int[2];
                    para1.getLocationInWindow(toggleParaPosition);
                    overlayView.setDrawerToggleParaPosition(new Point(
                            toggleParaPosition[0],
                            (toggleParaPosition[1] - statusBarRect.top) + (para1.getHeight() / 2)
                    ));

                    // Figure out how much the drawer preview has been scaled.
                    float drawerPreviewScale = mDrawerPreview.getWidth() / (float) drawerBitmap.getWidth();

                    // Set up a drawerList so we can measure the positions of items.
                    int drawerPreviewPosition[] = new int[2];
                    mDrawerPreview.getLocationInWindow(drawerPreviewPosition);
                    ListView drawerList = makeDrawerList();

                    // Send the position of the leaderboard item.
                    View drawerItem = drawerList.getChildAt(DRAWER_ITEM_LEADERBOARD);
                    overlayView.setLeaderboardItemPosition(new Point(
                            (int) (drawerPreviewPosition[0] + (drawerItem.getRight() * drawerPreviewScale) / 4 * 3),
                            (int) (drawerPreviewPosition[1] + (drawerItem.getTop() * drawerPreviewScale) -
                                    (drawerItem.getHeight() * drawerPreviewScale / 2)) - statusBarRect.top
                    ));

                    // Send the paragraph position corresponding to the button.
                    int leaderboardPosition[] = new int[2];
                    para2.getLocationInWindow(leaderboardPosition);
                    overlayView.setLeaderboardParaPosition(new Point(
                            leaderboardPosition[0],
                            (leaderboardPosition[1] - statusBarRect.top) + (para2.getHeight() / 2)
                    ));

                    // Send the position of the trophy case item.
                    drawerItem = drawerList.getChildAt(DRAWER_ITEM_TROPHY);
                    overlayView.setTrophyItemPosition(new Point(
                            (int) (drawerPreviewPosition[0] + (drawerItem.getRight() * drawerPreviewScale) / 4 * 3),
                            (int) (drawerPreviewPosition[1] + (drawerItem.getTop() * drawerPreviewScale) -
                                    (drawerItem.getHeight() * drawerPreviewScale / 2)) - statusBarRect.top
                    ));

                    // Send the paragraph position corresponding to the button.
                    int trophyPosition[] = new int[2];
                    para3.getLocationInWindow(trophyPosition);
                    overlayView.setTrophyParaPosition(new Point(
                            trophyPosition[0],
                            (trophyPosition[1] - statusBarRect.top) + (para3.getHeight() / 2)
                    ));

                    overlayView.invalidate();
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 1) {
                        overlayView.setVisibility(View.VISIBLE);
                    } else {
                        overlayView.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            return view;
        }

        private ListView makeDrawerList() {
            // Get the drawer list and its layout dimensions.
            if (mCachedDrawerList == null) {
                View layout = LayoutInflater.from(getActivity()).inflate(R.layout.onboarding_activity, null);
                ListView drawerList = (ListView) layout.findViewById(R.id.left_drawer);
                drawerList.setAdapter(new DrawerItemAdapter(getActivity(), R.layout.drawer_list_item, getDrawerItems(getActivity())));
                mCachedDrawerList = drawerList;
                return drawerList;
            } else {
                return mCachedDrawerList;
            }
        }

        private Bitmap drawDrawerIntoBitmap() {
            ListView drawerList = makeDrawerList();
            int width = drawerList.getLayoutParams().width;
            int height = (int) (768 * getResources().getDisplayMetrics().density);

            Rect rect = new Rect();
            rect.set(0, 0, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            int widthSpec = View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.UNSPECIFIED);
            drawerList.measure(widthSpec, heightSpec);
            drawerList.layout(0, 0, rect.width(), rect.height());

            drawerList.draw(canvas);
            return bitmap;
        }
    }
}
