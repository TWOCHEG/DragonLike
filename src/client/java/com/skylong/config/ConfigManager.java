package com.skylong.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    private final Path configDir = Path.of(System.getProperty("user.home"), ".skl");
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    private String moduleName;

    public ConfigManager() {
        this.moduleName = null;
    }
    public ConfigManager(String optionalParam) {
        this.moduleName = optionalParam;
    }

    public <T> T get(String key) {
        try {
            Map<String, Object> config = readJson();
            if (moduleName != null) {
                config = (Map<String, Object>) config.getOrDefault(moduleName, new HashMap<>());
            }

            Object result = config.get(key);
            if (result instanceof Number) {
                result = normalizeNumber(result);
            }
            return (T) result;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }


    public void set(String key, Object value) {
        try {
            Path configPath = getActiveConfig();
            Map<String, Object> root;

            if (Files.exists(configPath) && Files.isRegularFile(configPath)) {
                String content = Files.readString(configPath, StandardCharsets.UTF_8);
                if (!content.isBlank()) {
                    root = GSON.fromJson(content, new TypeToken<Map<String, Object>>() {
                    }.getType());
                } else {
                    root = new HashMap<>();
                }
            } else {
                root = new HashMap<>();
            }

            if (moduleName == null) {
                root.put(key, value);
            } else {
                Object moduleData = root.get(moduleName);
                Map<String, Object> moduleMap;

                if (moduleData instanceof Map) {
                    moduleMap = (Map<String, Object>) moduleData;
                } else {
                    moduleMap = new HashMap<>();
                }

                moduleMap.put(key, value);
                root.put(moduleName, moduleMap);
            }

            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            String jsonOutput = GSON.toJson(root);
            Files.writeString(configPath, jsonOutput, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException | RuntimeException e) {}
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson() throws IOException {
        Map<String, Object> defaults = DefaultSettings.get();

        Path path = getActiveConfig();
        if (Files.exists(path) && Files.isRegularFile(path)) {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (!content.isBlank()) {
                Map<String, Object> loaded = GSON.fromJson(content, new TypeToken<Map<String, Object>>() {}.getType());
                return deepMerge(defaults, loaded);
            }
        }
        return defaults;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> override) {
        for (Map.Entry<String, Object> entry : override.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map && base.get(key) instanceof Map) {
                Map<String, Object> baseMap = (Map<String, Object>) base.get(key);
                Map<String, Object> overrideMap = (Map<String, Object>) value;
                base.put(key, deepMerge(baseMap, overrideMap));
            } else {
                base.put(key, value);
            }
        }
        return base;
    }

    private Object normalizeNumber(Object value) {
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == (int) d) {
                return (int) d;
            } else {
                return (float) d;
            }
        }
        return value;
    }

    private Path getActiveConfig() {
        try {
            List<Path> list = Files.list(configDir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .collect(Collectors.toList());
            for (Path p : list) {
                try {
                    String content = Files.readString(p, StandardCharsets.UTF_8);
                    if (!content.isBlank()) {
                        Map<?, ?> tree = GSON.fromJson(content, new TypeToken<Map<?, ?>>() {
                        }.getType());
                        if (Boolean.TRUE.equals(tree.get("current"))) {
                            return p;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            if (list.isEmpty()) {
                Path defaultFile = configDir.resolve("default.json");
                return defaultFile;
            }
            return list.get(0);
        } catch (IOException | RuntimeException e) {}
        return null;
    }
}
