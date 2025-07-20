package com.psu.sweng888.gthenewapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import com.psu.sweng888.gthenewapp.data.Podcast;

public class AddPodcastFragment extends Fragment {
    private EditText titleInput, hostInput, episodeCountInput, publisherInput;
    private Button saveButton;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_podcast, container, false);
        titleInput = view.findViewById(R.id.input_podcast_title);
        hostInput = view.findViewById(R.id.input_podcast_host);
        episodeCountInput = view.findViewById(R.id.input_podcast_episode_count);
        publisherInput = view.findViewById(R.id.input_podcast_publisher);
        saveButton = view.findViewById(R.id.save_podcast_button);
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        saveButton.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Save button clicked", Toast.LENGTH_SHORT).show();
            try {
                savePodcast();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
        return view;
    }
    private void savePodcast() {
        Toast.makeText(getActivity(), "savePodcast() called", Toast.LENGTH_SHORT).show();
        String title = titleInput.getText().toString().trim();
        String host = hostInput.getText().toString().trim();
        String episodeCountStr = episodeCountInput.getText().toString().trim();
        String publisher = publisherInput.getText().toString().trim();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(host) || TextUtils.isEmpty(episodeCountStr) || TextUtils.isEmpty(publisher)) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        int episodeCount;
        try {
            episodeCount = Integer.parseInt(episodeCountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Episode count must be a number", Toast.LENGTH_SHORT).show();
            return;
        }
        Podcast podcast = new Podcast(title, host, episodeCount, publisher);
        Toast.makeText(getActivity(), "Calling addPodcast...", Toast.LENGTH_SHORT).show();
        firebaseDatabaseManager.addPodcast(podcast, task -> {
            Toast.makeText(getActivity(), "addPodcast callback", Toast.LENGTH_SHORT).show();
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Podcast added!", Toast.LENGTH_SHORT).show();
                titleInput.setText("");
                hostInput.setText("");
                episodeCountInput.setText("");
                publisherInput.setText("");
                // Navigate to PodcastsListFragment
                requireActivity().runOnUiThread(() -> {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.psu.sweng888.gthenewapp.fragments.PodcastsListFragment())
                        .commit();
                });
            } else {
                Toast.makeText(getActivity(), "Failed to add podcast", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 