package com.skylong.config;

import java.util.*;

public class DefaultSettings {

    public static Map get() {
        Map<String, Object> configMap = new HashMap<>();

        Map<String, Object> clickGui = new HashMap<>();
        clickGui.put("keybind", 344);
        clickGui.put("mouse_move", true);
        clickGui.put("image", "none");
        configMap.put("click_gui", clickGui);

        Map<String, Object> killAura = new HashMap<>();
        killAura.put("keybind", -1);
        killAura.put("enable", false);
        configMap.put("kill_aura", killAura);

        Map<String, Object> example = new HashMap<>();
        killAura.put("image", "none");
        killAura.put("mouse_move", false);
        configMap.put("example", example);

        return configMap;
    }
}
