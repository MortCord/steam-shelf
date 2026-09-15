package com.mort.shelflauncher.steamshelflauncher;

import com.mort.shelflauncher.steamshelflauncher.repository.GameRepository;
import com.mort.shelflauncher.steamshelflauncher.service.LauncherService;
import com.mort.shelflauncher.steamshelflauncher.service.SteamLibraryService;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService;
import com.mort.shelflauncher.steamshelflauncher.service.exception.LibraryPathsNotFoundException;
import com.mort.shelflauncher.steamshelflauncher.service.exception.SteamPathNotFoundException;
import com.mort.shelflauncher.steamshelflauncher.ui.GameShelf;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;


public class App extends Application {

    private final GameRepository gameRepository;
    private final LauncherService launcherService;
    private final SteamStoreService steamStoreService;

    public App(){

        steamStoreService = new SteamStoreService();
        gameRepository = new GameRepository(new SteamLibraryService(steamStoreService));
        launcherService = new LauncherService();

    }

    @Override
    public void start(Stage stage) {

        VBox vBox = new VBox();
        Label label = new Label("Steam Shelf");
        Button button = new Button("Open library");

        addElementsToBox(vBox, label, button);
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);

        VBox libraryBox = new VBox();
        Label gamesLabel = new Label("My Games");
        Button backButton = new Button("Back");

        addElementsToBox(libraryBox, gamesLabel);

        try {
            SubScene subScene = new GameShelf(gameRepository.getGames(), steamStoreService, launcherService).createScene();
            addElementsToBox(libraryBox, subScene);
        } catch (SteamPathNotFoundException | LibraryPathsNotFoundException | IOException | InterruptedException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Steam Shelf");
            alert.setHeaderText("Could not fetch Steam library");
            alert.setContentText(e.toString());
            alert.showAndWait();
        }

        addElementsToBox(libraryBox, backButton);

        libraryBox.setSpacing(20);
        libraryBox.setAlignment(Pos.CENTER);




        stage.setTitle("Steam Shelf");
        Scene scene = new Scene(vBox, 800, 600);

        button.setOnAction(event -> scene.setRoot(libraryBox));
        backButton.setOnAction(event -> scene.setRoot(vBox));

        stage.setScene(scene);
        stage.show();

    }

    private void addElementsToBox(Pane pane, Node... nodes){

        for(Node node : nodes){

            pane.getChildren().add(node);

        }

    }

}
