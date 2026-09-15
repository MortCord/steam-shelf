package com.mort.shelflauncher.steamshelflauncher.ui;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import com.mort.shelflauncher.steamshelflauncher.service.LauncherService;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class GameShelf {

    private final List<Game> games;
    private final SteamStoreService steamStoreService;
    private final LauncherService launcherService;

    public GameShelf(List<Game> games, SteamStoreService steamStoreService, LauncherService launcherService) {

        this.games = new ArrayList<>(games);
        this.steamStoreService = steamStoreService;
        this.launcherService = launcherService;

    }

    private GameCase createGameCase(Game game, AtomicReference<GameCase> selectedCase, int x, double normalZ, double selectedZ){


            GameCase gameCase = new GameCase(game, steamStoreService, normalZ, selectedZ, x);

            gameCase.setOnMouseClicked(event -> {

                if(event.getClickCount() == 2){
                    launcherService.launch(game);
                    return;
                }

                if(selectedCase.get() == null){

                    gameCase.select();

                    selectedCase.set(gameCase);
                } else if(selectedCase.get() == gameCase){
                    gameCase.deselect();
                    selectedCase.set(null);

                }else{
                    selectedCase.get().deselect();

                    gameCase.select();

                    selectedCase.set(gameCase);
                }

            });

            return gameCase;



    }

    public SubScene createScene() {

        Group root3d = new Group();
        int x = -200;
        double normalZ = 500;
        double selectedZ = 300;

        Box shelf = new Box(800, 20, 180);
//        shelf.setMaterial(new PhongMaterial(Color.BROWN));
        shelf.setTranslateX(-50);
        shelf.setTranslateY(120);
        shelf.setTranslateZ(500);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResource("/images/shelf.jpg")).toExternalForm()));
        shelf.setMaterial(material);

        root3d.getChildren().add(shelf);
//        System.out.println(games);

        AtomicReference<GameCase> selectedCase = new AtomicReference<>(null);

        for(Game game : games){
            GameCase gameCase = createGameCase(game, selectedCase, x, normalZ, selectedZ);
            root3d.getChildren().add(gameCase);

            x += 30;
        }


        SubScene scene = new SubScene(root3d, 800, 500, true, SceneAntialiasing.BALANCED);

        PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
        perspectiveCamera.setNearClip(0.1);
        perspectiveCamera.setFarClip(2000);
        perspectiveCamera.setFieldOfView(45);

        scene.setCamera(perspectiveCamera);
        scene.setFill(Color.DARKGRAY);

        AmbientLight light = new AmbientLight(Color.WHITE);
        root3d.getChildren().add(light);

        return scene;

    }


}

