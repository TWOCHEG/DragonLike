package com.skylong.modules.ui;

import com.skylong.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;
import com.skylong.modules.*;
import com.skylong.gui.ClickGuiScreen;
import com.skylong.gui.ConfigsGui;

import java.util.HashMap;
import java.util.Map;

public class Keybinds {
    private static Map<Integer, Boolean> callbacks = new HashMap<>();
    private static ConfigManager config = new ConfigManager();

    public Keybinds(ModuleManager moduleManager) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen instanceof ClickGuiScreen || client.currentScreen instanceof ConfigsGui) return;
            if (client.getWindow() != null) {
                Map<String, Object> json = config.readJson();
                long handle = client.getWindow().getHandle();
                for (String key : json.keySet()) {
                    if (key.equals(config.currentKeyName)) continue;
                    int keykode = new ConfigManager(key).get("keybind", -1.0f).intValue();
                    if (keykode == -1) continue;
                    int state = GLFW.glfwGetKey(handle, keykode);
                    boolean callback = callbacks.getOrDefault(keykode, false);
                    if (state == GLFW.GLFW_PRESS && !callback) {
                        callbacks.put(keykode, true);
                        Parent module = moduleManager.getModuleById(key);
                        if (module != null) {
                            module.setEnable(!module.getEnable());
                        }
                    } else if (state == GLFW.GLFW_RELEASE && callback) {
                        callbacks.put(keykode, false);
                    }
                }
            }
        });
    }
}
