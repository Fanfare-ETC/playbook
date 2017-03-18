package edu.cmu.etc.fanfare.playbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.gcm.GcmReceiver;

public class SeatSelectActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stadium_select_activity);


        /********************************Test for GCM**************************************/
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

        Intent intent1 = new Intent(this, GcmReceiver.class);
        startService(intent1);
        /********************************************************************************/

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(R.string.select_your_seat);
    }
}
