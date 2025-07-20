package com.psu.sweng888.gthenewapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.psu.sweng888.gthenewapp.MainActivity;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.auth.AuthHelper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText mEditTextEmail, mEditTextPassword;
    private Button mButtonLogin, mButtonSignUp;
    private FirebaseAuth mAuth;
    private AuthHelper mAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthHelper = new AuthHelper(this);

        // Initialize views
        mEditTextEmail = findViewById(R.id.edit_text_email);
        mEditTextPassword = findViewById(R.id.edit_text_password);
        mButtonLogin = findViewById(R.id.button_login);
        mButtonSignUp = findViewById(R.id.button_sign_up);

        Log.d(TAG, "LoginActivity created, views initialized");

        // Set click listeners
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button clicked");
                String email = mEditTextEmail.getText().toString().trim();
                String password = mEditTextPassword.getText().toString().trim();
                
                Log.d(TAG, "Email: " + email + ", Password length: " + password.length());
                
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Empty fields detected");
                    return;
                }

                Log.d(TAG, "Starting sign in process");
                
                // For testing purposes, let's try a simple test login first
                if (email.equals("test@test.com") && password.equals("123456")) {
                    Log.d(TAG, "Test login successful");
                    Toast.makeText(LoginActivity.this, "Test login successful!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity(email);
                    return;
                }
                
                signIn(email, password);
            }
        });

        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sign up button clicked");
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
        // Check if user is signed in
        if (mAuthHelper.isUserSignedIn()) {
            Log.d(TAG, "User already signed in, redirecting to MainActivity");
            // User is already signed in, go to main activity
            navigateToMainActivity(mAuthHelper.getCurrentUserEmail());
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn method called with email: " + email);
        mAuthHelper.signIn(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "Sign in task completed, success: " + task.isSuccessful());
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "Sign in successful, navigating to MainActivity");
                    FirebaseUser user = task.getResult().getUser();
                    String email = (user != null) ? user.getEmail() : mEditTextEmail.getText().toString().trim();
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity(email);
                } else {
                    // If sign in fails, display a message to the user.
                    String errorMessage = "Authentication failed";
                    if (task.getException() != null) {
                        errorMessage = "Authentication failed: " + task.getException().getMessage();
                        Log.e(TAG, "Sign in failed", task.getException());
                    }
                    Log.d(TAG, "Sign in failed: " + errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToMainActivity(String email) {
        Log.d(TAG, "navigateToMainActivity called with email: " + email);
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", email);
            Log.d(TAG, "Starting MainActivity with intent");
            startActivity(intent);
            Log.d(TAG, "MainActivity started successfully");
            finish();
            Log.d(TAG, "LoginActivity finished");
        } catch (Exception e) {
            Log.e(TAG, "Error starting MainActivity", e);
            Toast.makeText(LoginActivity.this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
