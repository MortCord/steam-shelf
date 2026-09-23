package com.mort.shelflauncher.steamshelflauncher;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import com.mort.shelflauncher.steamshelflauncher.repository.GameRepository;
import com.mort.shelflauncher.steamshelflauncher.service.LauncherService.LauncherService;
import com.mort.shelflauncher.steamshelflauncher.service.LauncherService.LauncherServiceImpl;
import com.mort.shelflauncher.steamshelflauncher.service.SteamLibraryService.SteamLibraryServiceImpl;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService.SteamStoreService;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService.SteamStoreServiceImpl;
import com.mort.shelflauncher.steamshelflauncher.service.exception.LibraryPathsNotFoundException;
import com.mort.shelflauncher.steamshelflauncher.service.exception.SteamPathNotFoundException;
import com.mort.shelflauncher.steamshelflauncher.ui.GameShelf;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.Objects;


public class App extends Application {

    private final GameRepository gameRepository;
    private final LauncherService launcherService;
    private final SteamStoreService steamStoreService;
    private List<Game> games;

    public App(){

        steamStoreService = new SteamStoreServiceImpl();
        gameRepository = new GameRepository(new SteamLibraryServiceImpl(steamStoreService));
        launcherService = new LauncherServiceImpl();

    }

    @Override
    public void start(Stage stage) {

        VBox vBox = new VBox();
        vBox.getStyleClass().add("main-view");

        Label label = new Label("Steam Shelf");
        label.getStyleClass().add("app-title");

        Button button = new Button("Open library");
        button.getStyleClass().add("main-button");

        addElementsToBox(vBox, label, button);
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.CENTER);

        VBox libraryBox = new VBox();
        libraryBox.getStyleClass().add("library-view");

        Label gamesLabel = new Label("My Library");
        gamesLabel.getStyleClass().add("library-title");

        addElementsToBox(libraryBox, gamesLabel);

        Label loadingLabel = new Label();

        addElementsToBox(libraryBox, loadingLabel);

        libraryBox.setSpacing(20);
        libraryBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");

        stage.setTitle("Steam Shelf");
        Scene scene = new Scene(vBox, 1280, 720);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/app.css")).toExternalForm());

        button.setOnAction(event -> {
            scene.setRoot(libraryBox);

            loadingLabel.setText("Loading Steam library...");
            Thread thread = getLoadingThread(libraryBox, loadingLabel, backButton);
            thread.start();

            backButton.setOnAction(e -> scene.setRoot(vBox));

        });

        stage.setScene(scene);
        stage.show();

    }

    private Thread getLoadingThread(VBox libraryBox, Label loadingLabel, Button backButton) {
        Task<List<Game>> task = new Task<>() {
            @Override
            protected List<Game> call() throws Exception {
                try {
                    return gameRepository.getGames();
                } catch (SteamPathNotFoundException | LibraryPathsNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        task.setOnSucceeded(event -> {
            loadingLabel.setText("");

            if(games == null){
                addElementsToBox(libraryBox, GameShelf.getInstance(task.getValue(), steamStoreService, launcherService, 50).createView());
                games = task.getValue();

                addElementsToBox(libraryBox, backButton);
            } else if (!games.equals(task.getValue())) {
                // TODO Refactor GameShelf into singleton and rerender games if new ones fetched
            }
        });

        task.setOnFailed(event -> {
            loadingLabel.setText("");
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Steam Shelf");
            alert.setHeaderText("Could not fetch Steam library");
            alert.setContentText(String.valueOf(task.getException()));
            alert.showAndWait();
            addElementsToBox(libraryBox, backButton);

        });



        Thread thread = new Thread(task);
        thread.setDaemon(true);
        return thread;
    }
    private void addElementsToBox(Pane pane, Node... nodes){

        for(Node node : nodes){

            pane.getChildren().add(node);

        }

    }

}
