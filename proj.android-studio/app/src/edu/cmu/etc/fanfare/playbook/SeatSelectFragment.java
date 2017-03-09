package edu.cmu.etc.fanfare.playbook;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

public class SeatSelectFragment extends Fragment  {
    public static int section;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view=inflater.inflate(R.layout.stadium_select_activity, container, false);

        //create a list box to enter section

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("1");
        arrayAdapter.add("2");
        arrayAdapter.add("3");

        return view;
    }



}
