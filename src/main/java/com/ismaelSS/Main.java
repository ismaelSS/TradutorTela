package com.ismaelSS;

import com.ismaelSS.translate.TranslateService;
import javafx.application.Application;
import javafx.stage.Stage;
import net.sourceforge.tess4j.Tesseract;
import space.dynomake.libretranslate.Language;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private Tesseract tesseract;
    private TranslateService translateService;

    // 🔥 cache de tradução (melhora MUITO performance)
    private Map<String, String> cache = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {


        // 🔍 OCR setup
        tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");

        // 🔥 melhora layout
        tesseract.setPageSegMode(1);
        tesseract.setTessVariable("preserve_interword_spaces", "1");

        // 🌍 tradução
        translateService = new TranslateService();

        ScreenSelector selector = new ScreenSelector();

        selector.startSelection((x, y, w, h) -> {
            try {
                BufferedImage img = ScreenCapture.capture(x, y, w, h);

                // 🔍 OCR
                String rawText = tesseract.doOCR(img);

                // 🧠 formatação
                String processedText = processText(rawText);

                System.out.println("==== ORIGINAL ====");
                System.out.println(processedText);

                // 🌍 TRADUÇÃO
                String translatedText = translatePreservingFormat(processedText);

                System.out.println("==== TRADUZIDO ====");
                System.out.println(translatedText);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 🔥 preserva layout do OCR
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

    // 🌍 traduz mantendo layout
    private String translatePreservingFormat(String text) {
        // 🔥 traduz tudo de uma vez
        String translated = translateService.translate(text, Language.ENGLISH, Language.PORTUGUESE);

        // mantém estrutura original
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