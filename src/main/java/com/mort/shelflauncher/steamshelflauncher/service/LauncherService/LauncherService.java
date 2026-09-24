package com.mort.shelflauncher.steamshelflauncher.service.LauncherService;

import com.mort.shelflauncher.steamshelflauncher.model.Game;

import java.io.IOException;

public interface LauncherService {

    void launch(Game game) throws IOException;

}
