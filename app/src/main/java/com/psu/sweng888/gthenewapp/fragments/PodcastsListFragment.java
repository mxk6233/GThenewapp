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

public class PodcastsListFragment extends Fragment {
    private static final String TAG = "PodcastsListFragment";
    private PodcastAdapter podcastAdapter;
    private RecyclerView mRecyclerView;
    private BookDatabaseHelper dbHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcast_list, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dbHelper = new BookDatabaseHelper(getActivity());
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        // Set up SearchView
        SearchView searchView = view.findViewById(R.id.podcast_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (podcastAdapter != null) podcastAdapter.filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (podcastAdapter != null) podcastAdapter.filter(newText);
                return true;
            }
        });
        loadPodcasts();
        return view;
    }
    private void loadPodcasts() {
        firebaseDatabaseManager.getAllPodcasts(task -> {
            List<Podcast> podcasts = task.getResult();
            if (podcasts == null || podcasts.isEmpty()) {
                if (dbHelper.isPodcastsEmpty()) dbHelper.populatePodcastsDatabase();
                podcasts = dbHelper.getAllPodcasts();
                Toast.makeText(getActivity(), "Loaded from local database", Toast.LENGTH_SHORT).show();
            }
            final List<Podcast> finalPodcasts = podcasts;
            getActivity().runOnUiThread(() -> {
                podcastAdapter = new PodcastAdapter(finalPodcasts);
                mRecyclerView.setAdapter(podcastAdapter);
                // Add long-press for edit/delete
                mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {}
                    @Override
                    public void onLongItemClick(View view, int position) {
                        showEditDeleteDialog(finalPodcasts.get(position));
                    }
                }));
            });
        });
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
                    dbHelper.clearAllPodcasts(); // For demo, replace with deletePodcast(podcast) for real app
                    firebaseDatabaseManager.addPodcast(podcast, task -> loadPodcasts());
                    Toast.makeText(getActivity(), "Podcast deleted", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dbHelper = new BookDatabaseHelper(context);
    }
} 