package com.ismaelSS.translate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TranslateService {

    public String translate(String q, String source, String target) {
        try {
            if (q == null || q.trim().isEmpty()) return "";

            // evita quebrar JSON
            q = q.replace("\"", "'");

            URL url = new URL("http://localhost:5000/translate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format(
                    "{\"q\":\"%s\",\"source\":\"%s\",\"target\":\"%s\",\"format\":\"q\"}",
                    q, source, target
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            return extract(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return q; // fallback
        }
    }

    private String extract(String json) {
        return json.split(":\"")[1].replace("\"}", "");
    }
}