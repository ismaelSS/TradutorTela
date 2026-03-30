package com.ismaelSS.layoutManagerView;

import com.ismaelSS.layouts.Region;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.ismaelSS.nativewin.WinOverlayUtil;
import com.ismaelSS.nativewin.WindowHandleUtil;

public class OverlayWindow {

    private Stage stage;
    private Text textNode;
    private StackPane root;

    public OverlayWindow(Region region) {

        textNode = new Text();
        textNode.setFill(Color.WHITE);

        // 🔥 fonte monoespaçada (mantém indentação PERFEITA)
        textNode.setStyle("-fx-font-family: 'Consolas';");

        root = new StackPane(textNode);
        root.setAlignment(Pos.TOP_LEFT);

        root.setStyle("-fx-background-color: rgba(2,8,23,0.8); -fx-padding: 5;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);

        // 🔥 permite clicar através da janela
//        stage.setMouseTransparent(true);

        stage.setScene(scene);

        // 🔥 posição correta
        stage.setX(region.getX());
        stage.setY(region.getY() -10);
        stage.setWidth(region.getWidth() );
        stage.setHeight(region.getHeight()-2);

        stage.show();

        // =========================
        // 🔥 INTEGRAÇÃO NATIVA (WINDOWS)
        // =========================
        stage.setOnShown(e -> {
            long hwnd = WindowHandleUtil.getHWND(stage);

            System.out.println("HWND: " + hwnd);

            if (hwnd != 0) {
                WinOverlayUtil.makeWindowTransparent(hwnd);
            } else {
                System.out.println("❌ Falha ao obter HWND");
            }
        });
    }

    // =========================
    // 🔥 ATUALIZA TEXTO
    // =========================
    public void updateText(String content) {

        if (content == null || content.trim().isEmpty()) {
            stage.hide();
            return;
        }

        if (!stage.isShowing()) {
            stage.show();
        }

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
                    "-fx-font-family: 'Consolas';" + // 🔥 mantém monoespaçado
                            "-fx-font-size: " + size + "px;" +
                            "-fx-fill: rgb(245 158 11);" +
                            "-fx-font-weight: bold;"
//                            "-fx-stroke: black;" +
//                            "-fx-stroke-width: 1px;"
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

    // =========================
    // 🔥 CONTROLE VISUAL
    // =========================
    public void hideOverlay() {
        stage.setOpacity(0);
    }

    public void showOverlay() {
        if (!stage.isShowing()) {
            stage.show();

            // Aplica a mágica do click-through após mostrar a janela
            Platform.runLater(() -> {
                long hwnd = WindowHandleUtil.getHWND(stage);
                if (hwnd != 0) {
                    WinOverlayUtil.makeWindowTransparent(hwnd);
                }
            });
        }
        stage.setOpacity(1);
    }

    // =========================
    // 🔥 FECHAR OVERLAY
    // =========================
    public void close() {
        stage.close();
    }
}