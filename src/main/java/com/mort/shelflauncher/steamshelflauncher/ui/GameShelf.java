package com.mort.shelflauncher.steamshelflauncher.ui;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import com.mort.shelflauncher.steamshelflauncher.service.LauncherService;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class GameShelf {
    private final List<Game> games;
    private final SteamStoreService steamStoreService;
    private final LauncherService launcherService;
    private final AtomicReference<GameCase> selectedCase;
    private final int spacingX;
    private final List<GameCase> gameCases;
    private int selectedIndex;
    private final Label gameTitleLabel;

    public GameShelf(List<Game> games, SteamStoreService steamStoreService, LauncherService launcherService, int spacingX) {

        this.games = new ArrayList<>(games);
        this.steamStoreService = steamStoreService;
        this.launcherService = launcherService;

        this.selectedCase = new AtomicReference<>(null);

        this.spacingX = spacingX;

        this.gameCases = new ArrayList<>();
        this.selectedIndex = -1;

        this.gameTitleLabel = new Label();



    }

    private GameCase createGameCase(Game game, int x, double normalZ, double selectedZ, TranslateTransition translateTransition){


            GameCase gameCase = new GameCase(game, steamStoreService, normalZ, selectedZ, x);

            gameCase.setOnMouseClicked(event -> {

                if(translateTransition.getStatus() == Animation.Status.RUNNING){
                    return;
                }

                if(event.getClickCount() == 2){
                    launcherService.launch(game);
                    return;
                }

                if(selectedCase.get() == null){

                    gameCase.select();

                    selectedCase.set(gameCase);
                    selectedIndex = gameCases.indexOf(gameCase);
                    moveToGamePosition(translateTransition);
                    updateGameTitleLabel(gameCase.getGame());
                } else if(selectedCase.get() == gameCase){
                    gameCase.deselect();
                    selectedCase.set(null);
                    selectedIndex = -1;
                    gameTitleLabel.setText("");
                }else{
                    selectedCase.get().deselect();
                    gameCase.select();
                    selectedCase.set(gameCase);
                    selectedIndex = gameCases.indexOf(gameCase);
                    moveToGamePosition(translateTransition);
                    updateGameTitleLabel(gameCase.getGame());
                }

            });

            return gameCase;



    }

    private void addGamesToGroup(Group group, int x, double normalZ, double selectedZ, TranslateTransition translateTransition){

        for(Game game : games){
            GameCase gameCase = createGameCase(game, x, normalZ, selectedZ, translateTransition);
            group.getChildren().add(gameCase);
            gameCases.add(gameCase);

            x += spacingX;
        }

    }

    public VBox createView(){

        VBox vBox = new VBox();
        vBox.getChildren().add(createScene());
        vBox.getChildren().add(gameTitleLabel);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);

        return vBox;
    }

    private SubScene createScene() {

        Group root3d = new Group();
        int x = 0;
        double normalZ = 500;
        double selectedZ = 300;

        Box shelf = new Box(1280, 20, 180);
        shelf.setTranslateX(-50);
        shelf.setTranslateY(120);
        shelf.setTranslateZ(500);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResource("/images/shelf.jpg")).toExternalForm()));
        shelf.setMaterial(material);

        Group gamesGroup = new Group();
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(150), gamesGroup);

        root3d.getChildren().add(shelf);
        root3d.getChildren().add(gamesGroup);

        addGamesToGroup(gamesGroup, x, normalZ, selectedZ, translateTransition);

        SubScene scene = new SubScene(root3d, 1280, 500, true, SceneAntialiasing.BALANCED);

        PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
        perspectiveCamera.setNearClip(0.1);
        perspectiveCamera.setFarClip(2000);
        perspectiveCamera.setFieldOfView(45);

        scene.setCamera(perspectiveCamera);
        scene.setFill(Color.DARKGRAY);

        AmbientLight light = new AmbientLight(Color.WHITE);
        root3d.getChildren().add(light);

        // keyboard support

        setupKeyboard(scene, translateTransition);

        return scene;

    }

    private void setupKeyboard(SubScene scene, TranslateTransition translateTransition){

        scene.setFocusTraversable(true);

        scene.setOnMouseClicked(event -> scene.requestFocus());

        setSceneOnKeyPressed(scene, translateTransition);

    }

    private void setSceneOnKeyPressed(SubScene scene, TranslateTransition translateTransition){

        scene.setOnKeyPressed((event -> {
            KeyCode keyCode = event.getCode();

            List<KeyCode> keyCodes = new ArrayList<>(Arrays.asList(KeyCode.A, KeyCode.D, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ENTER));

            if(keyCodes.contains(keyCode)){
                handleKeyPressed(keyCode, translateTransition);
            }
        }));

    }

    private void handleKeyPressed(KeyCode keyCode, TranslateTransition translateTransition){

        if(translateTransition.getStatus() == Animation.Status.RUNNING || gameCases.isEmpty()){
            return;
        }

        switch (keyCode){
            case KeyCode.LEFT, KeyCode.A:

                if(selectedIndex - 1 < 0){
                    selectedIndex = 0;
                    if(selectedCase.get() == null){
                        selectGameCase(gameCases.get(selectedIndex));
                        moveToGamePosition(translateTransition);
                    }
                    return;
                }else{
                    selectedIndex -= 1;
                }

                selectGameCase(gameCases.get(selectedIndex));
                moveToGamePosition(translateTransition);

                break;
            case KeyCode.RIGHT, KeyCode.D:

                if(selectedIndex + 1 >= gameCases.size()){
                    selectedIndex = 0;
                }else{
                    selectedIndex += 1;
                }

                selectGameCase(gameCases.get(selectedIndex));
                moveToGamePosition(translateTransition);

                break;

            case KeyCode.ENTER:
                if(selectedCase.get() != null){
                    launcherService.launch(selectedCase.get().getGame());
                }
                break;


        }

    }

    private void selectGameCase(GameCase gameCase){

        if(selectedCase.get() != null){
            selectedCase.get().deselect();
        }
        selectedCase.set(gameCase);
        selectedCase.get().select();
        updateGameTitleLabel(selectedCase.get().getGame());

    }

    private void moveToGamePosition(TranslateTransition translateTransition){
        if(selectedIndex != -1){
            translateTransition.setToX(-gameCases.get(selectedIndex).getTranslateX());
            translateTransition.play();
        }

    }

    private void updateGameTitleLabel(Game game){

        String stringBuilder = "Selected game: " + game.getTitle() + "\n" + "Publisher: " + Arrays.toString(game.getPublishers()) +
                "\n" + "Press ENTER to play";

        gameTitleLabel.setText(stringBuilder);

    }


}

