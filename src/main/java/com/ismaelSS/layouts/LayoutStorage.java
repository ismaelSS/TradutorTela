package com.ismaelSS.layouts;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LayoutStorage {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File file = new File("layouts.json");

    public static void save(List<Layout> layouts) throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, layouts);
    }

    public static List<Layout> load() throws Exception {
        if (!file.exists()) return List.of();

        return Arrays.asList(
                mapper.readValue(file, Layout[].class)
        );
    }
}
