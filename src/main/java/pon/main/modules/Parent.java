package pon.main.modules;

import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.Main.Categories;
import pon.main.config.ConfigManager;
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
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public List<Setting> settings = new LinkedList();

    public Parent(String name, Categories category) {
        this.name = name;
        this.config = new ConfigManager(name);
        this.enable = (boolean) getValue("enable", false);
        this.keybindCode = (int) getValue("keybind", -1);
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
                    Notify.NotifyType.Module,
                    getNotifyLiveTime()
                )
            );
        }
        config.set("enable", value);
        enable = value;
    }
    public void setEnable(boolean value) {
        setEnable(value, true);
    }

    public boolean getEnable() {
        return enable;
    }

    public void setKeybind(int code) {
        config.set("keybind", code);
        keybindCode = code;
    }

    public void setValue(String key, Object value) {
        config.set(key, value);
    }

    public Object getValue(String name, Object defaultValue) {
        Object value = config.get(name, defaultValue);
        try {
            if (value != null && defaultValue instanceof Integer) {
                value = ((Float) value).intValue();
            } else if (value instanceof Double) {
                value = ((Double) value).floatValue();
            }
        } catch (Exception ignore) {}
        return value;
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
        Notify notify = Main.moduleManager.getModule(Notify.class);
        notify.add(n);
    }

    public static int getNotifyLiveTime() {
        Notify notify = Main.moduleManager.getModule(Notify.class);
        return notify.liveTimeSet.getValue();
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
}