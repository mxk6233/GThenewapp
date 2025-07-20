package com.psu.sweng888.gthenewapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.data.Podcast;
import java.util.ArrayList;
import java.util.List;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {
    private List<Podcast> podcastList;
    private List<Podcast> podcastListFull;

    public PodcastAdapter(List<Podcast> podcastList) {
        this.podcastList = podcastList;
        this.podcastListFull = new ArrayList<>(podcastList);
    }
    @NonNull
    @Override
    public PodcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.podcast_item, parent, false);
        return new PodcastViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PodcastViewHolder holder, int position) {
        Podcast podcast = podcastList.get(position);
        holder.title.setText(podcast.getTitle());
        holder.host.setText(podcast.getHost());
        holder.episodeCount.setText("Episodes: " + podcast.getEpisodeCount());
        holder.publisher.setText(podcast.getPublisher());
    }
    @Override
    public int getItemCount() { return podcastList.size(); }

    public void filter(String query) {
        podcastList.clear();
        if (query == null || query.trim().isEmpty()) {
            podcastList.addAll(podcastListFull);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Podcast podcast : podcastListFull) {
                if (podcast.getTitle().toLowerCase().contains(lowerQuery) ||
                    podcast.getHost().toLowerCase().contains(lowerQuery) ||
                    podcast.getPublisher().toLowerCase().contains(lowerQuery) ||
                    String.valueOf(podcast.getEpisodeCount()).contains(lowerQuery)) {
                    podcastList.add(podcast);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class PodcastViewHolder extends RecyclerView.ViewHolder {
        TextView title, host, episodeCount, publisher;
        public PodcastViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.podcast_title);
            host = itemView.findViewById(R.id.podcast_host);
            episodeCount = itemView.findViewById(R.id.podcast_episode_count);
            publisher = itemView.findViewById(R.id.podcast_publisher);
        }
    }
} 