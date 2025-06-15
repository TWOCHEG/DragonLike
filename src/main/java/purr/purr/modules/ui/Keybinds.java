package purr.purr.modules.ui;

import meteordevelopment.orbit.EventHandler;
import purr.purr.config.ConfigManager;
import org.lwjgl.glfw.GLFW;
import purr.purr.events.impl.EventTick;
import purr.purr.modules.Parent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Keybinds extends Parent {
    private static Map<Integer, Boolean> callbacks = new HashMap<>();
    private static ConfigManager config = new ConfigManager();

    public Keybinds() {
        super(null, null);
    }

    @EventHandler
    private void onTick(EventTick e) {
        if (moduleManager == null) return;
        if (client.getWindow() != null) {
            Map<String, Object> json = config.readJson();
            long handle = client.getWindow().getHandle();
            for (String key : json.keySet()) {
                if (client.currentScreen == null && !Objects.equals(key, moduleManager.getModuleByClass(Gui.class).getName())) return;
                if (key.equals(config.currentKeyName)) continue;
                int keykode = new ConfigManager(key).get("keybind", -1.0f).intValue();
                if (keykode == -1) continue;
                int state = GLFW.glfwGetKey(handle, keykode);
                boolean callback = callbacks.getOrDefault(keykode, false);
                if (state == GLFW.GLFW_PRESS && !callback) {
                    callbacks.put(keykode, true);
                    Parent module = moduleManager.getModuleById(key);
                    if (module != null) {
                        System.out.println(module);
                        module.setEnable(!module.getEnable());
                    }
                } else if (state == GLFW.GLFW_RELEASE && callback) {
                    callbacks.put(keykode, false);
                }
            }
        }
    }
}
