package com.ismaelSS;


import com.ismaelSS.layoutManagerView.LayoutManagerView;
import javafx.application.Application;
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
        scene.getStylesheets().add("/styles/layoutManagerView.css");

        primaryStage.setTitle("Tradutor de Tela");
        primaryStage.setScene(scene);
        primaryStage.show();

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

    private String translatePreservingFormat(String text) {

        String translated = translateService.translate(
                text,
                Language.ENGLISH,
                Language.PORTUGUESE
        );

        String[] originalLines = text.split("\n");
        String[] translatedLines = translated.split("\n");

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < originalLines.length; i++) {

            String indent = originalLines[i].replaceAll("^(\\s*).*", "$1");

            String linhaTraduzida = i < translatedLines.length
                    ? translatedLines[i]
                    : "";

            result.append(indent)
                    .append(linhaTraduzida)
                    .append("\n");
        }

        return result.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}