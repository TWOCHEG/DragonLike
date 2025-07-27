package pon.purr.modules.ui;

import meteordevelopment.orbit.EventHandler;
import pon.purr.config.ConfigManager;
import pon.purr.events.impl.EventKeyPress;
import pon.purr.modules.Parent;

import java.util.HashMap;
import java.util.Map;

public class Keybinds extends Parent {
    private static Map<Integer, Boolean> callbacks = new HashMap<>();
    private static ConfigManager config = new ConfigManager();

    public Keybinds() {
        super(null, null);
    }

    @EventHandler
    private void keyPress(EventKeyPress e) {
        if (moduleManager == null) return;
        Parent module = moduleManager.getModuleByKey(e.getKey());
        if (module == null) return;

        module.toggle();
    }
}
