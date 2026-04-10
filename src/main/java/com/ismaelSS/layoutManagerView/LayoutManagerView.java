package com.ismaelSS.layoutManagerView;

import com.ismaelSS.HotkeyManager;
import com.ismaelSS.ScreenCapture;
import com.ismaelSS.ScreenSelector;
import com.ismaelSS.TextExtractor;
import com.ismaelSS.TranslateService;
import com.ismaelSS.layouts.Layout;
import com.ismaelSS.layouts.Region;
import com.ismaelSS.storage.LayoutStorage;
import com.ismaelSS.translate.LanguageExtended;
import com.ismaelSS.translate.LanguageValidator;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.Collections;

public class LayoutManagerView extends TabPane implements HotkeyManager.HotkeyCallbacks {

    // --- Dados e Listas ---
    private ObservableList<Layout> layouts = FXCollections.observableArrayList();
    private ObservableList<Region> regions = FXCollections.observableArrayList();
    private Map<Region, OverlayWindow> overlays = new HashMap<>();
    private OverlayWindow quickOverlay;

    // --- Componentes de UI ---
    private ListView<Layout> layoutList = new ListView<>(layouts);
    private ListView<Region> regionList = new ListView<>(regions);
    private ComboBox<WindowItem> comboWindows = new ComboBox<>();
    private Button btnPlayPause;

    // --- Configurações de Tradução ---
    private LanguageExtended sourceLang = LanguageExtended.ENGLISH;
    private LanguageExtended targetLang = LanguageExtended.PORTUGUESE_BRAZIL;

    // --- Serviços e Motores ---
    private ScreenSelector screenSelector = new ScreenSelector();
    private TextExtractor textExtractor = new TextExtractor();
    private TranslateService translateService = new TranslateService();
    private HotkeyManager hotkeyManager;

    private boolean isRunning = false;
    private long selectedHwnd = 0;

    private final Object captureLock = new Object();

