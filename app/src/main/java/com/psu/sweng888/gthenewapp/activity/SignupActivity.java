package com.psu.sweng888.gthenewapp.activity;

import android.content.Intent;
import android.os.Bundle;
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

public class SignupActivity extends AppCompatActivity {

    private EditText mEditTextEmail, mEditTextPassword, mEditTextConfirmPassword;
    private Button mButtonSignUp;
    private FirebaseAuth mAuth;
    private AuthHelper mAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthHelper = new AuthHelper(this);

        // Initialize views
        mEditTextEmail = findViewById(R.id.edit_text_email);
        mEditTextPassword = findViewById(R.id.edit_text_password);
        mEditTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        mButtonSignUp = findViewById(R.id.button_sign_up);

        // Set click listener
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditTextEmail.getText().toString().trim();
                String password = mEditTextPassword.getText().toString().trim();
                String confirmPassword = mEditTextConfirmPassword.getText().toString().trim();
                
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(SignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                createAccount(email, password);
            }
        });
    }

    private void createAccount(String email, String password) {
        mAuthHelper.signUp(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign up success, update UI with the signed-in user's information
                    FirebaseUser user = task.getResult().getUser();
                    String email = (user != null) ? user.getEmail() : mEditTextEmail.getText().toString().trim();
                    Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.putExtra("username", email);
                    startActivity(intent);
                    finish();
                } else {
                    // If sign up fails, display a message to the user.
                    String errorMessage = "Authentication failed";
                    if (task.getException() != null) {
                        String exceptionMessage = task.getException().getMessage();
                        if (exceptionMessage.contains("CONFIGURATION_NOT_FOUND")) {
                            errorMessage = "Firebase configuration error. Please check your Firebase setup.";
                        } else if (exceptionMessage.contains("EMAIL_ALREADY_IN_USE")) {
                            errorMessage = "Email already registered. Please use a different email or login.";
                        } else if (exceptionMessage.contains("WEAK_PASSWORD")) {
                            errorMessage = "Password is too weak. Use at least 6 characters.";
                        } else {
                            errorMessage = "Authentication failed: " + exceptionMessage;
                        }
                    }
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
