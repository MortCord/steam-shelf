package com.mort.shelflauncher.steamshelflauncher.service.SteamLibraryService;

import com.mort.shelflauncher.steamshelflauncher.model.Game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface SteamLibraryService {

     Optional<Path> findSteamPath() throws IOException;
     Optional<Path> findLibraryFoldersFile(Path steamPath);
     List<Path> findLibraryPaths(Path libraryFolders) throws IOException;
     List<Path> findAppManifestFiles(List<Path> paths);
     List<Game> createGamesOf(List<Path> manifests) throws IOException;

}
