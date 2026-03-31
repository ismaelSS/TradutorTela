package com.ismaelSS;

import com.ismaelSS.layouts.Region;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ScreenSelector {

    public void startSelection(OnAreaSelected callback) {

        Stage stage = new Stage();
        Pane root = new Pane();

        Scene scene = new Scene(root,
                Screen.getPrimary().getBounds().getWidth(),
                Screen.getPrimary().getBounds().getHeight());

        scene.setFill(Color.color(0, 0, 0, 0.3));

        Rectangle rect = new Rectangle();
        rect.setStroke(Color.color(0.992156862, 0.603921569, 0, 0.8));
        rect.setFill(Color.color(0.00784313725, 0.031372549, 0.0901960784, 0.8));

        root.getChildren().add(rect);
        root.setStyle("-fx-background-color: transparent;");


        final double[] startX = new double[1];
        final double[] startY = new double[1];

        scene.setOnMousePressed(e -> {
            startX[0] = e.getSceneX();
            startY[0] = e.getSceneY();

            rect.setX(startX[0]);
            rect.setY(startY[0]);
            rect.setWidth(0);
            rect.setHeight(0);
        });

        scene.setOnMouseDragged(e -> {
            double width = e.getSceneX() - startX[0];
            double height = e.getSceneY() - startY[0];

            rect.setWidth(Math.abs(width));
            rect.setHeight(Math.abs(height));
            rect.setX(Math.min(startX[0], e.getSceneX()));
            rect.setY(Math.min(startY[0], e.getSceneY()));
        });

        scene.setOnMouseReleased(e -> {

            Region region = new Region(
                    (int) rect.getX(),
                    (int) rect.getY(),
                    (int) rect.getWidth(),
                    (int) rect.getHeight()
            );

            stage.close();

            callback.onSelect(region);
        });

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.show();
    }
}