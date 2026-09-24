package com.mort.shelflauncher.steamshelflauncher.ui;

import com.mort.shelflauncher.steamshelflauncher.model.Game;
import com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService.SteamStoreService;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GameCase extends Group {

    private final Game game;
    private final SteamStoreService steamStoreService;
    private final double normalZ;
    private final double selectedZ;
    private Rectangle cover;
    private Animation currentAnimation;

    private final static Logger logger = LoggerFactory.getLogger(GameCase.class);

    public GameCase(Game game, SteamStoreService steamStoreService, double normalZ, double selectedZ, int x){

        this.game = game;
        this.steamStoreService = steamStoreService;
        this.normalZ = normalZ;
        this.selectedZ = selectedZ;

        init(x);
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

        Image boxImage = new Image(Objects.requireNonNull(getClass().getResource("/images/boxTexture.jpg")).toExternalForm());
        PhongMaterial phongMaterial = new PhongMaterial();
        box.setMaterial(phongMaterial);
        phongMaterial.setDiffuseMap(boxImage);

        cover.setFill(pattern);

        getChildren().add(cover);
        getChildren().add(box);

        fetchHeaderImage();



    }

    private void fetchHeaderImage(){

        CompletableFuture.runAsync(() -> {
            Optional<String> imageString = steamStoreService.fetchLibraryCoverUrl(game.getSteamAppId());

            if(imageString.isPresent()){
                Image image = new Image(imageString.get());
                if(image.isError()){
                    return;
                }
                ImagePattern fetchedPattern = new ImagePattern(image);
                Platform.runLater(() -> cover.setFill(fetchedPattern));
            }


        });

    }

    public void select() {

        if(currentAnimation != null){
            currentAnimation.stop();
        }

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), this);
        translateTransition.setFromZ(getTranslateZ());
        translateTransition.setToZ(selectedZ);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), this);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setFromAngle(getRotate());
        rotateTransition.setToAngle(-90);

        currentAnimation = new SequentialTransition(translateTransition, rotateTransition);
        currentAnimation.play();


    }

    public void deselect(){

        if(currentAnimation != null){
            currentAnimation.stop();
        }

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), this);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setFromAngle(getRotate());
        rotateTransition.setToAngle(0);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), this);
        translateTransition.setToZ(normalZ);
        translateTransition.setFromZ(getTranslateZ());

        currentAnimation = new SequentialTransition(rotateTransition, translateTransition);
        currentAnimation.play();


    }

    public Game getGame(){

        return game;

    }
}