    private final ExecutorService workerPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );
    private ScheduledExecutorService executor;

    public LayoutManagerView() {
        // Inicializa o gerenciador de teclas (thread separada)
        this.hotkeyManager = new HotkeyManager(this);

        setupTabs();
        loadData();
        startLoop();
    }

    private void setupTabs() {
        // Aba 1: Painel de Controle
        Tab tabControl = new Tab("Painel de Controle", createMainPanel());
        tabControl.setClosable(false);

        // Aba 2: Configurações
        Tab tabSettings = new Tab("Configurações", createSettingsPanel());
        tabSettings.setClosable(false);

        this.getTabs().addAll(tabControl, tabSettings);
    }

    private BorderPane createMainPanel() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(15));

        // TOPO: Seleção de Janela e Play/Pause
        HBox topBox = new HBox(10);
        topBox.setPadding(new Insets(0, 0, 15, 0));

        comboWindows.setPromptText("Selecione a janela alvo...");
        HBox.setHgrow(comboWindows, Priority.ALWAYS);
        comboWindows.setMaxWidth(Double.MAX_VALUE);

        Button btnRefresh = new Button("🔄");
        btnRefresh.setOnAction(e -> refreshWindowList());

        btnPlayPause = new Button("▶ Iniciar Tradução");
        btnPlayPause.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPlayPause.setOnAction(e -> toggleTranslation());

        comboWindows.setOnAction(e -> {
            WindowItem selected = comboWindows.getSelectionModel().getSelectedItem();
            if (selected != null) selectedHwnd = selected.hwnd;
        });

        topBox.getChildren().addAll(new Label("Alvo:"), comboWindows, btnRefresh, btnPlayPause);
        pane.setTop(topBox);

        // CENTRO: Layouts e Regiões
        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(10);
        centerGrid.setVgap(5);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(60);
        centerGrid.getColumnConstraints().addAll(col1, col2);

        centerGrid.add(new Label("Layouts:"), 0, 0);
        centerGrid.add(layoutList, 0, 1);
        centerGrid.add(new Label("Regiões OCR:"), 1, 0);
        centerGrid.add(regionList, 1, 1);

        VBox layoutButtons = new VBox(5, new Button("Novo Layout"), new Button("Remover Layout"));
        layoutButtons.getChildren().forEach(n -> ((Button)n).setMaxWidth(Double.MAX_VALUE));
        ((Button)layoutButtons.getChildren().get(0)).setOnAction(e -> addLayout());
        ((Button)layoutButtons.getChildren().get(1)).setOnAction(e -> removeLayout());
        centerGrid.add(layoutButtons, 0, 2);

        VBox regionButtons = new VBox(5, new Button("➕ Adicionar Área"), new Button("➖ Remover Área"));
        regionButtons.getChildren().forEach(n -> ((Button)n).setMaxWidth(Double.MAX_VALUE));
        ((Button)regionButtons.getChildren().get(0)).setOnAction(e -> addRegion());
        ((Button)regionButtons.getChildren().get(1)).setOnAction(e -> removeRegion());
        centerGrid.add(regionButtons, 1, 2);

        pane.setCenter(centerGrid);

        // Listener para atualizar regiões ao selecionar layout
        layoutList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            clearOverlays();
            overlays.clear();
            if (newVal != null) {
                regions.setAll(newVal.getRegions());
            } else {
                regions.clear();
            }
        });

        refreshWindowList();
        return pane;
    }

    private VBox createSettingsPanel() {
        VBox settings = new VBox(15);
        settings.setPadding(new Insets(20));

        Label title = new Label("Configurações de Idioma");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        ComboBox<LanguageExtended> cbSource = new ComboBox<>(FXCollections.observableArrayList(LanguageExtended.values()));
        cbSource.setValue(sourceLang);

        ComboBox<LanguageExtended> cbTarget = new ComboBox<>(FXCollections.observableArrayList(LanguageExtended.values()));
        cbTarget.setValue(targetLang);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        errorLabel.setVisible(false);

        Runnable validateLanguages = () -> {
            sourceLang = cbSource.getValue();
            targetLang = cbTarget.getValue();
            if (!LanguageValidator.isTargetSupported(sourceLang, targetLang)) {
                errorLabel.setText("Não é possível traduzir para " + targetLang + " a partir de " + sourceLang);
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        };

        cbSource.setOnAction(e -> validateLanguages.run());
        cbTarget.setOnAction(e -> validateLanguages.run());

        grid.add(new Label("Idioma de Origem (OCR):"), 0, 0);
        grid.add(cbSource, 1, 0);
        grid.add(new Label("Idioma de Destino:"), 0, 1);
        grid.add(cbTarget, 1, 1);

        Separator sep = new Separator();

        Label help = new Label("Atalhos Globais:\n" +
                "• SHIFT + 1: Selecionar área e traduzir instantaneamente\n" +
                "• SHIFT + 2: Fechar tradução instantânea\n" +
                "• SHIFT + 3: parar/iniciar tradução de layout selecionado");
        help.setStyle("-fx-text-fill: #7f8c8d;");

        settings.getChildren().addAll(title, grid, errorLabel, sep, help);
        return settings;
    }

    // --- Implementação dos Callbacks do HotkeyManager ---
    @Override
    public void onQuickCaptureRequested() {
        Platform.runLater(() -> {
            screenSelector.startSelection(region -> {
                processSingleRegion(region, true);
            });
        });
    }

    @Override
    public void onClearOverlaysRequested() {
        Platform.runLater(this::clearOverlays);
    }

    // --- Lógica de Tradução e OCR ---
    private void startLoop() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (!isRunning) {
                return;
            }
            if (selectedHwnd == 0 || regions.isEmpty()) return;

            for (Region region : regions) {
                processSingleRegion(region, false);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void processSingleRegion(Region region, boolean isQuickAction) {
        if (!isRunning && !isQuickAction) return;
        
        workerPool.submit(() -> {
            try {
                String text;
                synchronized (captureLock) {
                    BufferedImage img = ScreenCapture.captureWindowRegion(selectedHwnd, region);
                    text = textExtractor.extract(img).trim();
                }

                if (!text.isEmpty()) {
                    String translated = translateService.translate(text, sourceLang, targetLang);

                    Platform.runLater(() -> {
                        if (!isRunning && !isQuickAction) return;
                        
                        if (isQuickAction) {
                            if (quickOverlay != null) {
                                quickOverlay.hideOverlay();
                            }
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
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    @Override
    public void toggleTranslation() {
        isRunning = !isRunning;
        if (isRunning) {
            btnPlayPause.setText("⏸ Pausar Tradução");
            btnPlayPause.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            btnPlayPause.setText("▶ Iniciar Tradução");
            btnPlayPause.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
            clearOverlays();
            overlays.clear();
        }
    }

    private void clearOverlays() {
        overlays.values().forEach(OverlayWindow::close);
        if (quickOverlay != null) quickOverlay.close();
    }

    private void refreshWindowList() {
        ObservableList<WindowItem> windowList = FXCollections.observableArrayList();
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
                String title = new String(windowText).trim();
                if (!title.isEmpty()) {
                    windowList.add(new WindowItem(title, Pointer.nativeValue(hwnd.getPointer())));
                }
            }
            return true;
        }, null);
        comboWindows.setItems(windowList);
    }

    // --- Métodos de CRUD de Dados ---
    private void loadData() { layouts.setAll(LayoutStorage.load()); }

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
            overlays.clear();
        }
    }

    private void addRegion() {
        Layout selected = layoutList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        overlays.clear();
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
            OverlayWindow overlay = overlays.remove(selectedRegion);
            if (overlay != null) overlay.hideOverlay();
        }
    }

    private static class WindowItem {
        String title; long hwnd;
        WindowItem(String title, long hwnd) { this.title = title; this.hwnd = hwnd; }
        @Override public String toString() { return title; }
    }
}