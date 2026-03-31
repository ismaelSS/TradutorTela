package com.ismaelSS.layoutManagerView;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.ismaelSS.ScreenCapture;
import com.ismaelSS.ScreenSelector;
import com.ismaelSS.TextExtractor;
import com.ismaelSS.TranslateService;
import com.ismaelSS.layouts.Layout;
import com.ismaelSS.layouts.Region;
import com.ismaelSS.storage.LayoutStorage;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class LayoutManagerView extends BorderPane implements NativeKeyListener {

    private ObservableList<Layout> layouts = FXCollections.observableArrayList();
    private ObservableList<Region> regions = FXCollections.observableArrayList();

    private ListView<Layout> layoutList = new ListView<>(layouts);
    private ListView<Region> regionList = new ListView<>(regions);

    private ScreenSelector screenSelector = new ScreenSelector();
    private TextExtractor textExtractor = new TextExtractor();
    private TranslateService translateService = new TranslateService();

    // Gerenciamento de Overlays
    private Map<Region, OverlayWindow> overlays = new HashMap<>();
    private OverlayWindow quickOverlay; // Tradução rápida por atalho

    // Estado e Controle
    private boolean isRunning = false;
    private long selectedHwnd = 0;
    private ComboBox<WindowItem> comboWindows = new ComboBox<>();
    private Button btnPlayPause;

    private final ExecutorService workerPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    private ScheduledExecutorService executor;

    public LayoutManagerView() {
        setupUI();
        loadData();
        initGlobalHotkeys();
        startLoop();
    }

    private void setupUI() {
        setPadding(new Insets(10));

        // --- PAINEL SUPERIOR (Controles de Janela e Play) ---
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(0, 0, 10, 0));

        comboWindows.setPromptText("Selecione a janela alvo...");
        comboWindows.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboWindows, javafx.scene.layout.Priority.ALWAYS);

        Button btnRefresh = new Button("🔄");
        btnRefresh.setOnAction(e -> refreshWindowList());

        btnPlayPause = new Button("▶ Iniciar");
        btnPlayPause.setMinWidth(100);
        btnPlayPause.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPlayPause.setOnAction(e -> toggleTranslation());

        comboWindows.setOnAction(e -> {
            WindowItem selected = comboWindows.getSelectionModel().getSelectedItem();
            if (selected != null) selectedHwnd = selected.hwnd;
        });

        topPanel.getChildren().addAll(new Label("Alvo:"), comboWindows, btnRefresh, btnPlayPause);
        setTop(topPanel);

        // --- PAINEL LATERAL (Layouts) ---
        VBox leftBox = new VBox(5);
        leftBox.setMinWidth(200);
        leftBox.getChildren().addAll(new Label("Layouts:"), layoutList);

        Button btnAddLayout = new Button("Novo Layout");
        Button btnRemoveLayout = new Button("Remover Layout");
        btnAddLayout.setMaxWidth(Double.MAX_VALUE);
        btnRemoveLayout.setMaxWidth(Double.MAX_VALUE);

        btnAddLayout.setOnAction(e -> addLayout());
        btnRemoveLayout.setOnAction(e -> removeLayout());

        leftBox.getChildren().addAll(btnAddLayout, btnRemoveLayout);
        setLeft(leftBox);

        // --- PAINEL CENTRAL (Regiões) ---
        VBox centerBox = new VBox(5);
        centerBox.getChildren().addAll(new Label("Regiões (OCR):"), regionList);

        Button btnAddRegion = new Button("➕ Adicionar Área de Captura");
        Button btnRemoveRegion = new Button("➖ Remover Área");
        btnAddRegion.setMaxWidth(Double.MAX_VALUE);
        btnRemoveRegion.setMaxWidth(Double.MAX_VALUE);

        btnAddRegion.setOnAction(e -> addRegionToSelectedLayout());
        btnRemoveRegion.setOnAction(e -> removeRegion());

        centerBox.getChildren().addAll(btnAddRegion, btnRemoveRegion);
        setCenter(centerBox);

        // Listeners
        layoutList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                regions.setAll(newVal.getRegions());
                clearOverlays();
            }
        });

        refreshWindowList();
    }

    private void toggleTranslation() {
        isRunning = !isRunning;
        if (isRunning) {
            btnPlayPause.setText("⏸ Pausar");
            btnPlayPause.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            btnPlayPause.setText("▶ Iniciar");
            btnPlayPause.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
            clearOverlays();
        }
    }

    private void refreshWindowList() {
        ObservableList<WindowItem> windowList = FXCollections.observableArrayList();
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
                String title = new String(windowText).trim();
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
            if (!isRunning || selectedHwnd == 0 || regions.isEmpty()) return;

            for (Region region : regions) {
                processSingleRegion(region, false);
            }
        }, 0, 1500, TimeUnit.MILLISECONDS);
    }

    private void processSingleRegion(Region region, boolean isQuickAction) {
        workerPool.submit(() -> {
            try {
                isRunning = false;
                // ScreenCapture.captureWindowRegion deve ser o método que usa PrintWindow
                BufferedImage img = ScreenCapture.captureWindowRegion(selectedHwnd, region);
                String text = textExtractor.extract(img).trim();

                if (!text.isEmpty()) {
                    String translated = translateService.translate(text, Language.ENGLISH, Language.PORTUGUESE);

                    Platform.runLater(() -> {
                        if (isQuickAction) {
                            if (quickOverlay != null) quickOverlay.hideOverlay();
                            quickOverlay = new OverlayWindow(region);
                            quickOverlay.updateText(translated);
                            quickOverlay.showOverlay();
                        } else {
                            OverlayWindow overlay = overlays.computeIfAbsent(region, r -> new OverlayWindow(r));
                            overlay.updateText(translated);
                            overlay.showOverlay();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initGlobalHotkeys() {
        try {
            // Desativa os logs chatos do JNativeHook
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("Erro ao ativar atalhos globais.");
        }
    }

    // --- ATALHOS GLOBAIS ---

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Constantes para os atalhos
        final int QUICK_TRANSLATE_KEY = NativeKeyEvent.VC_1;
        final int CLOSE_OVERLAY_KEY = NativeKeyEvent.VC_2;
        final int TOGLE_TRANSLATE_STATE = NativeKeyEvent.VC_3;

        // Modificadores padrão (CTRL + SHIFT)
        final int REQUIRED_MODIFIERS = NativeKeyEvent.SHIFT_L_MASK;

        // Atalho: CTRL + SHIFT + S (Selecionar e Traduzir agora)
        boolean modifiers = (e.getModifiers() & REQUIRED_MODIFIERS) == REQUIRED_MODIFIERS;

        // Atalho para selecionar e traduzir
        if (modifiers && e.getKeyCode() == QUICK_TRANSLATE_KEY) {
            Platform.runLater(() -> {
                screenSelector.startSelection(region -> {
                    processSingleRegion(region, true);
                });
            });
        }

        // Atalho para fechar tradução rápida
        if (modifiers && e.getKeyCode() == CLOSE_OVERLAY_KEY) {
            Platform.runLater(() -> {
                if (quickOverlay != null) {
                    quickOverlay.hideOverlay();
                    quickOverlay = null;
                }
            });
        }
        // Atalho para iniciar e pausar traducoes continuas
        if (modifiers && e.getKeyCode() == TOGLE_TRANSLATE_STATE) {
            Platform.runLater(this::toggleTranslation);
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

    // --- MÉTODOS DE DADOS ---

    private void clearOverlays() {
        overlays.values().forEach(OverlayWindow::hideOverlay);
        overlays.clear();
        if (quickOverlay != null) quickOverlay.hideOverlay();
    }

    private void loadData() {
        layouts.setAll(LayoutStorage.load());
    }

    private void addLayout() {
        TextInputDialog dialog = new TextInputDialog("Novo Layout");
        dialog.setTitle("Adicionar Layout");
        dialog.setHeaderText("Digite o nome do layout:");
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

    // Helper interno
    private static class WindowItem {
        String title;
        long hwnd;
        WindowItem(String title, long hwnd) { this.title = title; this.hwnd = hwnd; }
        @Override public String toString() { return title; }
    }
}