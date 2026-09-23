package com.mort.shelflauncher.steamshelflauncher.service.LauncherService;

import com.mort.shelflauncher.steamshelflauncher.model.Game;

import java.io.IOException;


public class LauncherServiceImpl implements LauncherService {


    private static final String GAME_ID_START_URI = "steam://rungameid/";

    public void launch(Game game) {

        String startGame = GAME_ID_START_URI + game.getSteamAppId();

        try{
           ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "", startGame);

           processBuilder.redirectErrorStream(true);

           processBuilder.start();


        } catch (IOException e) {
            throw new IllegalStateException("Couldn't launch the game");
        }

        System.out.println("Launching " + game.getTitle() + " with steam id " + game.getSteamAppId());

    }

}
