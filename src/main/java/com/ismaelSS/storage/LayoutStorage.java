package com.ismaelSS.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ismaelSS.layouts.Layout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LayoutStorage {

    private static final String FILE = "layouts.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void save(List<Layout> layouts) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE), layouts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Layout> load() {
        try {
            File file = new File(FILE);

            if (!file.exists()) return new ArrayList<>();

            return mapper.readValue(file, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}