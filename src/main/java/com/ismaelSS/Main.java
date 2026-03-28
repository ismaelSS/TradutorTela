package com.ismaelSS;


import com.ismaelSS.layouts.Region;
import javafx.application.Application;
import javafx.stage.Stage;
import space.dynomake.libretranslate.Language;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

        selector.startSelection(region -> {

            try {
                System.out.println("==== REGIÃO ====");
                System.out.println("X: " + region.getX());
                System.out.println("Y: " + region.getY());
                System.out.println("W: " + region.getWidth());
                System.out.println("H: " + region.getHeight());

                BufferedImage img = ScreenCapture.capture(region);

                String rawText = textExtractor.extract(img);

                System.out.println("==== ORIGINAL ====");
                System.out.println(rawText);

                String processed = processText(rawText);

                String translated = cache.computeIfAbsent(
                        processed,
                        t -> translatePreservingFormat(t)
                );

                System.out.println("==== TRADUZIDO ====");
                System.out.println(translated);

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