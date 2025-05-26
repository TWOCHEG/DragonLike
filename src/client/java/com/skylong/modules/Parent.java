package com.skylong.modules;

import com.skylong.config.ConfigManager;
import com.skylong.modules.settings.Setting;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Parent {
    private final String name;
    private final String category;
    protected final ConfigManager config;
    protected final String id;
    protected boolean enable;
    protected int keybindCode;

    public Parent(String name, String id, String category) {
        this.name = name;
        this.config = ConfigManager.getInstance(id);
        this.id = id;
        this.enable = config.get("enable");
        this.keybindCode = config.get("keybind");
        this.category = category;
    }

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

    public void setValue(String name, Object value) {
        String key = name.replace(' ', '_');
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
}