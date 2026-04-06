package com.ismaelSS;


import com.ismaelSS.layoutManagerView.LayoutManagerView;
import com.ismaelSS.nativewin.WinOverlayUtil;
import com.ismaelSS.nativewin.WindowHandleUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import space.dynomake.libretranslate.Language;

import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private TranslateService translateService;
    private TextExtractor textExtractor;

    private Map<String, String> cache = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        LayoutManagerView view = new LayoutManagerView();
        Scene scene = new Scene(view, 600, 400);

        primaryStage.setTitle("Tradutor de Tela");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Adicione isso para que a janela do programa também não saia no print
        Platform.runLater(() -> {
            long hwnd = WindowHandleUtil.getHWND(primaryStage);
            if (hwnd != 0) {
                WinOverlayUtil.makeWindowTransparent(hwnd);
                // Se você quiser que a janela principal ainda receba cliques,
                // você pode criar um método específico no WinOverlayUtil que apenas
                // chama o SetWindowDisplayAffinity sem o WS_EX_TRANSPARENT.
            }
        });

        textExtractor = new TextExtractor();
        translateService = new TranslateService();
    }

    private String processText(String text) {

        String[] linhas = text.split("\n");
        StringBuilder resultado = new StringBuilder();

        for (String linha : linhas) {

            String indent = linha.replaceAll("^(\\s*).*", "$1");
            String conteudo = linha.trim();

            if (!conteudo.isEmpty()) {
                resultado.append(indent).append(conteudo);
            }

            resultado.append("\n");
        }

        return resultado.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}