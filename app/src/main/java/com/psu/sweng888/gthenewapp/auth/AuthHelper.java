package com.psu.sweng888.gthenewapp.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthHelper {
    private static final String TAG = "AuthHelper";
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    
    private FirebaseAuth mAuth;
    private SharedPreferences mPrefs;
    private Context mContext;

    public AuthHelper(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "AuthHelper initialized");
    }

    public void signUp(String email, String password, OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "signUp called with email: " + email);
        // First try Firebase authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Firebase signUp task completed, success: " + task.isSuccessful());
                    if (task.isSuccessful()) {
                        // Save credentials locally for backup
                        saveCredentials(email, password);
                        if (listener != null) {
                            listener.onComplete(task);
                        }
                    } else {
                        // If Firebase fails due to reCAPTCHA, create a local account
                        if (task.getException() != null && 
                            task.getException().getMessage().contains("CONFIGURATION_NOT_FOUND")) {
                            Log.w(TAG, "Firebase reCAPTCHA not configured, creating local account");
                            createLocalAccount(email, password, listener);
                        } else {
                            Log.e(TAG, "Firebase signUp failed", task.getException());
                            if (listener != null) {
                                listener.onComplete(task);
                            }
                        }
                    }
                });
    }

    public void signIn(String email, String password, OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "signIn called with email: " + email);
        // First try Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Firebase signIn task completed, success: " + task.isSuccessful());
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase signIn successful");
                        if (listener != null) {
                            listener.onComplete(task);
                        }
                    } else {
                        // If Firebase fails, try local authentication
                        if (task.getException() != null && 
                            task.getException().getMessage().contains("CONFIGURATION_NOT_FOUND")) {
                            Log.w(TAG, "Firebase reCAPTCHA not configured, trying local auth");
                            checkLocalCredentials(email, password, listener);
                        } else {
                            Log.e(TAG, "Firebase signIn failed", task.getException());
                            if (listener != null) {
                                listener.onComplete(task);
                            }
                        }
                    }
                });
    }

    private void createLocalAccount(String email, String password, OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "Creating local account for: " + email);
        // Save credentials locally
        saveCredentials(email, password);
        
        // Create a local auth result
        LocalAuthResult localResult = new LocalAuthResult(email);
        Task<AuthResult> mockTask = Tasks.forResult(localResult);
        
        if (listener != null) {
            listener.onComplete(mockTask);
        }
    }

    private void checkLocalCredentials(String email, String password, OnCompleteListener<AuthResult> listener) {
        Log.d(TAG, "Checking local credentials for: " + email);
        String savedEmail = mPrefs.getString(KEY_EMAIL, "");
        String savedPassword = mPrefs.getString(KEY_PASSWORD, "");
        
        Log.d(TAG, "Saved email: " + savedEmail + ", provided email: " + email);
        Log.d(TAG, "Password match: " + password.equals(savedPassword));
        
        if (email.equals(savedEmail) && password.equals(savedPassword)) {
            // Local authentication successful
            Log.d(TAG, "Local authentication successful");
            LocalAuthResult localResult = new LocalAuthResult(email);
            Task<AuthResult> mockTask = Tasks.forResult(localResult);
            
            if (listener != null) {
                listener.onComplete(mockTask);
            }
        } else {
            // Local authentication failed
            Log.d(TAG, "Local authentication failed");
            Task<AuthResult> mockTask = Tasks.forException(new Exception("Invalid credentials"));
            if (listener != null) {
                listener.onComplete(mockTask);
            }
        }
    }

    private void saveCredentials(String email, String password) {
        Log.d(TAG, "Saving credentials for: " + email);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public boolean isUserSignedIn() {
        boolean firebaseSignedIn = mAuth.getCurrentUser() != null;
        boolean localSignedIn = !mPrefs.getString(KEY_EMAIL, "").isEmpty();
        Log.d(TAG, "isUserSignedIn - Firebase: " + firebaseSignedIn + ", Local: " + localSignedIn);
        return firebaseSignedIn || localSignedIn;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Getting Firebase user email: " + user.getEmail());
            return user.getEmail();
        }
        String localEmail = mPrefs.getString(KEY_EMAIL, "");
        Log.d(TAG, "Getting local user email: " + localEmail);
        return localEmail;
    }

    public void signOut() {
        Log.d(TAG, "Signing out user");
        mAuth.signOut();
        // Clear local credentials
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.clear();
        editor.apply();
    }

    // Simple user info class for local authentication
    private static class LocalUser {
        private String email;
        private String uid;

        public LocalUser(String email) {
            this.email = email;
            this.uid = "local_user_" + email.hashCode();
        }

        public String getEmail() {
            return email;
        }

        public String getUid() {
            return uid;
        }
    }

    // Custom AuthResult for local authentication
    private static class LocalAuthResult implements AuthResult {
        private LocalUser user;

        public LocalAuthResult(String email) {
            this.user = new LocalUser(email);
        }

        @Override
        public FirebaseUser getUser() {
            return null; // Return null for local accounts
        }

        @Override
        public com.google.firebase.auth.AuthCredential getCredential() {
            return null;
        }

        @Override
        public com.google.firebase.auth.AdditionalUserInfo getAdditionalUserInfo() {
            return null;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {
            // Empty implementation for Parcelable
        }

        public LocalUser getLocalUser() {
            return user;
        }
    }
} 