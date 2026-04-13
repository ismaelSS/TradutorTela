package com.ismaelSS;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LibreTranslateManager {
    
    private Process process;
    private static final int PORT = 5000;
    private static final int MAX_WAIT_SECONDS = 600;
    private static final String LIBRE_TRANSLATE_VERSION = "1.9.1";
    private static final String WINPYTHON_VERSION = "3.13.12.0dot";
    private static final String WINPYTHON_URL = "https://github.com/winpython/winpython/releases/download/17.2.20260307final/WinPython64-" + WINPYTHON_VERSION + ".zip";
    
    private final Path winpythonDir;
    private Path pythonExePath;
    private Path scriptsPath;
    
    private final AtomicBoolean modelsReady = new AtomicBoolean(false);
    private final AtomicReference<String> statusMessage = new AtomicReference<>("Aguardando...");
    private final AtomicReference<String> downloadProgress = new AtomicReference<>("");
    private final AtomicReference<Integer> downloadPercent = new AtomicReference<>(0);
    
    public interface StatusCallback {
        void onStatusUpdate(String status, boolean isError);
    }
    
    private StatusCallback statusCallback;
    
    public LibreTranslateManager() {
        this.winpythonDir = Paths.get(System.getProperty("user.home"), ".tradutorDeTela", "WinPython");
        
        Path userPython = findPythonExe();
        if (userPython != null && Files.exists(userPython)) {
            this.pythonExePath = userPython;
            this.scriptsPath = userPython.getParent().resolve("Scripts");
        } else {
            this.pythonExePath = winpythonDir.resolve("python-" + WINPYTHON_VERSION.substring(0, 5).replace(".", "")).resolve("python.exe");
            this.scriptsPath = winpythonDir.resolve("python-" + WINPYTHON_VERSION.substring(0, 5).replace(".", "")).resolve("Scripts");
        }
    }
    
    public void setStatusCallback(StatusCallback callback) {
        this.statusCallback = callback;
    }
    
    private void updateStatus(String message, boolean isError) {
        statusMessage.set(message);
        if (statusCallback != null) {
            statusCallback.onStatusUpdate(message, isError);
        }
    }
    
    public void start() throws Exception {
        if (isServerReady()) {
            updateStatus("Pronto (ja em execucao)", false);
            return;
        }
        
        if (!setupWinPython()) {
            throw new IOException("Falha ao configurar WinPython");
        }
        
        verificarInstalacao();
        iniciarServidor();
    }
    
    public void startAsync() {
        Thread.startVirtualThread(() -> {
            try {
                start();
            } catch (Exception e) {
                updateStatus("Erro: " + e.getMessage(), true);
                e.printStackTrace();
            }
        });
    }
    
    private boolean setupWinPython() throws Exception {
        Path checkPython = findPythonExe();
        if (checkPython != null && Files.exists(checkPython)) {
            pythonExePath = checkPython;
            scriptsPath = checkPython.getParent().resolve("Scripts");
            System.out.println("[Python] Usando Python do sistema: " + pythonExePath);
            return true;
        }
        
        if (Files.exists(pythonExePath)) {
            System.out.println("[WinPython] Ja instalado em: " + pythonExePath);
            return true;
        }
        
        updateStatus("Baixando WinPython...", false);
        System.out.println("[WinPython] Baixando WinPython " + WINPYTHON_VERSION + "...");
        
        Files.createDirectories(winpythonDir);
        
        Path zipPath = winpythonDir.resolve("winpython.zip");
        baixarArquivo(new URL(WINPYTHON_URL), zipPath);
        
        updateStatus("Extraindo WinPython...", false);
        System.out.println("[WinPython] Extraindo...");
        extrairZip(zipPath, winpythonDir);
        Files.deleteIfExists(zipPath);
        
        if (!Files.exists(pythonExePath)) {
            System.out.println("[WinPython] Python nao encontrado em: " + pythonExePath);
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(winpythonDir)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry) && entry.getFileName().toString().startsWith("python-")) {
                        Path altPython = entry.resolve("python.exe");
                        if (Files.exists(altPython)) {
                            System.out.println("[WinPython] Encontrado em: " + altPython);
                            return true;
                        }
                    }
                }
            }
            
            throw new IOException("Python nao encontrado apos extracao");
        }
        
        System.out.println("[WinPython] Configurado com sucesso");
        return true;
    }
    
    private void baixarArquivo(URL url, Path destino) throws Exception {
        try (InputStream in = url.openStream();
             BufferedInputStream bis = new BufferedInputStream(in);
             FileOutputStream fos = new FileOutputStream(destino.toFile())) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.connect();
            long contentLength = conn.getContentLengthLong();
            conn.disconnect();
            
            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                if (contentLength > 0) {
                    int percent = (int) ((totalBytesRead * 100) / contentLength);
                    if (percent % 10 == 0 && downloadPercent.get() != percent) {
                        downloadPercent.set(percent);
                        updateStatus("Baixando WinPython: " + percent + "%", false);
                    }
                }
            }
        }
    }
    
    private void extrairZip(Path zipPath, Path destDir) throws IOException {
        byte[] buffer = new byte[8192];
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = destDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) {
                        Files.createDirectories(newPath.getParent());
                    }
                    try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
    
    private Path findPythonExe() {
        Path userPython = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Python", "pythoncore-3.14-64", "python.exe");
        if (Files.exists(userPython)) {
            return userPython;
        }
        
        Path userPythonAlt = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Python", "bin", "python.exe");
        if (Files.exists(userPythonAlt)) {
            return userPythonAlt;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(winpythonDir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry) && entry.getFileName().toString().startsWith("python-")) {
                    Path python = entry.resolve("python.exe");
                    if (Files.exists(python)) {
                        return python;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String pathPython = System.getenv("PATH");
        if (pathPython != null) {
            for (String dir : pathPython.split(File.pathSeparator)) {
                Path pythonPath = Paths.get(dir, "python.exe");
                if (Files.exists(pythonPath)) {
                    return pythonPath;
                }
            }
        }
        
        return null;
    }
    
    private boolean isServerReady() {
        try {
            URI uri = URI.create("http://localhost:" + PORT + "/languages");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            String body = response.body();
            boolean hasLanguages = response.statusCode() == 200 && body.contains("\"code\"");
            
            if (hasLanguages && !modelsReady.get()) {
                int langCount = body.split("\"code\"").length - 1;
                System.out.println("[LibreTranslate] Modelos carregados: " + langCount + " idiomas");
                modelsReady.set(true);
            }
            
            return hasLanguages;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Path getPythonExe() {
        Path python = findPythonExe();
        if (python != null) {
            return python;
        }
        return pythonExePath;
    }
    
    private Path getScriptsPath() {
        Path pythonDir = getPythonExe().getParent();
        return pythonDir.resolve("Scripts");
    }
    
    private void verificarInstalacao() throws IOException, InterruptedException {
        updateStatus("Verificando LibreTranslate...", false);
        
        Path pythonExe = getPythonExe();
        Path scriptsDir = getScriptsPath();
        
        ProcessBuilder checkPb = new ProcessBuilder(
            pythonExe.toString(), "-m", "pip", "show", "libretranslate"
        );
        checkPb.environment().put("PYTHONPATH", scriptsDir.toString());
        checkPb.redirectErrorStream(true);
        Process checkProcess = checkPb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        
        boolean installed = checkProcess.waitFor(30, TimeUnit.SECONDS) && 
                          checkProcess.exitValue() == 0 && 
                          output.toString().contains("Name: libretranslate");
        
        if (!installed) {
            updateStatus("Instalando LibreTranslate...", false);
            System.out.println("[LibreTranslate] Instalando...");
            
            ProcessBuilder installPb = new ProcessBuilder(
                pythonExe.toString(), "-m", "pip", "install",
                "--upgrade",
                "libretranslate==" + LIBRE_TRANSLATE_VERSION
            );
            installPb.environment().put("PYTHONPATH", scriptsDir.toString());
            installPb.inheritIO();
            Process installProcess = installPb.start();
            
            if (!installProcess.waitFor(600, TimeUnit.SECONDS)) {
                installProcess.destroyForcibly();
                throw new IOException("Timeout ao instalar o LibreTranslate");
            }
            
            if (installProcess.exitValue() != 0) {
                throw new IOException("Falha ao instalar o LibreTranslate");
            }
            
            updateStatus("LibreTranslate instalado", false);
        }
    }
    
    private void iniciarServidor(String pythonExe) throws IOException, InterruptedException {
        updateStatus("Iniciando servidor...", false);
        System.out.println("[LibreTranslate] Iniciando na porta " + PORT + "...");
        
        Path libreTranslateDir = Paths.get("C:/programacao/java/tradutorTelafull/libretranslate").toAbsolutePath();
        
        ProcessBuilder pb;
        if (Files.exists(libreTranslateDir.resolve("main.py"))) {
            pb = new ProcessBuilder(
                pythonExe, libreTranslateDir.resolve("main.py").toString(),
                "--port", String.valueOf(PORT),
                "--host", "127.0.0.1",
                "--threads", "1"
            );
        } else {
            pb = new ProcessBuilder(
                pythonExe, "-m", "libretranslate",
                "--port", String.valueOf(PORT),
                "--host", "127.0.0.1",
                "--threads", "1"
            );
        }
        
        pb.redirectErrorStream(true);
        
        process = pb.start();
        
        StringBuilder progressLog = new StringBuilder();
        
        Thread outputReader = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[LibreTranslate] " + line);
                    progressLog.append(line).append("\n");
                    downloadProgress.set(progressLog.toString());
                    
                    if (line.contains("Downloading") || line.contains("downloading")) {
                        String shortLine = line.length() > 60 ? line.substring(0, 60) + "..." : line;
                        updateStatus("Baixando modelo: " + shortLine, false);
                    } else if (line.contains("Installing") || line.contains("installing")) {
                        String shortLine = line.length() > 60 ? line.substring(0, 60) + "..." : line;
                        updateStatus("Instalando modelo: " + shortLine, false);
                    } else if (line.contains("ERROR") || line.contains("Error")) {
                        updateStatus("Erro no servidor", true);
                    }
                }
            } catch (Exception e) {
                System.out.println("[LibreTranslate] Leitor encerrado");
            }
        });
        outputReader.setDaemon(true);
        outputReader.start();
        
        esperarServidorPronto();
    }
    
    private void iniciarServidor() throws IOException, InterruptedException {
        iniciarServidor(getPythonExe().toString());
    }
    
    private void esperarServidorPronto() throws InterruptedException, IOException {
        System.out.println("[LibreTranslate] Aguardando servidor ficar pronto...");
        
        for (int i = 0; i < MAX_WAIT_SECONDS; i++) {
            if (isServerReady()) {
                System.out.println("[LibreTranslate] Servidor pronto apos " + (i + 1) + "s");
                updateStatus("Pronto", false);
                return;
            }
            
            if (i % 10 == 0) {
                String progress = downloadProgress.get();
                if (!progress.isEmpty()) {
                    String lastLine = progress.lines().reduce((first, second) -> second).orElse("");
                    if (lastLine.length() > 50) {
                        lastLine = lastLine.substring(0, 50) + "...";
                    }
                    if (!lastLine.isEmpty()) {
                        updateStatus("Iniciando: " + lastLine, false);
                    }
                } else {
                    updateStatus("Aguardando... (" + (i + 1) + "s)", false);
                }
            }
            
            TimeUnit.SECONDS.sleep(1);
        }
        
        throw new IOException("Timeout ao iniciar o LibreTranslate apos " + MAX_WAIT_SECONDS + " segundos");
    }
    
    public void stop() {
        if (process != null && process.isAlive()) {
            System.out.println("[LibreTranslate] Encerrando...");
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
            }
            System.out.println("[LibreTranslate] Encerrado");
        }
        modelsReady.set(false);
    }
    
    public boolean isReady() {
        return modelsReady.get();
    }
    
    public String getStatus() {
        return statusMessage.get();
    }
}
