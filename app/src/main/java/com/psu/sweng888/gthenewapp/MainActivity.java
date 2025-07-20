package com.psu.sweng888.gthenewapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.psu.sweng888.gthenewapp.activity.LoginActivity;
import com.psu.sweng888.gthenewapp.auth.AuthHelper;
import com.psu.sweng888.gthenewapp.fragments.AddBookFragment;
import com.psu.sweng888.gthenewapp.fragments.BooksListFragment;
import com.psu.sweng888.gthenewapp.fragments.HomeFragment;
import com.psu.sweng888.gthenewapp.fragments.PodcastsListFragment;
import com.psu.sweng888.gthenewapp.fragments.ProductsListFragment;
import com.psu.sweng888.gthenewapp.fragments.AddPodcastFragment;
import com.psu.sweng888.gthenewapp.fragments.AddProductFragment;
import com.psu.sweng888.gthenewapp.fragments.AccountFragment;
import com.psu.sweng888.gthenewapp.fragments.LanguageFragment;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;
    private AuthHelper mAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate started");
        
        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "MainActivity layout set successfully");

            // Set up the toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                Log.d(TAG, "Toolbar set up successfully");
            } else {
                Log.e(TAG, "Toolbar not found in layout");
            }

            // Initialize Drawer and NavigationView
            mDrawerLayout = findViewById(R.id.nav_drawer_layout);
            mNavigationView = findViewById(R.id.nav_view);
            
            if (mDrawerLayout != null && mNavigationView != null) {
                mNavigationView.setNavigationItemSelectedListener(this);
                Log.d(TAG, "Navigation drawer initialized successfully");
            } else {
                Log.e(TAG, "Navigation drawer components not found");
            }

            // Initialize AuthHelper
            mAuthHelper = new AuthHelper(this);
            
            // Set up ActionBarDrawerToggle
            if (toolbar != null) {
                mActionBarDrawerToggle = new ActionBarDrawerToggle(
                        this,
                        mDrawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                );
                mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
                mActionBarDrawerToggle.syncState();
                Log.d(TAG, "ActionBarDrawerToggle set up successfully");
            }

            // Set the default fragment
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                Log.d(TAG, "Default fragment (HomeFragment) set successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error setting default fragment", e);
                Toast.makeText(this, "Error loading home screen", Toast.LENGTH_SHORT).show();
            }

            // Display user's email in Navigation Drawer header
            try {
                View headerView = mNavigationView.getHeaderView(0);
                if (headerView != null) {
                    TextView usernameText = headerView.findViewById(R.id.nav_header_username);
                    if (usernameText != null) {
                        String username = getIntent().getStringExtra("username");
                        if (username != null) {
                            usernameText.setText(username);
                            Log.d(TAG, "Username set in header: " + username);
                        } else {
                            usernameText.setText("Guest");
                            Log.d(TAG, "No username provided, set to Guest");
                        }
                    } else {
                        Log.e(TAG, "Username TextView not found in header");
                    }
                } else {
                    Log.e(TAG, "Navigation header view not found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting username in header", e);
            }
            
            Log.d(TAG, "MainActivity onCreate completed successfully");
            Toast.makeText(this, "Welcome to GThenewapp!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in MainActivity onCreate", e);
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "Navigation item selected: " + id);

        try {
            if (id == R.id.nav_books) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BooksListFragment())
                        .commit();
                Log.d(TAG, "Switched to BooksListFragment");
            } else if (id == R.id.nav_podcasts) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PodcastsListFragment())
                        .commit();
                Log.d(TAG, "Switched to PodcastsListFragment");
            } else if (id == R.id.nav_products) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProductsListFragment())
                        .commit();
                Log.d(TAG, "Switched to ProductsListFragment");
            } else if (id == R.id.nav_add_book) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddBookFragment())
                        .commit();
                Log.d(TAG, "Switched to AddBookFragment");
            } else if (id == R.id.nav_add_podcast) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddPodcastFragment())
                        .commit();
                Log.d(TAG, "Switched to AddPodcastFragment");
            } else if (id == R.id.nav_add_product) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddProductFragment())
                        .commit();
                Log.d(TAG, "Switched to AddProductFragment");
            } else if (id == R.id.nav_account) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AccountFragment())
                        .commit();
                Log.d(TAG, "Switched to AccountFragment");
            } else if (id == R.id.nav_language) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LanguageFragment())
                        .commit();
                Log.d(TAG, "Switched to LanguageFragment");
            } else if (id == R.id.nav_dark_mode) {
                showDarkModeDialog();
            } else if (id == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                Log.d(TAG, "Switched to HomeFragment");
            } else if (id == R.id.nav_logout) {
                Log.d(TAG, "Logout selected");
                mAuthHelper.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error handling navigation item selection", e);
            Toast.makeText(this, "Error navigating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void showDarkModeDialog() {
        final String[] modes = {"Light", "Dark", "System Default"};
        int checkedItem = getDarkModePref();
        new AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(modes, checkedItem, null)
            .setPositiveButton("OK", (dialog, which) -> {
                int selected = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                setDarkMode(selected);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void setDarkMode(int mode) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        switch (mode) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putInt("dark_mode", 0);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putInt("dark_mode", 1);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                editor.putInt("dark_mode", 2);
                break;
        }
        editor.apply();
    }
    private int getDarkModePref() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        return prefs.getInt("dark_mode", 2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_drawer_items, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
}
