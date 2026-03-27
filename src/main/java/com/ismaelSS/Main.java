package com.ismaelSS;

import javafx.application.Application;
import javafx.stage.Stage;
import space.dynomake.libretranslate.Language;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private TranslateService translateService;
    private TextExtractor textExtractor;

    private Map<String, String> cache = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        textExtractor = new TextExtractor();
        translateService = new TranslateService();

        ScreenSelector selector = new ScreenSelector();

        selector.startSelection((x, y, w, h) -> {
            try {
                BufferedImage img = ScreenCapture.capture(x, y, w, h);

                String rawText = textExtractor.extract(img);

                String processedText = processText(rawText);

                System.out.println("==== ORIGINAL ====");
                System.out.println(processedText);

                String translatedText = translatePreservingFormat(processedText);

                System.out.println("==== TRADUZIDO ====");
                System.out.println(translatedText);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

        String translated = translateService.translate(text, Language.ENGLISH, Language.PORTUGUESE);

        String[] originalLines = text.split("\n");
        String[] translatedLines = translated.split("\n");

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < originalLines.length; i++) {

            String indent = originalLines[i].replaceAll("^(\\s*).*", "$1");

            String linhaTraduzida = i < translatedLines.length
                    ? translatedLines[i]
                    : "";

            result.append(indent).append(linhaTraduzida).append("\n");
        }

        return result.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}