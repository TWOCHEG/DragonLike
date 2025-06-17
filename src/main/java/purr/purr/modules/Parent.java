package purr.purr.modules;

import purr.purr.Purr;
import purr.purr.config.ConfigManager;
import purr.purr.modules.settings.Setting;
import purr.purr.modules.ui.Notify;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Parent {
    private final String name;
    private final String category;
    protected final ConfigManager config;
    protected ModuleManager moduleManager = null;
    protected boolean enable;
    protected int keybindCode;
    protected MinecraftClient client = MinecraftClient.getInstance();

    public Parent(String name, String category) {
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

    public String getCategory() {
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
        if (moduleManager != null && showNotify) {
            Notify notify = (Notify) moduleManager.getModuleByClass(Notify.class);
            if (notify != null) {
                String text = value ? "enable" : "disable";
                notify.add(name + " | " + text, Notify.NotifyType.Important);
            }
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
                } catch (IllegalAccessException ignored) {}
            }

            currentSuperclass = currentSuperclass.getSuperclass();
        }

        settingList.forEach(s -> s.setModule(this));

        return settingList;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
}