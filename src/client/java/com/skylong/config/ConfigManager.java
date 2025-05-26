package com.skylong.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping() // сохраняем русские символы
            .setPrettyPrinting()
            .create();

    private static ConfigManager INSTANCE = new ConfigManager();

    private final Path configDir = Path.of(System.getProperty("user.home"), ".skl");
    private final String moduleName;
    private Map<String, Map<String, Object>> config = new HashMap<>();
    private final Map<String, Object> moduleDefaults;

    private ConfigManager() {
        this(null);
    }

    public ConfigManager(String moduleName) {
        this.moduleName = moduleName;
        // загружаем стандартные значения из ресурсов
        Map<String, Object> allDefaults = DefaultSettings.get();
        if (moduleName != null) {
            this.moduleDefaults = (Map<String, Object>) allDefaults.getOrDefault(moduleName, new HashMap<>());
        } else {
            this.moduleDefaults = new HashMap<>();
        }
        loadConfig();
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public static ConfigManager getInstance(String moduleName) {
        INSTANCE = new ConfigManager(moduleName);
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        loadConfig();

        Map<String, Object> values = (Map<String, Object>) ((moduleName == null)
                ? config
                : config.getOrDefault(moduleName, new HashMap<>()));
        Object value = values.getOrDefault(key, moduleDefaults.getOrDefault(key, false));

        if (value instanceof Double) {
            Number def = (Number) moduleDefaults.get(key);
            Double dValue = (Double) value;

            if (def instanceof Integer) {
                return (T) Integer.valueOf(dValue.intValue());
            }
            if (def instanceof Long) {
                long roundedLong = Math.round(dValue);
                return (T) Long.valueOf(roundedLong);
            }
            double rounded = Math.round(dValue * 10d) / 10d;
            return (T) Double.valueOf(rounded);
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public void set(String key, Object value) {
        Map values = (moduleName == null)
                ? config
                : config.computeIfAbsent(moduleName, k -> new HashMap<>());

        Object def = moduleDefaults.get(key);
        if (def instanceof Integer && value instanceof Number) {
            values.put(key, ((Number) value).intValue());
        } else if (def instanceof Long && value instanceof Number) {
            values.put(key, ((Number) value).longValue());
        } else if (def instanceof Float && value instanceof Number) {
            values.put(key, ((Number) value).floatValue());
        } else {
            values.put(key, value);
        }
        saveConfig();
    }

    private void loadConfig() {
        try {
            if (Files.notExists(configDir)) {
                Files.createDirectories(configDir);
            }
            Path activeFile = getActiveConfigFile();
            if (Files.exists(activeFile)) {
                String json = Files.readString(activeFile, StandardCharsets.UTF_8);
                if (!json.isBlank()) {
                    Map<String, Map<String, Object>> loaded = GSON.fromJson(
                            json,
                            new TypeToken<Map<String, Map<String, Object>>>() {}.getType()
                    );
                    config = (loaded != null) ? loaded : new HashMap<>();
                } else {
                    config = new HashMap<>();
                }
            } else {
                config = new HashMap<>();
                saveConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
            config = new HashMap<>();
        }
    }

    private void saveConfig() {
        try {
            if (Files.notExists(configDir)) {
                Files.createDirectories(configDir);
            }
            Path activeFile = getActiveConfigFile();
            Files.writeString(activeFile, GSON.toJson(config), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ищет все JSON-файлы в папке и возвращает файл, у которого ключ "current" == true.
     * Если такой не найден, возвращает первый по алфавиту.
     */
    private Path getActiveConfigFile() throws IOException {
        List<Path> list = Files.list(configDir)
                .filter(p -> p.toString().endsWith(".json"))
                .sorted()
                .collect(Collectors.toList());
        for (Path p : list) {
            try {
                String content = Files.readString(p, StandardCharsets.UTF_8);
                if (!content.isBlank()) {
                    Map<?, ?> tree = GSON.fromJson(content, new TypeToken<Map<?, ?>>() {}.getType());
                    if (Boolean.TRUE.equals(tree.get("current"))) {
                        return p;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        // если нет ни одного, создаем или возвращаем первый
        if (list.isEmpty()) {
            Path defaultFile = configDir.resolve("config.json");
            return defaultFile;
        }
        return list.get(0);
    }
}