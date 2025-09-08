package pon.main.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
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
    public static final String fileExtension = ".json";
    public static final String defaultFileName = "default";

    public ConfigManager() {}
    public ConfigManager(String moduleName) {
        this.moduleName = moduleName;
    }

    public <T> T get(String key, T defaultValue) {
        try {
            Map<String, Object> config = readJson(getCurrent());
            if (moduleName != null) {
                config = (Map<String, Object>) config.get(moduleName);
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
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            saveJson(key, value);
        } catch (IOException | RuntimeException ignored) {}
    }

    public void saveJson(Path path, Map<String, Object> map) {
        try {
            String jsonOutput = GSON.toJson(map);
            Files.writeString(path, jsonOutput, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignore) {}
    }
    public void saveJson(String key, Object value) {
        try {
            Path configPath = getCurrent();
            Map<String, Object> root = readJson(configPath);
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
            saveJson(configPath, root);
        } catch (Exception ignore) {}
    }
    public Map<String, Object> readJson(Path path) {
        try {
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

    public void setCurrent(Path path) {
        try {
            Path oldConfig = getCurrent();

            if (!Files.exists(path)) {
                return;
            }

            Map<String, Object> activeData = readJson(path);
            activeData.put(currentKeyName, true);
            String updatedActiveContent = GSON.toJson(activeData);
            Files.writeString(path, updatedActiveContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            for (Path configFile : getFiles()) {
                if (configFile.equals(path)) continue;

                try {
                    String fileContent = Files.readString(configFile, StandardCharsets.UTF_8);
                    if (fileContent.isBlank()) continue;

                    Map<String, Object> fileData = GSON.fromJson(fileContent, new TypeToken<Map<String, Object>>() {}.getType());
                    if (fileData != null) {
                        fileData.put(currentKeyName, false);
                        String updatedContent = GSON.toJson(fileData);
                        Files.writeString(configFile, updatedContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                } catch (IOException | RuntimeException ignored) {}
            }
            Path active = getCurrent();
            Main.EVENT_BUS.post(new OnChangeConfig(oldConfig, active));
        } catch (IOException | RuntimeException ignored) {}
    }

    public List<Path> getFiles() {
        try {
            List<Path> list = Files.list(configDir)
                    .filter(p -> p.toString().endsWith(fileExtension) && !p.equals(FriendsManager.DATA_FILE_PATH))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
            if (list.isEmpty()) {
                list.add(configDir.resolve(defaultFileName + fileExtension));
                Map<String, Object> map = new HashMap<>();
                map.put(currentKeyName, true);
                saveJson(list.getFirst(), map);
            }
            return list;
        } catch (IOException | RuntimeException e) {
            return new ArrayList(List.of(configDir.resolve(defaultFileName)));
        }
    }

    public Path getCurrent() {
        List<Path> list = getFiles();
        for (Path p : list) {
            Map<String, Object> loaded = readJson(p);
            boolean value = (boolean) loaded.getOrDefault(currentKeyName, false);
            if (value) {
                return p;
            }
        }
        setCurrent(list.getFirst());
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

    public void deleteFile(Path path) {
        try {
            if (path != null && Files.exists(path) && path.toString().endsWith(fileExtension) && path.getParent().equals(configDir)) {
                Path current = getCurrent();
                Files.delete(path);
                if (path.equals(current)) {
                    Main.EVENT_BUS.post(new OnChangeConfig(null, getCurrent()));
                }
            }
        } catch (IOException | RuntimeException ignored) {}
    }

    public void createFile() {
        String name = String.valueOf(getFiles().size() + 1);
        int i = 0;
        while (getFiles().contains(configDir.resolve(name + (i != 0 ? i : "") + fileExtension))) {
            i++;
        }
        name += (i != 0 ? i : "") + fileExtension;

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path filePath = configDir.resolve(name);

            if (Files.exists(filePath)) {
                return;
            }

            Map<String, Object> content = new HashMap<>();
            String json = GSON.toJson(content);

            Files.writeString(filePath, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException | RuntimeException ignored) {}
    }

    public Path renameFile(Path path, String newName) {
        if (path == null || !Files.exists(path) || !path.toString().endsWith(fileExtension) || !path.getParent().equals(configDir)) {
            return null;
        }
        String name = newName.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        if (name.isEmpty()) {
            return null;
        }

        int i = 0;
        while (getFiles().contains(configDir.resolve(name + (i != 0 ? i : "") + fileExtension))) {
            i++;
        }
        name += (i != 0 ? i : "") + fileExtension;

        if (!name.endsWith(fileExtension)) {
            name += fileExtension;
        }

        Path current = getCurrent();

        Path newPath = configDir.resolve(name);

        try {
            if (Files.exists(newPath)) {
                return null;
            }
            if (current.equals(path)) {
                Main.EVENT_BUS.post(new OnChangeConfig(null, getCurrent()));
            }
            return Files.move(path, newPath);
        } catch (IOException | RuntimeException ignored) {}
        return null;
    }
}