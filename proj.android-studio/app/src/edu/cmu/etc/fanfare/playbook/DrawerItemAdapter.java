package edu.cmu.etc.fanfare.playbook;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class DrawerItemAdapter extends ArrayAdapter<DrawerItemAdapter.DrawerItem> {
    static final int DRAWER_ITEM_PREDICTION = 0;
    static final int DRAWER_ITEM_COLLECTION = 1;
    static final int DRAWER_ITEM_TREASURE_HUNT = 2;
    static final int DRAWER_ITEM_LEADERBOARD = 3;
    static final int DRAWER_ITEM_TROPHY = 4;

    DrawerItemAdapter(Context context, int resource, DrawerItem[] objects) {
        super(context, resource, objects);
    }

    private class ViewHolder {
        private TextView mTextView;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.drawer_list_item, parent, false);

            viewHolder = new DrawerItemAdapter.ViewHolder();
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.text);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DrawerItemAdapter.ViewHolder) convertView.getTag();
        }

        DrawerItem item = getItem(position);
        if (item != null) {
            viewHolder.mTextView.setText(item.text);
            viewHolder.mTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, item.icon, null, null);

            ColorStateList stateList;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                stateList = getContext().getResources().getColorStateList(R.color.drawer_list_item_selector, getContext().getTheme());
                viewHolder.mTextView.setCompoundDrawableTintList(stateList);
            }

        }

        return convertView;
    }

    static DrawerItem[] getDrawerItems(Context context) {
        Resources resources = context.getResources();
        String[] menuItems = resources.getStringArray(R.array.menu_items);
        TypedArray menuItemIcons = resources.obtainTypedArray(R.array.menu_item_icons);
        String[] menuItemFragmentTitle = resources.getStringArray(R.array.menu_item_fragment_title);
        TypedArray menuItemFragmentColor = resources.obtainTypedArray(R.array.menu_item_fragment_color);
        TypedArray menuItemFragmentColorDark = resources.obtainTypedArray(R.array.menu_item_fragment_color_dark);
        TypedArray menuItemTitleTextColor = resources.obtainTypedArray(R.array.menu_item_title_text_color);
        DrawerItem[] drawerItems = new DrawerItem[menuItems.length];

        for (int i = 0; i < menuItems.length; i++) {
            DrawerItem drawerItem = new DrawerItem();
            drawerItem.text = menuItems[i];
            drawerItem.icon = menuItemIcons.getDrawable(i);
            drawerItem.fragmentTitle = menuItemFragmentTitle[i];

            drawerItem.fragmentColor = menuItemFragmentColor.getColor(i, ContextCompat.getColor(context, R.color.primary));
            drawerItem.fragmentColorDark = menuItemFragmentColorDark.getColor(i, ContextCompat.getColor(context, R.color.primary_dark));

            // Resolve attr values through the theme.
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(menuItemTitleTextColor.peekValue(i).data, typedValue, true);
            TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{ menuItemTitleTextColor.peekValue(i).data });
            drawerItem.titleTextColor = arr.getColor(0, -1);
            arr.recycle();

            drawerItems[i] = drawerItem;
        }

        menuItemIcons.recycle();
        menuItemFragmentColor.recycle();
        menuItemFragmentColorDark.recycle();
        menuItemTitleTextColor.recycle();
        return drawerItems;
    }

    static class DrawerItem {
        String text;
        Drawable icon;
        String fragmentTitle;
        int fragmentColor;
        int fragmentColorDark;
        int titleTextColor;
    }
}
