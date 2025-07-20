package com.psu.sweng888.gthenewapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.adapter.PodcastAdapter;
import com.psu.sweng888.gthenewapp.data.BookDatabaseHelper;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import com.psu.sweng888.gthenewapp.data.Podcast;
import java.util.List;
import androidx.appcompat.widget.SearchView;
import android.app.AlertDialog;
import com.psu.sweng888.gthenewapp.util.RecyclerItemClickListener;
import com.psu.sweng888.gthenewapp.fragments.EditPodcastFragment;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import java.util.ArrayList;

public class PodcastsListFragment extends Fragment {
    private static final String TAG = "PodcastsListFragment";
    private PodcastAdapter podcastAdapter;
    private RecyclerView mRecyclerView;
    private BookDatabaseHelper dbHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;
    private ArrayAdapter<String> autoAdapter; // Store as a field

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcast_list, container, false);
        try {
            mRecyclerView = view.findViewById(R.id.recyclerView);
            if (mRecyclerView == null) {
                Log.e(TAG, "RecyclerView is null!");
                Toast.makeText(getActivity(), "RecyclerView is null!", Toast.LENGTH_LONG).show();
                return view;
            }
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            dbHelper = new BookDatabaseHelper(getActivity());
            firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
            if (dbHelper.isPodcastsEmpty()) {
                dbHelper.clearAllPodcasts();
                dbHelper.populatePodcastsDatabase();
                Log.d(TAG, "Podcasts database repopulated");
                Toast.makeText(getActivity(), "Podcasts database repopulated", Toast.LENGTH_SHORT).show();
            }
            AutoCompleteTextView searchAuto = view.findViewById(R.id.podcast_search_autocomplete);
            autoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line);
            searchAuto.setAdapter(autoAdapter);
            searchAuto.setThreshold(1);
            searchAuto.setOnItemClickListener((parent, v, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                if (podcastAdapter != null) podcastAdapter.filter(selected);
            });
            searchAuto.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (podcastAdapter != null) podcastAdapter.filter(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
            Button refreshButton = view.findViewById(R.id.refresh_button);
            if (refreshButton != null) {
                refreshButton.setOnClickListener(v -> refreshPodcasts());
                refreshButton.setOnLongClickListener(v -> {
                    resetDatabase();
                    return true;
                });
            }
            Button loadDummyButton = view.findViewById(R.id.load_data_button);
            if (loadDummyButton != null) {
                loadDummyButton.setOnClickListener(v -> testRecyclerView());
            }
            Button syncFirebaseButton = view.findViewById(R.id.sync_firebase_button);
            if (syncFirebaseButton != null) {
                syncFirebaseButton.setOnClickListener(v -> {
                    if (firebaseDatabaseManager.isUserAuthenticated()) {
                        firebaseDatabaseManager.getAllPodcasts(task -> {
                            List<Podcast> podcasts = task.getResult();
                            if (podcasts != null && !podcasts.isEmpty()) {
                                getActivity().runOnUiThread(() -> {
                                    podcastAdapter = new PodcastAdapter(podcasts);
                                    mRecyclerView.setAdapter(podcastAdapter);
                                    podcastAdapter.notifyDataSetChanged();
                                    Toast.makeText(getActivity(), "Synced " + podcasts.size() + " podcasts from Firebase", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "No podcasts found in Firebase", Toast.LENGTH_SHORT).show());
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Not authenticated with Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Button pushFirebaseButton = view.findViewById(R.id.push_firebase_button);
            if (pushFirebaseButton != null) {
                pushFirebaseButton.setOnClickListener(v -> {
                    if (firebaseDatabaseManager.isUserAuthenticated()) {
                        List<Podcast> podcasts = dbHelper.getAllPodcasts();
                        if (podcasts != null && !podcasts.isEmpty()) {
                            int[] completed = {0};
                            for (Podcast podcast : podcasts) {
                                firebaseDatabaseManager.addPodcast(podcast, task -> {
                                    completed[0]++;
                                    if (completed[0] == podcasts.size()) {
                                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Pushed " + podcasts.size() + " podcasts to Firebase", Toast.LENGTH_SHORT).show());
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getActivity(), "No podcasts in SQLite to push", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Not authenticated with Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            loadPodcasts(autoAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Exception in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }
    private void loadPodcasts(ArrayAdapter<String> autoAdapter) {
        Log.d(TAG, "Loading podcasts from SQLite only (ignore Firebase)");
        List<Podcast> podcasts = dbHelper.getAllPodcasts();
        final List<Podcast> finalPodcasts = podcasts;
        getActivity().runOnUiThread(() -> {
            try {
                podcastAdapter = new PodcastAdapter(finalPodcasts);
                mRecyclerView.setAdapter(podcastAdapter);
                autoAdapter.clear();
                for (Podcast p : finalPodcasts) autoAdapter.add(p.getTitle() + " — " + p.getHost() + " — Episodes: " + p.getEpisodeCount() + " — " + p.getPublisher());
                autoAdapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded " + finalPodcasts.size() + " podcasts");
                Toast.makeText(getActivity(), "Loaded " + finalPodcasts.size() + " podcasts", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Exception updating UI with podcasts: " + e.getMessage(), e);
                Toast.makeText(getActivity(), "Error updating podcast list", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void refreshPodcasts() {
        Log.d(TAG, "Refreshing podcasts list (SQLite only)...");
        loadPodcasts(autoAdapter);
    }
    private void resetDatabase() {
        dbHelper.clearAllPodcasts();
        dbHelper.populatePodcastsDatabase();
        Toast.makeText(getActivity(), "Podcast database reset.", Toast.LENGTH_SHORT).show();
        loadPodcasts(autoAdapter);
    }
    private void showEditDeleteDialog(Podcast podcast) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(podcast.getTitle())
            .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                if (which == 0) {
                    // Edit
                    EditPodcastFragment editFragment = EditPodcastFragment.newInstance(podcast);
                    getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
                } else if (which == 1) {
                    // Delete
                    dbHelper.deletePodcast(podcast);
                    refreshPodcasts();
                    Toast.makeText(getActivity(), "Podcast deleted", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void testRecyclerView() {
        Log.d(TAG, "Testing RecyclerView with dummy podcast data...");
        List<Podcast> dummyPodcasts = new ArrayList<>();
        dummyPodcasts.add(new Podcast("The Daily", "Michael Barbaro", 2000, "The New York Times"));
        dummyPodcasts.add(new Podcast("How I Built This", "Guy Raz", 500, "NPR"));
        dummyPodcasts.add(new Podcast("Science Vs", "Wendy Zukerman", 300, "Gimlet"));
        dummyPodcasts.add(new Podcast("99% Invisible", "Roman Mars", 500, "PRX"));
        dummyPodcasts.add(new Podcast("Radiolab", "Jad Abumrad, Robert Krulwich", 600, "WNYC Studios"));
        dummyPodcasts.add(new Podcast("Freakonomics Radio", "Stephen J. Dubner", 400, "Freakonomics, LLC"));
        dummyPodcasts.add(new Podcast("TED Radio Hour", "Manoush Zomorodi", 350, "NPR"));
        dummyPodcasts.add(new Podcast("Planet Money", "NPR", 900, "NPR"));
        dummyPodcasts.add(new Podcast("Reply All", "PJ Vogt, Alex Goldman", 170, "Gimlet"));
        dummyPodcasts.add(new Podcast("Stuff You Should Know", "Josh Clark, Chuck Bryant", 1500, "iHeartRadio"));
        // Insert into SQLite
        dbHelper.clearAllPodcasts();
        for (Podcast p : dummyPodcasts) dbHelper.addPodcast(p);
        // Reload from SQLite
        List<Podcast> podcastsFromDb = dbHelper.getAllPodcasts();
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Setting up test adapter with " + podcastsFromDb.size() + " podcasts from SQLite");
            podcastAdapter = new PodcastAdapter(podcastsFromDb);
            mRecyclerView.setAdapter(podcastAdapter);
            podcastAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), "Test: Loaded " + podcastsFromDb.size() + " real podcasts from SQLite", Toast.LENGTH_LONG).show();
        });
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dbHelper = new BookDatabaseHelper(context);
    }
} 