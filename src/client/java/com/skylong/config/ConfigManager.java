package com.skylong.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping() // сохраняем русские символы
            .setPrettyPrinting()
            .create();

    private static ConfigManager INSTANCE = new ConfigManager();

    private final Path configDir = Path.of(System.getProperty("user.home"), ".skl");
    private final Path configFile = configDir.resolve("skl.json");

    private Map<String, Map<String, Object>> config = new HashMap<>();
    private final Map<String, Object> moduleDefaults;
    private final String moduleName;

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
        // можно создать новый менеджер на лету или сохранять в map
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
        // GSON читает числа как Double
        if (value instanceof Double) {
            Number def = (Number) moduleDefaults.get(key);
            if (def instanceof Integer) {
                return (T) Integer.valueOf(((Double) value).intValue());
            }
            if (def instanceof Long) {
                return (T) Long.valueOf(((Double) value).longValue());
            }
        }
        return (T) value;
    }

    public void set(String key, Object value) {
        Map<String, Object> values = (Map<String, Object>) ((moduleName == null)
                ? config
                : config.computeIfAbsent(moduleName, k -> new HashMap<>()));

        // приводим Double к Integer по дефолту
        Object def = moduleDefaults.get(key);
        if (def instanceof Integer && value instanceof Double) {
            values.put(key, ((Double) value).intValue());
        } else {
            values.put(key, value);
        }
        saveConfig();
    }

    private void loadConfig() {
        try {
            if (Files.exists(configFile)) {
                String json = Files.readString(configFile, StandardCharsets.UTF_8);
                if (!json.isBlank()) {
                    // читаем как map модулей
                    Map<String, Map<String, Object>> loaded = GSON.fromJson(
                            json, new TypeToken<Map<String, Map<String, Object>>>() {}.getType());
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
            Files.createDirectories(configDir);
            Files.writeString(configFile, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}