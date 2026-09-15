package com.mort.shelflauncher.steamshelflauncher.repository;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import com.mort.shelflauncher.steamshelflauncher.service.SteamLibraryService;
import com.mort.shelflauncher.steamshelflauncher.service.exception.LibraryPathsNotFoundException;
import com.mort.shelflauncher.steamshelflauncher.service.exception.SteamPathNotFoundException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameRepository {

    private List<Game> games;
    private final SteamLibraryService service;

    public GameRepository(SteamLibraryService service){

        this.service = service;

    }

    public void init() throws IOException, SteamPathNotFoundException, LibraryPathsNotFoundException {

        Optional<Path> path = service.findSteamPath();

        Optional<Path> libraryFolders = service.findLibraryFoldersFile(path.orElseThrow(() ->
                    new SteamPathNotFoundException("Couldn't find steam path")));

        List<Path> libraryPaths = service.findLibraryPaths(libraryFolders.orElseThrow(() ->
                new LibraryPathsNotFoundException("Couldn't find library path")));


        List<Path> manifests = service.findAppManifestFiles(libraryPaths);
        games = service.createGamesOf(manifests);



    }

    public List<Game> getGames() throws SteamPathNotFoundException, IOException, LibraryPathsNotFoundException, InterruptedException {
        if(games == null){

            init();

        }
        return new ArrayList<>(games);
    }
}
