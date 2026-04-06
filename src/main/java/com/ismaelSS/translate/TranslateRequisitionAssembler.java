package com.ismaelSS.translate;


import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

// Assumindo que você tenha essas classes ou equivalentes no seu projeto
// Se não tiver, pode adaptar para retornar String ou usar sua lógica de parse
import lombok.NonNull;
import space.dynomake.libretranslate.type.TranslateResponse;
import space.dynomake.libretranslate.util.JsonUtil;

public final class TranslateRequisitionAssembler {
    private static String apiUrl = "http://localhost:5000/translate";
    private static String apiKey = "unknown";

    private TranslateRequisitionAssembler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String translate(@NonNull String text, @NonNull LanguageExtended source, @NonNull LanguageExtended target) {
        if (text.isBlank()) return "";

        // Seguindo a lógica do modelo: se as linguagens forem iguais, não traduz
        if (source == target) {
            return text;
        }

        return translateDetect(source.getCode(), target.getCode(), text).getTranslatedText();
    }

    public static TranslateResponse translateDetect(@NonNull String from, @NonNull String to, @NonNull String request) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("accept", "application/json");
            // Mudando para x-www-form-urlencoded para bater com o modelo
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setDoOutput(true);

            // Montando a query string igual ao modelo original
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            String payload = "q=" + URLEncoder.encode(request, "UTF-8") +
                    "&source=" + from +
                    "&target=" + to +
                    "&format=text";

            if (apiKey != null && !apiKey.equals("unknown")) {
                payload += "&api_key=" + apiKey;
            }

            writer.write(payload);
            writer.flush();
            writer.close();

            if (httpConn.getResponseCode() / 100 != 2) {
                throw new RuntimeException("Erro HTTP: " + httpConn.getResponseCode());
            } else {
                InputStream responseStream = httpConn.getInputStream();
                Scanner s = (new Scanner(responseStream)).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";

                // Usando o utilitário de Json para converter a resposta
                return (TranslateResponse) JsonUtil.from(response, TranslateResponse.class);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static void setApiUrl(String apiUrl) {
        TranslateRequisitionAssembler.apiUrl = apiUrl;
    }

    public static void setApiKey(String apiKey) {
        TranslateRequisitionAssembler.apiKey = apiKey;
    }
}