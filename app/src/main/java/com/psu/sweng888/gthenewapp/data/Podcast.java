package com.psu.sweng888.gthenewapp.data;

import java.io.Serializable;

public class Podcast implements Serializable {
    private String title;
    private String host;
    private int episodeCount;
    private String publisher;

    public Podcast() {}

    public Podcast(String title, String host, int episodeCount, String publisher) {
        this.title = title;
        this.host = host;
        this.episodeCount = episodeCount;
        this.publisher = publisher;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getEpisodeCount() { return episodeCount; }
    public void setEpisodeCount(int episodeCount) { this.episodeCount = episodeCount; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
} 