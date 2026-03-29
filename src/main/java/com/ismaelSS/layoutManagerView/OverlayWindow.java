package com.ismaelSS.layoutManagerView;

import com.ismaelSS.layouts.Region;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class OverlayWindow {

    private Stage stage;
    private Text textNode;
    private StackPane root;

    public OverlayWindow(Region region) {

        textNode = new Text();
        textNode.setFill(Color.WHITE);

        // 🔥 fonte monoespaçada (ESSENCIAL pra indentação)
        textNode.setStyle("-fx-font-family: 'Consolas';");

        root = new StackPane(textNode);
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: rgba(0,0,0,1); -fx-padding: 5;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);

        // 🔥 NÃO BLOQUEIA CLIQUE
        //    stage.setMouseTransparent(true);

        stage.setScene(scene);

        // 🔥 posicionamento correto
        stage.setX(region.getX());
        stage.setY(region.getY());
        stage.setWidth(region.getWidth());
        stage.setHeight(region.getHeight());

        stage.show();
    }

    // =========================
    // 🔥 ATUALIZA TEXTO
    // =========================
    public void updateText(String content) {

        if (content == null || content.trim().isEmpty()) {
            stage.hide();
            return;
        }

        stage.show();

        textNode.setText(content);

        adjustFontSize();
    }

    // =========================
    // 🔥 AJUSTA TAMANHO DA FONTE
    // =========================
    private void adjustFontSize() {

        double maxWidth = stage.getWidth() - 10;
        double maxHeight = stage.getHeight() - 10;

        double size = 50;

        while (size > 5) {

            textNode.setStyle(
                    "-fx-font-size: " + size + "px;" +
                            "-fx-fill: yellow;" +
                            "-fx-font-weight: bold;" +
                            "-fx-stroke: black;" +
                            "-fx-stroke-width: 1px;"

            );

            textNode.setWrappingWidth(maxWidth);

            // 🔥 força recalcular layout
            root.applyCss();
            root.layout();

            Bounds bounds = textNode.getLayoutBounds();

            if (bounds.getHeight() <= maxHeight) {
                break;
            }

            size -= 1;
        }
    }

    public void hideOverlay() {
        stage.setOpacity(0);
    }

    public void showOverlay() {
        stage.setOpacity(1);
    }

    // =========================
    // 🔥 FECHAR OVERLAY
    // =========================
    public void close() {
        stage.close();
    }
}