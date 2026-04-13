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
    private Stage mainStage;
    private SplashScreen splashScreen;

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        
        splashScreen = new SplashScreen();
        splashScreen.show();
        
        splashScreen.updateProgress(1, "Checking dependencies...", 10);

        Thread.startVirtualThread(() -> {
            try {
                initializeServices();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    splashScreen.showError(e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void initializeServices() throws Exception {
        splashScreen.updateProgress(1, "Checking dependencies...", 25);
        
        LibreTranslateManager libManager = new LibreTranslateManager();
        libManager.setStatusCallback((status, isError) -> {
            Platform.runLater(() -> {
                if (isError) {
                    splashScreen.showError(status);
                } else {
                    splashScreen.updateProgress(2, status, 50);
                }
            });
        });
        
        libManager.start();
        
        Platform.runLater(() -> {
            splashScreen.updateProgress(3, "Loading application...", 75);
            
            showMainApp();
            
            splashScreen.updateProgress(4, "Done!", 100);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            Platform.runLater(splashScreen::close);
        });
    }

    private void showMainApp() {
        LayoutManagerView view = new LayoutManagerView();
        Scene scene = new Scene(view, 600, 400);

        mainStage.setTitle("Tradutor de Tela");
        mainStage.setScene(scene);
        mainStage.show();

        Platform.runLater(() -> {
            long hwnd = WindowHandleUtil.getHWND(mainStage);
            if (hwnd != 0) {
                WinOverlayUtil.makeWindowTransparent(hwnd);
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