package com.mort.shelflauncher.steamshelflauncher.model;

import java.util.Arrays;

public class Game {

    private final String title;
    private String[] publishers;
    private final long steamAppId;

    public Game(String title, long steamAppId){

        this.title = title;
        publishers = new String[]{"Unknown"};
        this.steamAppId = steamAppId;

    }

    public String getTitle() {
        return title;
    }

    public String[] getPublishers() {
        return publishers;
    }

    public long getSteamAppId() {
        return steamAppId;
    }

    public void setPublishers(String... publishers) {
        this.publishers = publishers;
    }

    @Override
    public String toString() {
        return "Game{" +
                "title='" + title + '\'' +
                ", publishers='" + Arrays.toString(publishers) + '\'' +
                ", steamAppId=" + steamAppId +
                '}';
    }
}
