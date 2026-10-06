package com.mort.shelflauncher.steamshelflauncher.ui;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import com.mort.shelflauncher.steamshelflauncher.service.LauncherService.LauncherService;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService.SteamStoreService;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameShelf {
    private final List<Game> games;
    private final SteamStoreService steamStoreService;
    private final LauncherService launcherService;
    private GameCase selectedCase;
    private final int spacingX;
    private final List<GameCase> gameCases;
    private int selectedIndex;
    private final Label gameTitleLabel;
    private final Label publisherLabel;
    private final Label controlsLabel;
    private final Label installedGamesLabel;
    private final Label selectedGameLabel;
    private final Label emptyLibraryLabel;


    private static GameShelf instance;

    private static final List<KeyCode> KEY_CODES = List.of(KeyCode.A, KeyCode.D, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ENTER, KeyCode.ESCAPE);

    private static final double NORMAL_Z = 500;
    private static final double SELECTED_Z = 300;
    private static final double SCENE_WIDTH = 1280;
    private static final double SCENE_HEIGHT = 500;
    private static final double SHELF_HEIGHT = 20;
    private static final double SHELF_DEPTH = 180;

    private GameShelf(List<Game> games, SteamStoreService steamStoreService, LauncherService launcherService, int spacingX) {

        this.games = new ArrayList<>(games);
        this.steamStoreService = steamStoreService;
        this.launcherService = launcherService;

        this.selectedCase = null;

        this.spacingX = spacingX;

        this.gameCases = new ArrayList<>();
        this.selectedIndex = -1;

        this.gameTitleLabel = new Label();
        gameTitleLabel.getStyleClass().add("game-title");

        this.publisherLabel = new Label();
        publisherLabel.getStyleClass().add("publisher");

        this.controlsLabel = new Label("← → or A/D to browse || ENTER or double click to play || ESC to deselect and return to start");
        controlsLabel.getStyleClass().add("controls");

        this.installedGamesLabel = new Label();
        installedGamesLabel.getStyleClass().add("game-count");

        this.selectedGameLabel = new Label("No game selected");
        selectedGameLabel.getStyleClass().add("selected-game");

        this.emptyLibraryLabel = new Label("No Steam games found");
        emptyLibraryLabel.getStyleClass().add("no-games-found");




    }

    private GameCase createGameCase(Game game, int x, TranslateTransition translateTransition){


            GameCase gameCase = new GameCase(game, steamStoreService, NORMAL_Z, SELECTED_Z, x);

            gameCase.setOnMouseClicked(event -> {

                if(translateTransition.getStatus() == Animation.Status.RUNNING){
                    return;
                }

                if(event.getClickCount() == 2){
                    launchGame(game);
                    return;
                }

                if(selectedCase == gameCase){
                    resetSelection(translateTransition);
                    return;
                }

                selectedIndex = gameCases.indexOf(gameCase);
                selectGameCase(gameCase, translateTransition);

            });

            gameCase.setOnMouseEntered(event -> {

                gameCase.setCursor(Cursor.HAND);

                if(selectedCase != gameCase){

                    gameCase.scaleTo(1.1);
                }

            });

            gameCase.setOnMouseExited(event -> {

                gameCase.setCursor(Cursor.DEFAULT);

                if(selectedCase != gameCase){

                    gameCase.scaleTo(1);
                }

            });

            return gameCase;



    }

    private void addGamesToGroup(Group group, int x, TranslateTransition translateTransition){

        for(Game game : games){
            GameCase gameCase = createGameCase(game, x, translateTransition);
            group.getChildren().add(gameCase);
            gameCases.add(gameCase);

            x += spacingX;
        }

    }

    public VBox createView(){

        VBox vBox = new VBox();
        vBox.getStyleClass().add("shelf-view");

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);

        installedGamesLabel.setText("Installed games: " + games.size());
        vBox.getChildren().add(installedGamesLabel);

        SubScene subScene = createScene();
        subScene.widthProperty().bind(vBox.widthProperty());
        vBox.setMinWidth(800);
        VBox.setVgrow(subScene, Priority.ALWAYS);

        if(games.isEmpty()){
            vBox.getChildren().add(emptyLibraryLabel);
            vBox.getChildren().add(subScene);
            return vBox;
        }

        vBox.getChildren().add(selectedGameLabel);


        vBox.getChildren().add(subScene);
        vBox.getChildren().add(gameTitleLabel);
        vBox.getChildren().add(publisherLabel);
        vBox.getChildren().add(controlsLabel);

        return vBox;
    }

    private SubScene createScene() {

        Group root3d = new Group();
        int x = 0;

        Box shelf = new Box(SCENE_WIDTH, SHELF_HEIGHT, SHELF_DEPTH);
        shelf.setTranslateX(-50);
        shelf.setTranslateY(120);
        shelf.setTranslateZ(NORMAL_Z);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResource("/images/shelf.jpg")).toExternalForm()));
        shelf.setMaterial(material);

        Group gamesGroup = new Group();
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(150), gamesGroup);


        root3d.getChildren().add(gamesGroup);
        root3d.getChildren().add(shelf);

        addGamesToGroup(gamesGroup, x, translateTransition);

        SubScene scene = new SubScene(root3d, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED);
        shelf.widthProperty().bind(scene.widthProperty());

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

            if(KEY_CODES.contains(keyCode)){
                handleKeyPressed(keyCode, translateTransition);
            }
        }));

    }

    private void handleKeyPressed(KeyCode keyCode, TranslateTransition translateTransition) {

        if(translateTransition.getStatus() == Animation.Status.RUNNING || gameCases.isEmpty()){
            return;
        }

        switch (keyCode){
            case KeyCode.LEFT, KeyCode.A, KeyCode.RIGHT, KeyCode.D:

                moveSelection(detectDirection(keyCode), translateTransition);

                break;
            case KeyCode.ENTER:
                if(selectedCase != null){
                    launchGame(selectedCase.getGame());
                }
                break;

            case KeyCode.ESCAPE:
                if(selectedCase != null){
                    resetSelection(translateTransition);
                }
                break;


        }

    }

    private SelectedDirection detectDirection(KeyCode keyCode){

        if(keyCode == KeyCode.LEFT || keyCode == KeyCode.A){

            return SelectedDirection.LEFT;

        }

        return SelectedDirection.RIGHT;

    }

    private void resetSelection(TranslateTransition transition){

        deselectGameCase(selectedCase);
        selectedIndex = -1;
        moveToStartPosition(transition);

    }

    private void moveSelection(SelectedDirection direction, TranslateTransition transition){

        selectedIndex = moveSelectedIndex(direction);

        selectGameCase(gameCases.get(selectedIndex), transition);

    }

    private void selectGameCase(GameCase gameCase, TranslateTransition translateTransition){
        gameCase.scaleTo(1);
        if(selectedCase != null){
            deselectGameCase(selectedCase);
        }
        selectedCase = gameCase;
        selectedCase.select();
        updateGameInfoUI(selectedCase.getGame());
        moveToGamePosition(translateTransition);

    }

    private void deselectGameCase(GameCase gameCase){
        gameCase.deselect();
        selectedCase = null;
        clearGameInfoUI();
    }

    private void moveToGamePosition(TranslateTransition translateTransition){
        if(selectedIndex != -1){
            translateTransition.setToX(-gameCases.get(selectedIndex).getTranslateX());
            translateTransition.play();
        }

    }

    private void moveToStartPosition(TranslateTransition translateTransition){

        translateTransition.setToX(0);
        translateTransition.play();

    }

    private void updateGameInfoUI(Game game){
        gameTitleLabel.setText("Selected game: " + game.getTitle());
        selectedGameLabel.setText("Game " + (selectedIndex + 1) + " of " + games.size());

        String[] publishers = game.getPublishers();
        if(publishers == null || publishers.length == 0){
            publisherLabel.setText("Publisher: unknown");
        }else{
            publisherLabel.setText("Publisher: " + String.join(", ", publishers));
        }

    }

    private void clearGameInfoUI(){

        gameTitleLabel.setText("");
        publisherLabel.setText("");
        selectedGameLabel.setText("No game selected");

    }

    public static GameShelf getInstance(List<Game> games, SteamStoreService steamStoreService, LauncherService launcherService, int spacingX){

        if(instance == null){
            instance = new GameShelf(games, steamStoreService, launcherService, spacingX);
        }

        return instance;

    }

    private void launchGame(Game game){

        try {
            launcherService.launch(game);
        }catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could not launch the game");
            alert.setHeaderText(game.getTitle() + " could not be launched through Steam");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }

    private enum SelectedDirection{

        LEFT, RIGHT

    }

    private int moveSelectedIndex(SelectedDirection direction){
        if(selectedIndex == -1 && direction == SelectedDirection.LEFT){
            return gameCases.size() - 1;
        }

        if(direction == SelectedDirection.LEFT){

            return Math.floorMod(selectedIndex - 1, gameCases.size());

        }

        return (selectedIndex + 1) % gameCases.size();

    }


}

