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
import com.psu.sweng888.gthenewapp.data.BookDatabaseHelper;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import com.psu.sweng888.gthenewapp.data.Podcast;

public class EditPodcastFragment extends Fragment {
    private static final String ARG_PODCAST = "arg_podcast";
    private Podcast podcast;
    private EditText titleInput, hostInput, episodeCountInput, publisherInput;
    private Button saveButton;
    private BookDatabaseHelper dbHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    public static EditPodcastFragment newInstance(Podcast podcast) {
        EditPodcastFragment fragment = new EditPodcastFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PODCAST, podcast);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_podcast, container, false);
        titleInput = view.findViewById(R.id.input_podcast_title);
        hostInput = view.findViewById(R.id.input_podcast_host);
        episodeCountInput = view.findViewById(R.id.input_podcast_episode_count);
        publisherInput = view.findViewById(R.id.input_podcast_publisher);
        saveButton = view.findViewById(R.id.save_podcast_button);
        dbHelper = new BookDatabaseHelper(getActivity());
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        if (getArguments() != null) {
            podcast = (Podcast) getArguments().getSerializable(ARG_PODCAST);
            if (podcast != null) {
                titleInput.setText(podcast.getTitle());
                hostInput.setText(podcast.getHost());
                episodeCountInput.setText(String.valueOf(podcast.getEpisodeCount()));
                publisherInput.setText(podcast.getPublisher());
            }
        }
        saveButton.setOnClickListener(v -> savePodcast());
        return view;
    }
    private void savePodcast() {
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
        Podcast updatedPodcast = new Podcast(title, host, episodeCount, publisher);
        dbHelper.clearAllPodcasts(); // For demo, replace with updatePodcast(updatedPodcast) for real app
        firebaseDatabaseManager.addPodcast(updatedPodcast, task -> {
            Toast.makeText(getActivity(), "Podcast updated!", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }
} 