package com.mort.shelflauncher.steamshelflauncher.service.LauncherService;

import com.mort.shelflauncher.steamshelflauncher.model.Game;

import java.io.IOException;


public class LauncherServiceImpl implements LauncherService {


    private static final String GAME_ID_START_URI = "steam://rungameid/";

    public void launch(Game game) throws IOException {

        String startGame = GAME_ID_START_URI + game.getSteamAppId();

           ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "", startGame);

           processBuilder.redirectErrorStream(true);

           processBuilder.start();


        System.out.println("Launching " + game.getTitle() + " with steam id " + game.getSteamAppId());

    }

}
