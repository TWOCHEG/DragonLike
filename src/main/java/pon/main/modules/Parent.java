package pon.main.modules;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.network.packet.Packet;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.Main.Categories;
import pon.main.events.impl.EventSync;
import pon.main.managers.Managers;
import pon.main.managers.main.ConfigManager;
import pon.main.events.impl.OnChangeConfig;
import pon.main.modules.client.Rotations;
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

    protected float rotationYaw = -999, rotationPitch = -999;

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

    @EventHandler
    public void onSync(EventSync e) {
        if (rotationYaw != -999) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
            rotationYaw = -999;
        }
    }

    public void rotate(float yaw, float pitch) {
        rotationYaw = yaw;
        rotationPitch = pitch;
        Managers.MODULE_MANAGER.getModule(Rotations.class).fixRotation = rotationYaw;
    }
    public void rotate(float[] angles) {
        rotate(angles[0], angles[1]);
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

    public void setEnable(boolean value, Notify.NotifyData notify) {
        CONFIG.set(ConfigManager.enableKeyName, value);
        enable = value;

        if (notify != null) {
            notify(notify);
        }

        if (getEnable()) {
            onEnable();
        } else {
            onDisable();
        }
    }
    public void setEnable(boolean value) {
        setEnable(value, new Notify.NotifyData(
            getName() + (value ? " enable" : " disable"),
            Notify.NotifyType.Module
        ));
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
        Notify notify = Managers.MODULE_MANAGER.getModule(Notify.class);
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
    protected void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(packet);
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