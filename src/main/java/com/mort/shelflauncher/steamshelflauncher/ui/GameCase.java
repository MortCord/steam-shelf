package com.mort.shelflauncher.steamshelflauncher.ui;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class GameCase extends Group {

    private final Game game;
    private final SteamStoreService steamStoreService;
    private final double normalZ;
    private final double selectedZ;
    private Rectangle cover;

    public GameCase(Game game, SteamStoreService steamStoreService, double normalZ, double selectedZ, int x){

        this.game = game;
        this.steamStoreService = steamStoreService;
        this.normalZ = normalZ;
        this.selectedZ = selectedZ;

        init(x);



    }

    private Color getRandomColor(){

        Random random = new Random();

        Color[] colors = {Color.WHITE, Color.GOLD, Color.VIOLET, Color.DARKBLUE, Color.FIREBRICK};

        return colors[random.nextInt(0, colors.length)];

    }

    private void init(int x){

        cover = new Rectangle(-60, -100, 120, 200);
        cover.setRotationAxis(Rotate.Y_AXIS);
        cover.setRotate(90);
        cover.setTranslateX(-5.5);

        Box box = new Box(10, 200, 120);
        setTranslateX(x);
        setTranslateY(0);
        setTranslateZ(normalZ);
        Image defaultImage = new Image(Objects.requireNonNull(getClass().getResource("/images/Untitled.png")).toExternalForm());
        ImagePattern pattern = new ImagePattern(defaultImage);
        box.setMaterial(new PhongMaterial(getRandomColor()));
        cover.setFill(pattern);

        getChildren().add(cover);
        getChildren().add(box);

        fetchHeaderImage();



    }

    private void fetchHeaderImage(){

        CompletableFuture.runAsync(() -> {


            Optional<String> imageString = Optional.empty();
            try {
                imageString = steamStoreService.fetchHeaderImageUrl(game.getSteamAppId());
            } catch (IOException | InterruptedException ignored) {

            }

            if(imageString.isPresent()){
                    Image image = new Image(imageString.get());
                    ImagePattern fetchedPattern = new ImagePattern(image);
                    Platform.runLater(() -> cover.setFill(fetchedPattern));
                }


        });

    }

    public void select() {

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), this);
        translateTransition.setToZ(selectedZ);
        translateTransition.play();

        translateTransition.setOnFinished(event -> {

            RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), this);
            rotateTransition.setAxis(Rotate.Y_AXIS);
            rotateTransition.setToAngle(-90);
            rotateTransition.play();
            System.out.println("Selected game " + game.getTitle() + ", publisher(s): " + Arrays.toString(game.getPublishers()));
        });


    }

    public void deselect(){


        RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), this);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setToAngle(0);
        rotateTransition.play();

        rotateTransition.setOnFinished(event -> {

            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), this);
            translateTransition.setToZ(normalZ);
            translateTransition.play();

        });

    }
}
