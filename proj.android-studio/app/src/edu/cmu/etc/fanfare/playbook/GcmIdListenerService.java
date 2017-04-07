package edu.cmu.etc.fanfare.playbook;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * The Listener Service for Registration ID's refresh.
 */
public class GcmIdListenerService extends FirebaseInstanceIdService {
    private static final String TAG = GcmIdListenerService.class.getSimpleName();

    /**
     * Starts the Registration Service when the Registration ID has to be refreshed.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        //sendRegistrationToServer(refreshedToken);
    }
}
