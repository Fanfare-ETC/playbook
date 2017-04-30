package edu.cmu.etc.fanfare.playbook;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WelcomeFragment extends PlaybookFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.welcome_fragment, container, false);

        // Sets the font for everything.
        TextView textView = (TextView) view.findViewById(R.id.welcome_three_minigames);
        TextView prediction = (TextView) view.findViewById(R.id.welcome_prediction);
        TextView collection = (TextView) view.findViewById(R.id.welcome_collection);
        TextView hotCold = (TextView) view.findViewById(R.id.welcome_hotcold);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "nova3.ttf");
        textView.setTypeface(typeface);
        prediction.setTypeface(typeface);
        collection.setTypeface(typeface);
        hotCold.setTypeface(typeface);

        prediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AppActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_PREDICTION);
                startActivity(intent);
            }
        });

        collection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AppActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_COLLECTION);
                startActivity(intent);
            }
        });

        hotCold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AppActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(AppActivity.INTENT_EXTRA_DRAWER_ITEM, DrawerItemAdapter.DRAWER_ITEM_TREASURE_HUNT);
                startActivity(intent);
            }
        });

        return view;
    }
}
