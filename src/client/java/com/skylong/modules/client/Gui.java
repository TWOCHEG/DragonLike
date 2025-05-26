package com.skylong.modules.client;

import com.skylong.modules.*;
import com.skylong.modules.settings.*;

import java.util.*;

public class Gui extends Parent {
    public Setting<Boolean> mouseMove = new Setting<>("move", "mouse_move", config.get("mouse_move"));
    public ListSetting<String> image = new ListSetting<>(
        "image",
        "image",
        config.get("image"),
        Arrays.asList("none", "furry", "cat")
    );

    public Gui() {
        super("click gui", "click_gui", "interface");
    }

    @Override
    public void setValue(String name, Object value) {
        if (name == "keybind" && (int) value == -1) {
            return;
        }
        String key = name.replace(' ', '_');
        config.set(key, value);
    }

    @Override
    public boolean getEnable() {
        return false;
    }
}
