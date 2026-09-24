package com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService;

import java.io.IOException;
import java.util.Optional;

public interface SteamStoreService {

    Optional<String> fetchHeaderImageUrl(long gameId) throws IOException, InterruptedException;
    String[] fetchGamePublisher(long gameId) throws IOException, InterruptedException;
    Optional<String> fetchLibraryCoverUrl(long gameId);

}
