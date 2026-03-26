package com.ismaelSS;

import javafx.application.Application;
import javafx.stage.Stage;
import net.sourceforge.tess4j.Tesseract;

import java.awt.image.BufferedImage;
import java.io.*;

public class Main extends Application {

    private Tesseract tesseract;

    @Override
    public void start(Stage primaryStage) {

        tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");

        tesseract.setPageSegMode(1);
        tesseract.setTessVariable("preserve_interword_spaces", "1");

        ScreenSelector selector = new ScreenSelector();

        selector.startSelection((x, y, w, h) -> {
            try {
                BufferedImage img = ScreenCapture.capture(x, y, w, h);

                String rawText = tesseract.doOCR(img);

                String processedText = processText(rawText);

                System.out.println("==== ORIGINAL ====");
                System.out.println(processedText);

                String translatedText = translateWithPython(processedText);

                System.out.println("==== TRADUZIDO ====");
                System.out.println(translatedText);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String translateWithPython(String text) {


        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Users\\ismae\\AppData\\Local\\Python\\pythoncore-3.14-64\\python.exe",
                    "translate.py");

            pb.directory(new File("src/main/python/scripts"));
            Process process = pb.start();

            try (OutputStream os = process.getOutputStream()) {
                os.write(text.getBytes());
                os.flush();
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder resultado = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                resultado.append(line).append("\n");
            }

            process.waitFor();

            return resultado.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }
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