package com.purr.modules;

import com.purr.config.ConfigManager;
import com.purr.modules.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Parent {
    private final String name;
    private final String category;
    protected final ConfigManager config;
    protected ModuleManager moduleManager = null;
    protected final String id;
    protected boolean enable;
    protected int keybindCode;
    protected boolean visible = true;
    protected MinecraftClient client = MinecraftClient.getInstance();

    public Parent(String name, String id, String category) {
        this.name = name;
        this.config = new ConfigManager(id);
        this.id = id;
        this.enable = config.get("enable", false);
        this.keybindCode = config.get("keybind", -1.0f).intValue();
        this.category = category;
    }

    protected void onUpdate() {}

    protected void onEnable() {}

    protected void onDisable() {}

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public int getKeybind() {
        return keybindCode;
    }

    public void setEnable(boolean value) {
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
        config.set("enable", value);
        enable = value;
    }

    public boolean getEnable() {
        return enable;
    }

    public void setKeybind(int code) {
        config.set("keybind", code);
        keybindCode = code;
    }

    public void setValue(String key, Object value) {
        onUpdate();

        config.set(key, value);
    }

    public Object getValue(String name) {
        String key = name.replace(' ', '_');
        return config.get(key);
    }

    public List<Setting<?>> getSettings() {
        ArrayList<Setting<?>> settingList = new ArrayList<>();
        Class<?> currentSuperclass = getClass();

        while (currentSuperclass != null) {
            for (Field field : currentSuperclass.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType()))
                    continue;

                try {
                    field.setAccessible(true);
                    settingList.add((Setting<?>) field.get(this));
                } catch (IllegalAccessException error) {
                    System.out.println(error.getMessage());
                }
            }

            currentSuperclass = currentSuperclass.getSuperclass();
        }

        settingList.forEach(s -> s.setModule(this));

        return settingList;
    }

    public boolean getVisible() {
        return visible;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
}