package pon.main.modules;

import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.Main.Categories;
import pon.main.config.ConfigManager;
import pon.main.events.impl.OnChangeConfig;
import pon.main.modules.settings.Setting;
import pon.main.modules.ui.Notify;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Parent {
    private final String name;
    private final Categories category;
    protected final ConfigManager config;
    protected boolean enable;
    protected int keybindCode;
    private int defaultKeybind = -1;
    private boolean defaultEnable = false;
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public List<Setting> settings = new LinkedList();

    public Parent(Categories category) {
        this.name = this.getClass().getName();
        this.config = new ConfigManager(name);
        this.enable = getValue(ConfigManager.enableKeyName, defaultEnable);
        this.keybindCode = getValue(ConfigManager.keybindKeyName, defaultKeybind);
        this.category = category;
    }
    public Parent(String name, Categories category) {
        this.name = name;
        this.config = new ConfigManager(name);
        this.enable = getValue(ConfigManager.enableKeyName, defaultEnable);
        this.keybindCode = getValue(ConfigManager.keybindKeyName, defaultKeybind);
        this.category = category;
    }
    public Parent(String name, Categories category, int keybind) {
        this.name = name;
        this.defaultKeybind = keybind;
        this.config = new ConfigManager(name);
        this.enable = getValue(ConfigManager.enableKeyName, defaultEnable);
        this.keybindCode = getValue(ConfigManager.keybindKeyName, defaultKeybind);
        this.category = category;
    }
    public Parent(String name, Categories category, boolean enable) {
        this.name = name;
        this.defaultEnable = enable;
        this.config = new ConfigManager(name);
        this.enable = getValue(ConfigManager.enableKeyName, defaultEnable);
        this.keybindCode = getValue(ConfigManager.keybindKeyName, defaultKeybind);
        this.category = category;
    }

    public void onUpdate(Setting setting) {}

    protected void onEnable() {}

    protected void onDisable() {}

    public String getName() {
        return name;
    }

    public Categories getCategory() {
        return category;
    }

    public int getKeybind() {
        return keybindCode;
    }

    public void toggle() {
        setEnable(!getEnable());
    }

    public void setEnable(boolean value, boolean showNotify) {
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
        if (showNotify) {
            String text = value ? "enable" : "disable";
            notify(
                new Notify.NotifyData(
                    text + " " + name,
                    Notify.NotifyType.Module
                )
            );
        }
        config.set(ConfigManager.enableKeyName, value);
        enable = value;
    }
    public void setEnable(boolean value) {
        setEnable(value, true);
    }

    public boolean getEnable() {
        return enable;
    }

    public void setKeybind(int code) {
        config.set(ConfigManager.keybindKeyName, code);
        keybindCode = code;
    }

    public void setValue(String key, Object value) {
        config.set(key, value);
    }

    public <T extends Object> T getValue(String name, T defaultValue) {
        Object value = config.get(name, defaultValue);
        try {
            if (value != null && defaultValue instanceof Integer) {
                value = ((Float) value).intValue();
            } else if (value instanceof Double) {
                value = ((Double) value).floatValue();
            }
        } catch (Exception ignore) {}
        return (T) value;
    }

    @EventHandler
    private void onChangeConfig(OnChangeConfig e) {
        for (Setting s : getSettings()) {
            s.setValue(getValue(s.getName(), s.defaultValue));
        }
        setKeybind(getValue(ConfigManager.keybindKeyName, defaultKeybind));
        setEnable(getValue(ConfigManager.keybindKeyName, defaultEnable));
    }

    public Object getValue(String name) {
        return getValue(name, null);
    }

    public List<Setting> getSettings() {
        if (!settings.isEmpty()) {
            return settings;
        }
        List<Setting> settingList = new LinkedList<>();
        Class<?> currentSuperclass = getClass();

        while (currentSuperclass != null) {
            for (Field field : currentSuperclass.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType()))
                    continue;

                try {
                    field.setAccessible(true);
                    settingList.add((Setting) field.get(this));
                } catch (IllegalAccessException ignored) {}
            }

            currentSuperclass = currentSuperclass.getSuperclass();
        }

        settingList.forEach(s -> s.setModule(this));

        settings = settingList;
        return settingList;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public static void notify(Notify.NotifyData n) {
        Notify notify = Main.MODULE_MANAGER.getModule(Notify.class);
        notify.add(n);
    }

    public void resetSettings() {
        for (Setting s : getSettings()) {
            s.setValue(s.defaultValue);
        }
    }

    public void onThread() {
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public boolean isKeyPressed(int button) {
        return GLFW.glfwGetKey(mc.getWindow().getHandle(), button) == GLFW.GLFW_PRESS;
    }

    public void onKey(int key) {
        if (key == getKeybind()) {
            toggle();
        }
    }

    public boolean isToggleable() {
        return true;
    }
}