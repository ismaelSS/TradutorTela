package com.ismaelSS.layoutManagerView;

import com.ismaelSS.ScreenCapture;
import com.ismaelSS.ScreenSelector;
import com.ismaelSS.TextExtractor;
import com.ismaelSS.TranslateService;
import com.ismaelSS.layouts.Layout;
import com.ismaelSS.layouts.Region;
import com.ismaelSS.storage.LayoutStorage;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private TextExtractor textExtractor = new TextExtractor();
    private TranslateService translateService = new TranslateService();

    // Map para controlar as janelas de overlay abertas
    private Map<Region, OverlayWindow> overlays = new HashMap<>();

    // Gerenciamento de Janela Alvo
    private ComboBox<WindowItem> comboWindows = new ComboBox<>();
    private long selectedHwnd = 0;

    private final ExecutorService workerPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    private ScheduledExecutorService executor;

    public LayoutManagerView() {
        setupUI();
        loadData();
        startLoop();
    }

    private void setupUI() {
        setPadding(new Insets(10));

        // --- PAINEL SUPERIOR (Seleção de Janela) ---
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(0, 0, 10, 0));

        Button btnRefresh = new Button("🔄 Atualizar Janelas");
        comboWindows.setPromptText("Selecione a janela alvo...");
        comboWindows.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboWindows, javafx.scene.layout.Priority.ALWAYS);

        btnRefresh.setOnAction(e -> refreshWindowList());
        comboWindows.setOnAction(e -> {
            WindowItem selected = comboWindows.getSelectionModel().getSelectedItem();
            if (selected != null) selectedHwnd = selected.hwnd;
        });

        topPanel.getChildren().addAll(comboWindows, btnRefresh);
        setTop(topPanel);

        // --- PAINEL LATERAL (Layouts) ---
        VBox leftBox = new VBox(5);
        leftBox.getChildren().addAll(new Label("Layouts:"), layoutList);

        Button btnAddLayout = new Button("Novo Layout");
        Button btnRemoveLayout = new Button("Remover Layout");
        btnAddLayout.setOnAction(e -> addLayout());
        btnRemoveLayout.setOnAction(e -> removeLayout());

        leftBox.getChildren().addAll(btnAddLayout, btnRemoveLayout);
        setLeft(leftBox);

        // --- PAINEL CENTRAL (Regiões) ---
        VBox centerBox = new VBox(5);
        centerBox.getChildren().addAll(new Label("Regiões no Layout:"), regionList);

        Button btnAddRegion = new Button("Selecionar Área na Tela");
        Button btnRemoveRegion = new Button("Remover Região");
        btnAddRegion.setOnAction(e -> addRegionToSelectedLayout());
        btnRemoveRegion.setOnAction(e -> removeRegion());

        centerBox.getChildren().addAll(btnAddRegion, btnRemoveRegion);
        setCenter(centerBox);

        // Sincronizar listas
        layoutList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                regions.setAll(newVal.getRegions());
                clearOverlays();
            }
        });

        refreshWindowList();
    }

    private void refreshWindowList() {
        ObservableList<WindowItem> windowList = FXCollections.observableArrayList();
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
                String title = new String(windowText).trim();

                // Filtramos janelas vazias e o nosso próprio programa
                if (!title.isEmpty() && !title.equals("Tradutor de Tela")) {
                    windowList.add(new WindowItem(title, Pointer.nativeValue(hwnd.getPointer())));
                }
            }
            return true;
        }, null);
        comboWindows.setItems(windowList);
    }

    private void startLoop() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (selectedHwnd == 0 || regions.isEmpty()) return;

            for (Region region : regions) {
                workerPool.submit(() -> {
                    try {
                        // Captura apenas da janela alvo
                        BufferedImage img = ScreenCapture.captureWindowRegion(selectedHwnd, region);
                        String text = textExtractor.extract(img).trim();

                        if (!text.isEmpty()) {
                            String translated = translateService.translate(text, Language.ENGLISH, Language.PORTUGUESE);

                            Platform.runLater(() -> {
                                OverlayWindow overlay = overlays.computeIfAbsent(region, r -> new OverlayWindow(r));
                                overlay.updateText(translated);
                                overlay.showOverlay();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private void clearOverlays() {
        overlays.values().forEach(OverlayWindow::hideOverlay);
        overlays.clear();
    }

    private void loadData() {
        layouts.setAll(LayoutStorage.load());
    }

    // --- Métodos de CRUD de Layout (Baseados no seu código original) ---
    private void addLayout() {
        TextInputDialog dialog = new TextInputDialog("Novo Layout");
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
            clearOverlays();
        }
    }

    private void addRegionToSelectedLayout() {
        Layout selected = layoutList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

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

    // Classe auxiliar para o ComboBox
    private static class WindowItem {
        String title;
        long hwnd;
        WindowItem(String title, long hwnd) {
            this.title = title;
            this.hwnd = hwnd;
        }
        @Override public String toString() { return title; }
    }
}