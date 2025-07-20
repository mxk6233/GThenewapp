package com.psu.sweng888.gthenewapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FirebaseDatabaseManager firebaseDatabaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "HomeFragment onCreateView called");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize Firebase manager
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        
        // Set up the send email button
        Button sendEmailButton = view.findViewById(R.id.send_email_button);
        if (sendEmailButton != null) {
            sendEmailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Send Email button clicked");
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "No email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Log.d(TAG, "Send Email button set up successfully");
        } else {
            Log.e(TAG, "Send Email button not found in layout");
        }
        
        // Set up the populate Firebase button
        Button populateButton = view.findViewById(R.id.populate_firebase_button);
        if (populateButton != null) {
            populateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Populate Firebase button clicked");
                    populateFirebaseWithSampleBooks();
                }
            });
            Log.d(TAG, "Populate Firebase button set up successfully");
        } else {
            Log.e(TAG, "Populate Firebase button not found in layout");
        }
        
        // Set up the sync SQLite button
        Button syncButton = view.findViewById(R.id.sync_sqlite_button);
        if (syncButton != null) {
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Sync SQLite button clicked");
                    syncSQLiteToFirebase();
                }
            });
            Log.d(TAG, "Sync SQLite button set up successfully");
        } else {
            Log.e(TAG, "Sync SQLite button not found in layout");
        }
        
        Log.d(TAG, "HomeFragment view created successfully");
        return view;
    }
    
    private void populateFirebaseWithSampleBooks() {
        Log.d(TAG, "Attempting to populate Firebase with sample books...");
        
        if (!firebaseDatabaseManager.isUserAuthenticated()) {
            Toast.makeText(getActivity(), "Please sign in to Firebase first", Toast.LENGTH_LONG).show();
            Log.w(TAG, "User not authenticated with Firebase");
            return;
        }
        
        Toast.makeText(getActivity(), "Adding sample books to Firebase...", Toast.LENGTH_SHORT).show();
        
        firebaseDatabaseManager.populateSampleBooks(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Successfully populated Firebase with sample books");
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Sample books added to Firebase successfully! Check the Books section.", Toast.LENGTH_LONG).show();
                });
            } else {
                Log.e(TAG, "Failed to populate Firebase", task.getException());
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to add sample books: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void syncSQLiteToFirebase() {
        Log.d(TAG, "Attempting to sync SQLite books to Firebase...");
        
        if (!firebaseDatabaseManager.isUserAuthenticated()) {
            Toast.makeText(getActivity(), "Please sign in to Firebase first", Toast.LENGTH_LONG).show();
            Log.w(TAG, "User not authenticated with Firebase");
            return;
        }
        
        Toast.makeText(getActivity(), "Syncing local books to Firebase...", Toast.LENGTH_SHORT).show();
        
        firebaseDatabaseManager.syncSQLiteToFirebase(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Successfully synced SQLite books to Firebase");
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Local books synced to Firebase successfully! Check the Books section.", Toast.LENGTH_LONG).show();
                });
            } else {
                Log.e(TAG, "Failed to sync to Firebase", task.getException());
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to sync books: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "HomeFragment onViewCreated called");
    }
}
