package com.mort.shelflauncher.steamshelflauncher.service.LauncherService;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class LauncherServiceImpl implements LauncherService {


    private static final String GAME_ID_START_URI = "steam://rungameid/";

    private static final Logger logger = LoggerFactory.getLogger(LauncherServiceImpl.class);

    public void launch(Game game) throws IOException {

        String startGame = GAME_ID_START_URI + game.getSteamAppId();

           ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "", startGame);

           processBuilder.redirectErrorStream(true);

           processBuilder.start();


        logger.info("Launching {} with steam id {}", game.getTitle(), game.getSteamAppId());

    }

}
