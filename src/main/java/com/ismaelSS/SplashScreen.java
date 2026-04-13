package com.ismaelSS;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {

    private static final Color BG_COLOR = Color.web("#0d1117");
    private static final Color ACCENT_COLOR = Color.web("#00ff88");
    private static final Color DIM_COLOR = Color.web("#30363d");
    private static final Color TEXT_COLOR = Color.web("#c9d1d9");
    private static final Color SUBTEXT_COLOR = Color.web("#8b949e");

    private Stage stage;
    private Scene scene;
    private VBox stepList;
    private Rectangle progressBar;
    private Rectangle progressBarBg;
    private Label percentLabel;
    private Label statusLabel;
    private Timeline glowAnimation;
    private int totalSteps = 4;
    private StepLabel[] stepLabels;

    public interface SplashCallback {
        void onStageComplete(int step, String status);
    }

    public SplashScreen() {
        buildUI();
    }

    private void buildUI() {
        stepLabels = new StepLabel[totalSteps];
        VBox root = new VBox(30);
        root.setPadding(new Insets(40, 60, 40, 60));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + toHex(BG_COLOR));

        Label title = createTitleLabel();
        VBox progressContainer = createProgressBar();
        VBox stepsContainer = createStepsList();
        Label status = createStatusLabel();

        root.getChildren().addAll(title, progressContainer, stepsContainer, status);
        root.setPrefSize(500, 400);

        scene = new Scene(root);
        scene.setFill(BG_COLOR);
    }

    private Label createTitleLabel() {
        Label label = new Label("TRADUTOR DE TELA");
        label.setFont(Font.font("Consolas", FontWeight.BOLD, 28));
        label.setTextFill(ACCENT_COLOR);
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(400);

        addGlowEffect(label);
        return label;
    }

    private void addGlowEffect(Label label) {
        label.setStyle(label.getStyle() 
            + "-fx-effect: dropshadow(glow, " + toHex(ACCENT_COLOR) + ", 10, 0.8, 0, 0);");
    }

    private VBox createProgressBar() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPrefWidth(400);

        StackPane progressWrapper = new StackPane();
        progressWrapper.setPrefSize(400, 24);

        progressBarBg = new Rectangle(400, 24);
        progressBarBg.setArcHeight(12);
        progressBarBg.setArcWidth(12);
        progressBarBg.setFill(DIM_COLOR);

        progressBar = new Rectangle(0, 24);
        progressBar.setArcHeight(12);
        progressBar.setArcWidth(12);
        progressBar.setFill(ACCENT_COLOR);
        progressBar.setTranslateX(0);

        progressWrapper.getChildren().addAll(progressBarBg, progressBar);

        percentLabel = new Label("0%");
        percentLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        percentLabel.setTextFill(TEXT_COLOR);
        percentLabel.setAlignment(Pos.CENTER);
        percentLabel.setPrefWidth(400);

        container.getChildren().addAll(progressWrapper, percentLabel);
        return container;
    }

    private VBox createStepsList() {
        stepList = new VBox(8);
        stepList.setAlignment(Pos.CENTER_LEFT);
        stepList.setPrefWidth(400);

        String[] stepNames = {
            "Checking dependencies...",
            "Downloading WinPython...",
            "Installing LibreTranslate...",
            "Loading application..."
        };

        for (int i = 0; i < totalSteps; i++) {
            stepLabels[i] = new StepLabel(stepNames[i], i, i == 0);
            stepList.getChildren().add(stepLabels[i].getLabel());
        }

        return stepList;
    }

    private Label createStatusLabel() {
        statusLabel = new Label("Initializing...");
        statusLabel.setFont(Font.font("Consolas", 12));
        statusLabel.setTextFill(SUBTEXT_COLOR);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setPrefWidth(400);
        return statusLabel;
    }

    private class StepLabel {
        private final Label label;
        private final int index;
        private boolean completed;

        StepLabel(String text, int stepIndex, boolean active) {
            this.label = new Label("  " + text);
            this.label.setFont(Font.font("Consolas", 14));
            this.index = stepIndex;
            updateStyle(active);
        }

        void updateStyle(boolean active) {
            if (completed) {
                label.setTextFill(ACCENT_COLOR);
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + toHex(ACCENT_COLOR) + ";");
            } else if (active) {
                label.setTextFill(TEXT_COLOR);
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + toHex(TEXT_COLOR) + ";");
            } else {
                label.setTextFill(SUBTEXT_COLOR);
                label.setStyle("-fx-text-fill: " + toHex(SUBTEXT_COLOR) + ";");
            }
        }

        void setCompleted() {
            completed = true;
            updateStyle(false);
            label.setText("\u2713 " + label.getText().substring(2));
        }

        Label getLabel() {
            return label;
        }
    }

    public void show() {
        stage = new Stage(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(400);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        startGlowAnimation();
    }

    public void close() {
        stopGlowAnimation();
        if (stage != null) {
            stage.close();
        }
    }

    public void updateProgress(int step, String status, int percent) {
        if (step < 1 || step > totalSteps) return;

        for (int i = 0; i < totalSteps; i++) {
            if (i < step - 1) {
                stepLabels[i].setCompleted();
            } else if (i == step - 1) {
                stepLabels[i].updateStyle(true);
            } else {
                stepLabels[i].updateStyle(false);
            }
        }

        double barWidth = (400.0 * percent) / 100.0;
        progressBar.setWidth(Math.max(0, barWidth));

        percentLabel.setText(percent + "%");

        if (status != null && !status.isEmpty()) {
            statusLabel.setText(status);
        }

        stage.setTitle("Loading... " + percent + "%");
    }

    public void complete() {
        updateProgress(totalSteps, "Done!", 100);

        for (int i = 0; i < totalSteps; i++) {
            stepLabels[i].setCompleted();
        }
        progressBar.setWidth(400);

        stopGlowAnimation();
    }

    public void showError(String error) {
        stopGlowAnimation();
        statusLabel.setText("Error: " + error);
        statusLabel.setTextFill(Color.web("#ff4444"));
    }

    public Stage getStage() {
        return stage;
    }

    private void startGlowAnimation() {
        final double[] glowIntensity = {0.8};

        glowAnimation = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            double newIntensity = 0.8 + 0.4 * Math.sin(System.currentTimeMillis() / 500.0);
            progressBar.setFill(ACCENT_COLOR.deriveColor(0, 1, 1, newIntensity));
        }));
        glowAnimation.setCycleCount(Animation.INDEFINITE);
        glowAnimation.play();
    }

    private void stopGlowAnimation() {
        if (glowAnimation != null) {
            glowAnimation.stop();
            progressBar.setFill(ACCENT_COLOR);
        }
    }

    private String toHex(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}