package com.psu.sweng888.gthenewapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.psu.sweng888.gthenewapp.MainActivity;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.auth.AuthHelper;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "SplashActivity created");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Splash delay completed, checking authentication");
                
                // Use AuthHelper to check if user is signed in (Firebase or local)
                AuthHelper authHelper = new AuthHelper(SplashActivity.this);
                
                Intent intent;
                if (authHelper.isUserSignedIn()) {
                    // User is signed in (Firebase or local), go to main activity
                    String userEmail = authHelper.getCurrentUserEmail();
                    Log.d(TAG, "User is signed in, email: " + userEmail);
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("username", userEmail);
                } else {
                    // User is not signed in, go to login activity
                    Log.d(TAG, "User is not signed in, going to LoginActivity");
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                
                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }
}
