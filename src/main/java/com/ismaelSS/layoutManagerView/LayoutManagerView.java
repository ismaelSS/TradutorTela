package com.ismaelSS.layoutManagerView;

import com.ismaelSS.ScreenCapture;
import com.ismaelSS.ScreenSelector;
import com.ismaelSS.TextExtractor;
import com.ismaelSS.TranslateService;
import com.ismaelSS.layouts.Layout;
import com.ismaelSS.layouts.Region;
import com.ismaelSS.storage.LayoutStorage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import space.dynomake.libretranslate.Language;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class LayoutManagerView extends BorderPane {

    private ObservableList<Layout> layouts = FXCollections.observableArrayList();
    private ObservableList<Region> regions = FXCollections.observableArrayList();

    private ListView<Layout> layoutList = new ListView<>(layouts);
    private ListView<Region> regionList = new ListView<>(regions);

    private ScreenSelector screenSelector = new ScreenSelector();

    private ExecutorService workerPool = Executors.newFixedThreadPool(4);


    // 🔥 MOTOR
    private ScheduledExecutorService executor;
    private boolean running = false;

    // 🔥 OCR + tradução
    private TextExtractor extractor = new TextExtractor();
    private TranslateService translator = new TranslateService();

    // 🔥 overlays ativos
    private Map<Region, OverlayWindow> overlays = new HashMap<>();

    public LayoutManagerView() {

        layouts.addAll(LayoutStorage.load());

        // =========================
        // LEFT — Layouts
        // =========================
        VBox left = new VBox(10);
        left.setPadding(new Insets(10));

        Button addLayout = new Button("+ Layout");
        Button removeLayout = new Button("- Layout");

        layoutList.setPrefWidth(200);

        left.getChildren().addAll(addLayout, removeLayout, layoutList);

        // =========================
        // CENTER — Regions
        // =========================
        VBox center = new VBox(10);
        center.setPadding(new Insets(10));

        Button addRegion = new Button("+ Região");
        Button removeRegion = new Button("- Região");

        regionList.setPrefWidth(300);

        Button play = new Button("▶ Play");
        Button pause = new Button("⏸ Pause");

        center.getChildren().addAll(addRegion, removeRegion, play, pause, regionList);

        setLeft(left);
        setCenter(center);

        // =========================
        // EVENTOS
        // =========================

        layoutList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                regions.setAll(selected.getRegions());
            } else {
                regions.clear();
            }
        });

        addLayout.setOnAction(e -> createLayout());
        removeLayout.setOnAction(e -> removeLayout());

        addRegion.setOnAction(e -> addRegionToSelectedLayout());
        removeRegion.setOnAction(e -> removeRegion());

        play.setOnAction(e -> startExecution());
        pause.setOnAction(e -> stopExecution());
    }

    // =========================
    // 🔥 START
    // =========================
    private void startExecution() {

        if (running) return;

        Layout selected = layoutList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Selecione um layout primeiro!");
            return;
        }

        running = true;

        // 🔥 cria overlays UMA VEZ
        overlays.clear();
        for (Region region : selected.getRegions()) {
            overlays.put(region, new OverlayWindow(region));
        }

        executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {

            try {

                // =========================
                // 🔥 FASE 1 — CAPTURA RÁPIDA
                // =========================

                Platform.runLater(() -> {
                    overlays.values().forEach(OverlayWindow::hideOverlay);
                });

                Thread.sleep(40); // mínimo possível

                Map<Region, BufferedImage> captures = new HashMap<>();

                for (Region region : selected.getRegions()) {
                    BufferedImage img = ScreenCapture.capture(region);
                    captures.put(region, img);
                }

                // 🔥 mostra overlays IMEDIATAMENTE
                Platform.runLater(() -> {
                    overlays.values().forEach(OverlayWindow::showOverlay);
                });

                // =========================
                // 🔥 FASE 2 — PROCESSAMENTO PARALELO
                // =========================

                for (Map.Entry<Region, BufferedImage> entry : captures.entrySet()) {

                    Region region = entry.getKey();
                    BufferedImage img = entry.getValue();

                    workerPool.submit(() -> {

                        try {
                            String text = extractor.extract(img);

                            String translated = translator.translate(
                                    text,
                                    Language.ENGLISH,
                                    Language.PORTUGUESE
                            );

                            OverlayWindow overlay = overlays.get(region);

                            if (overlay != null) {
                                Platform.runLater(() -> overlay.updateText(translated));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 0, 2, TimeUnit.SECONDS);
    }

    // =========================
    // 🔥 STOP
    // =========================
    private void stopExecution() {

        running = false;

        // 🔥 para thread
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        // 🔥 fecha overlays
        overlays.values().forEach(overlay -> {
            Platform.runLater(overlay::close);
        });

        overlays.clear();

        System.out.println("Execução parada.");
    }

    // =========================
    // CRUD
    // =========================

    private void createLayout() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Nome do layout");

        dialog.showAndWait().ifPresent(name -> {
            Layout layout = new Layout(name);
            layouts.add(layout);
            LayoutStorage.save(layouts);
        });
    }

    private void removeLayout() {
        Layout selected = layoutList.getSelectionModel().getSelectedItem();

        if (selected != null) {
            layouts.remove(selected);
            regions.clear();
            LayoutStorage.save(layouts);
        }
    }

    private void addRegionToSelectedLayout() {

        Layout selected = layoutList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Selecione um layout primeiro!");
            return;
        }

        screenSelector.startSelection(region -> {

            selected.getRegions().add(region);
            regions.setAll(selected.getRegions());

            LayoutStorage.save(layouts);
        });
    }

    private void removeRegion() {

        Layout selectedLayout = layoutList.getSelectionModel().getSelectedItem();
        Region selectedRegion = regionList.getSelectionModel().getSelectedItem();

        if (selectedLayout != null && selectedRegion != null) {

            selectedLayout.getRegions().remove(selectedRegion);
            regions.setAll(selectedLayout.getRegions());

            LayoutStorage.save(layouts);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(msg);
        alert.showAndWait();
    }
}