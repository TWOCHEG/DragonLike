package com.skylong.modules.client;

import com.skylong.modules.*;
import com.skylong.modules.settings.*;

import java.util.*;

public class Gui extends Parent {
    public TextSetting header = new TextSetting("test text");
    public Setting<Float> attackRange = new Setting<>("move speed", "mouse_move_speed", 3.5f, 1.0f, 6.0f);
    public Setting<Boolean> autoAttack = new Setting<>("move", "mouse_move", true);
    public ListSetting<String> targetMode = new ListSetting<>(
            "image",
            "image",
            "none",
            Arrays.asList("none", "furry", "cat")
    );

    public Gui() {
        super("click gui", "click_gui");
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
