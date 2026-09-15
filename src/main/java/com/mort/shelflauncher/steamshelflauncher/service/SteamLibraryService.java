package com.mort.shelflauncher.steamshelflauncher.service;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class SteamLibraryService {

    private final SteamStoreService storeService;

    public SteamLibraryService(SteamStoreService storeService){

        this.storeService = storeService;

    }

    public Optional<Path> findSteamPath() throws IOException {

        String target = "SteamPath";

        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "reg", "query","\"HKCU\\Software\\Valve\\Steam\"",
                "/v", "SteamPath");

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null){
            if(line.contains(target)){
                System.out.println(line);

                int index = line.indexOf("REG_SZ");
                line = line.substring(index + "REG_SZ".length()).trim();

                Path path = Path.of(line);

                if(!Files.isDirectory(path)){
                    break;
                }

                return Optional.of(path);

            }
        }

            return Optional.empty();


    }

    public Optional<Path> findLibraryFoldersFile(Path steamPath){

        Path path = steamPath.resolve("steamapps").resolve("libraryfolders.vdf");


        if(Files.isRegularFile(path)){
            return Optional.of(path);
        }

        return Optional.empty();

    }

    // No longer used
    @Deprecated
    public void printLibraryFolders(Path libraryFolders) throws IOException {

        List<String> strings = Files.readAllLines(libraryFolders);

        for(String s : strings){

            System.out.println(s);

        }

    }

    public List<Path> findLibraryPaths(Path libraryFolders) throws IOException {

        List<String> strings = Files.readAllLines(libraryFolders);
        List<Path> paths = new ArrayList<>();

        for(String s : strings){

            if(s.contains("path")){

                int index = s.indexOf("path");
                s = s.substring(index + "path".length() + 1).trim();
                s = s.replaceAll("[\\\\/]{2,}", "/");
                s = s.replace("\"", "");
                Path path = Path.of(s);
                if(Files.isDirectory(path)){
                    System.out.println(path);
                    paths.add(path);
                }

            }

        }

        return paths;

    }

    public List<Path> findAppManifestFiles(List<Path> paths){

        List<Path> manifestPaths = new ArrayList<>();

        if(paths == null || paths.isEmpty()){
            return manifestPaths;
        }

        for (Path path : paths) {

            path = path.resolve("steamapps");


            try (Stream<Path> pathStream = Files.list(path)) {

                pathStream.forEach(p -> {
                    String fileName = p.getFileName().toString();
                    if (fileName.startsWith("appmanifest_") && fileName.endsWith(".acf") && Files.isRegularFile(p)) {
                        manifestPaths.add(p);
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        System.out.println(manifestPaths);

        return manifestPaths;
    }

    // No longer used
    @Deprecated
    public void printManifest(Path manifest) throws IOException {

        List<String> strings = Files.readAllLines(manifest);

        for(String s : strings){
            System.out.println(s);
        }

    }


    private Optional<Game> createGameOf(Path manifest) throws IOException {

        List<String> strings = Files.readAllLines(manifest);
        long gameId = -1;
        String title = null;

        for(String s : strings){

            if(s.contains("appid")){
                s = s.substring(s.indexOf("appid") + "appid".length() + 1).trim();
                s = s.replace("\"", "");
                gameId = Long.parseLong(s);
            }

            if(s.contains("name")){
                s = s.substring(s.indexOf("name") + "name".length() + 1).trim();
                s = s.replace("\"", "");
                title = s;
            }

        }

        if(gameId != -1 && title != null){

            Game game = new Game(title, gameId);

            CompletableFuture.runAsync(() -> {

                try {
                    game.setPublishers(fetchGamePublishers(game.getSteamAppId()));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });


            return Optional.of(game);

        }

        return Optional.empty();

    }

    private String[] fetchGamePublishers(long gameId) throws IOException, InterruptedException {

        String[] publishers = storeService.fetchGamePublisher(gameId);

        if(publishers != null && publishers.length == 1){
            return publishers;
        }else {
            return new String[]{"Unknown"};
        }

    }

    public List<Game> createGamesOf(List<Path> manifests) throws IOException {

        List<Game> games = new ArrayList<>();

        if(manifests == null || manifests.isEmpty()){
            return games;
        }

        for(Path path : manifests){

            Optional<Game> game = createGameOf(path);

            if(game.isPresent()){
                System.out.println(game.get());
                games.add(game.get());
            }

        }

        return games;

    }


}
