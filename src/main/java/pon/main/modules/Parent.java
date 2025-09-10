package pon.main.modules;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.resource.language.LanguageManager;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.Main.Categories;
import pon.main.managers.ConfigManager;
import pon.main.events.impl.OnChangeConfig;
import pon.main.modules.settings.Setting;
import pon.main.modules.client.Notify;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Parent {
    private final String NAME;
    private final Categories CATEGORY;
    protected final ConfigManager CONFIG;
    protected boolean enable;
    protected int keybindCode;
    private int defaultKeybind;
    private boolean defaultEnable;
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public List<Setting> settings = new LinkedList();

    public Parent(String name, Categories category) {
        this(name, category, false);
    }
    public Parent(String name, Categories category, int keybind) {
        this(name, category, false, keybind);
    }
    public Parent(String name, Categories category, boolean enable) {
        this(name, category, enable, -1);
    }
    public Parent(String name, Categories category, boolean enable, int keybind) {
        this.NAME = name;
        this.defaultKeybind = keybind;
        this.defaultEnable = enable;
        this.CONFIG = new ConfigManager(name);
        this.enable = getValue(ConfigManager.enableKeyName, defaultEnable);
        setKeybind(getValue(ConfigManager.keybindKeyName, defaultKeybind));
        this.CATEGORY = category;
    }

    public void onSettingUpdate(Setting setting) {}

    protected void onEnable() {}

    protected void onDisable() {}

    public String getName() {
        return NAME;
    }

    public Categories getCategory() {
        return CATEGORY;
    }

    public int getKeybind() {
        return keybindCode;
    }

    public void toggle() {
        if (isToggleable()) {
            setEnable(!getEnable());
        }
    }

    public void setEnable(boolean value, boolean showNotify) {
        if (showNotify) {
            String text = value ? "enable" : "disable";
            notify(
                new Notify.NotifyData(
                    text + " " + NAME,
                    Notify.NotifyType.Module
                )
            );
        }
        CONFIG.set(ConfigManager.enableKeyName, value);
        enable = value;

        if (getEnable()) {
            onEnable();
        } else {
            onDisable();
        }
    }
    public void setEnable(boolean value) {
        setEnable(value, true);
    }

    public boolean getEnable() {
        return enable;
    }

    public void setKeybind(int code) {
        CONFIG.set(ConfigManager.keybindKeyName, code);
        keybindCode = code;
    }

    public void setValue(String key, Object value) {
        CONFIG.set(key, value);
    }
    public <T> T getValue(String name, T defaultValue) {
        return CONFIG.get(name, defaultValue);
    }
    public <T> T getValue(String name) {
        return CONFIG.get(name);
    }

    @EventHandler
    private void onChangeConfig(OnChangeConfig e) {
        for (Setting s : getSettings()) {
            s.setValue(getValue(s.getName(), s.defaultValue));
        }
        setKeybind(getValue(ConfigManager.keybindKeyName, defaultKeybind));
        setEnable(getValue(ConfigManager.enableKeyName, defaultEnable));
    }

    public static boolean isRu() {
        MinecraftClient client = MinecraftClient.getInstance();
        LanguageManager languageManager = client.getLanguageManager();

        String languageCode = languageManager.getLanguage();

        return languageCode.startsWith("ru");
    }

    public List<Setting> getSettings() {
        if (!settings.isEmpty()) {
            return settings;
        }
        Class<?> currentSuperclass = getClass();

        while (currentSuperclass != null) {
            for (Field field : currentSuperclass.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType()))
                    continue;

                try {
                    field.setAccessible(true);
                    settings.add((Setting) field.get(this));
                } catch (IllegalAccessException ignored) {}
            }

            currentSuperclass = currentSuperclass.getSuperclass();
        }
        settings.forEach(s -> s.init(CONFIG));
        return settings;
    }

    public ConfigManager getConfig() {
        return CONFIG;
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

    protected void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (mc.getNetworkHandler() == null || mc.world == null) return;

        int sequence = Math.toIntExact(mc.world.getTime());

        mc.getNetworkHandler().sendPacket(packetCreator.predict(sequence));
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null || Main.MODULE_MANAGER == null;
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