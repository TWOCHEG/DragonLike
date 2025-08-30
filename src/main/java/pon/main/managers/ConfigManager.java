package pon.main.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pon.main.Main;
import pon.main.events.impl.OnChangeConfig;
import pon.main.modules.Parent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.awt.Desktop;
import java.io.File;

public class ConfigManager implements IManager {
    public final Path configDir = Path.of(System.getProperty("user.home"), ".dl");
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    private String moduleName = null;

    public static final String currentKeyName = "current";
    public static final String keybindKeyName = "keybind";
    public static final String enableKeyName = "enable";

    public ConfigManager() {}
    public ConfigManager(String moduleName) {
        this.moduleName = moduleName;
    }

    public <T> T get(String key, T defaultValue) {
        try {
            Map<String, Object> config = readJson();
            if (moduleName != null) {
                config = (Map<String, Object>) config.getOrDefault(moduleName, new HashMap<>());
            }

            Object result = config.get(key);
            if (result == null) {
                return defaultValue;
            }
            if (result instanceof Number) {
                result = normalizeNumber(result, defaultValue);
            }
            return (T) result;
        } catch (RuntimeException ignored) {}
        return defaultValue;
    }
    public <T> T get(String key) {
        return get(key, null);
    }

    public ConfigManager forModule(Parent module) {
        return new ConfigManager(module.getName());
    }

    public void set(String key, Object value) {
        try {
            Path configPath = getCurrent();
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
        } catch (IOException | RuntimeException ignored) {}
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> readJson() {
        try {
            Path path = getCurrent();
            if (Files.exists(path) && Files.isRegularFile(path)) {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                if (!content.isBlank()) {
                    Map<String, Object> loaded = GSON.fromJson(content, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    return loaded;
                }
            }
        } catch (IOException ignored) {}
        return new HashMap<>();
    }

    public Object normalizeNumber(Object value, Object defaultValue) {
        if (value instanceof Double) {
            double d = (Double) value;
            if (defaultValue instanceof Float) {
                return (float) d;
            }
            if (defaultValue instanceof Integer) {
                return (int) d;
            }
        }
        return value;
    }

    public void setCurrent(Path activePath) {
        try {
            Path oldConfig = getCurrent();

            if (!Files.exists(activePath)) {
                return;
            }

            Map<String, Object> activeData;
            String content = Files.readString(activePath, StandardCharsets.UTF_8);
            if (!content.isBlank()) {
                activeData = GSON.fromJson(content, new TypeToken<Map<String, Object>>() {}.getType());
            } else {
                activeData = new HashMap<>();
            }
            activeData.put(currentKeyName, true);
            String updatedActiveContent = GSON.toJson(activeData);
            Files.writeString(activePath, updatedActiveContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            for (Path configFile : getFiles()) {
                if (configFile.equals(activePath)) continue;

                try {
                    String fileContent = Files.readString(configFile, StandardCharsets.UTF_8);
                    if (fileContent.isBlank()) continue;

                    Map<String, Object> fileData = GSON.fromJson(fileContent, new TypeToken<Map<String, Object>>() {}.getType());
                    if (fileData != null) {
                        fileData.put("current", false);
                        String updatedContent = GSON.toJson(fileData);
                        Files.writeString(configFile, updatedContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                } catch (IOException | RuntimeException ignored) {}
            }
            Path active = getCurrent();
            if (active != null) {
                Main.EVENT_BUS.post(new OnChangeConfig(oldConfig, active));
            }
        } catch (IOException | RuntimeException ignored) {}
    }

    public List<Path> getFiles() {
        try {
            List<Path> list = Files.list(configDir)
                .filter(p -> p.toString().endsWith(".json"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .collect(Collectors.toList());
            if (list.isEmpty()) {
                list.add(configDir.resolve("default.json"));
            }
            return list;
        } catch (IOException | RuntimeException e) {
            return new ArrayList(List.of(configDir.resolve("default.json")));
        }
    }

    public Path getCurrent() {
        List<Path> list = getFiles();
        for (Path p : list) {
            if (Files.isRegularFile(p) && p.toString().endsWith(".json")) {
                try {
                    String content = Files.readString(p, StandardCharsets.UTF_8);
                    if (!content.isBlank()) {
                        Map<String, Object> loaded = GSON.fromJson(content, new TypeToken<Map<String, Object>>() {}.getType());
                        boolean value = (boolean) loaded.getOrDefault(currentKeyName, false);
                        if (value) {
                            return p;
                        }
                    }
                } catch (IOException ignored) {}
            }
        }
        return list.getFirst();
    }

    public void openFilesDir() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            File dir = configDir.toFile();

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(dir);
                    return;
                }
            }

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", dir.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", dir.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", dir.getAbsolutePath()).start();
            }
        } catch (IOException ignored) {}
    }

    public boolean deleteCfg(Path path) {
        try {
            if (path != null && Files.exists(path) && path.toString().endsWith(".json") && path.getParent().equals(configDir)) {
                Files.delete(path);
                return true;
            }
        } catch (IOException | RuntimeException ignored) {}
        return false;
    }

    public Path createCfg() {
        String name = String.valueOf(getFiles().size() + 1);
        while (getFiles().contains(configDir.resolve(name + ".json"))) {
            name += "-c";
        }

        name += ".json";

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path filePath = configDir.resolve(name);

            if (Files.exists(filePath)) {
                return filePath;
            }

            Map<String, Object> content = new HashMap<>();
            String json = GSON.toJson(content);

            Files.writeString(filePath, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            return filePath;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Path renameCfg(Path path, String newName) {
        if (path == null || !Files.exists(path) || !path.toString().endsWith(".json") || !path.getParent().equals(configDir)) {
            return null;
        }
        String cleanedName = newName.replaceAll("[\\\\/:*?\"<>|]", "").trim();

        if (cleanedName.isEmpty()) {
            return null;
        }
        if (!cleanedName.endsWith(".json")) {
            cleanedName += ".json";
        }

        Path newPath = configDir.resolve(cleanedName);

        try {
            if (Files.exists(newPath)) {
                return null;
            }
            return Files.move(path, newPath);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
