package edu.cmu.etc.fanfare.playbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import static edu.cmu.etc.fanfare.playbook.PlaybookApplication.PREF_KEY_IS_ONBOARDING_COMPLETE;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    public static FirebaseUser acct;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Is it the first time we're using the app?
        // If so, show the onboarding flow.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isOnboardingComplete = prefs.getBoolean(PREF_KEY_IS_ONBOARDING_COMPLETE, false);
        if (!isOnboardingComplete) {
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }

            private void signIn() {
                //Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                //startActivityForResult(signInIntent, RC_SIGN_IN);
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.Theme_AppCompat_Dialog_Alert)
                        .setIsSmartLockEnabled(false)
                        .setProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()
                        ))
                        .build();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // Set up Google Cloud Messaging.
        Intent intent = new Intent(this, GcmRegistrationIntentService.class);
        startService(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            handleSignInResponse(response, resultCode);
        }
    }

    private void handleSignInResponse(IdpResponse response, int resultCode) {
        Log.d(TAG, "handleSignInResponse:" + resultCode);
        if (resultCode == ResultCodes.OK) {
            // Signed in successfully, show authenticated UI.
            acct = FirebaseAuth.getInstance().getCurrentUser();
            if (acct != null) {
                PlaybookApplication.setPlayerName(acct.getDisplayName());
                PlaybookApplication.setPlayerID(acct.getUid());
                //insert player
                BackgroundWorker backgroundWorker = new BackgroundWorker();
                backgroundWorker.execute("section");

                Intent intent = new Intent(this, AppActivity.class);
                intent.putExtras(getIntent());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to sign in using Google.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to sign in using Google.", Toast.LENGTH_SHORT).show();
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }
}
