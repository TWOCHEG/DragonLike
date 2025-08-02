package pon.purr.modules;

import pon.purr.Purr;
import pon.purr.Purr.Categories;
import pon.purr.config.ConfigManager;
import pon.purr.modules.settings.Setting;
import pon.purr.modules.ui.Notify;
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
            Notify.NotifyData n = new Notify.NotifyData(
                text + " " + name,
                Notify.NotifyType.Module,
                getNotifyLiveTime()
            );
            notify(n);
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

        return settingList;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public void notify(Notify.NotifyData n) {
        if (Purr.moduleManager != null) {
            Parent notify = Purr.moduleManager.getModuleByClass(Notify.class);
            if (notify instanceof Notify no)  {
                no.add(n);
            }
        }
    }

    public int getNotifyLiveTime() {
        if (Purr.moduleManager != null) {
            Parent notify = Purr.moduleManager.getModuleByClass(Notify.class);
            if (notify instanceof Notify no)  {
                return no.liveTimeSet.getValue();
            }
        }
        return 50;
    }

    public void resetSettings() {
        for (Setting s : getSettings()) {
            s.setValue(s.defaultValue);
        }
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }
}