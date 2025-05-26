package com.skylong.modules.client;

import com.skylong.modules.*;
import com.skylong.modules.settings.*;

import java.util.*;

public class Gui extends Parent {
    public final Map<String, String> images = Map.of(
        "none", "none",
        "furry", "textures/gui/furry.png",
        "billy", "textures/gui/billy.png",
        "boykisser", "textures/gui/boykisser.png",
        "nya", "textures/gui/nya.png",
        "sonic", "textures/gui/sonic.png"
    );

    public Setting<Boolean> mouseMove = new Setting<>("move", "mouse_move", config.get("mouse_move"));
    public ListSetting<String> image = new ListSetting<>(
        "image",
        "image",
        config.get("image"),
        new ArrayList<>(images.keySet())
    );

    public Gui() {
        super("click gui", "click_gui", "interface");
    }

    public Map<String, String> getImages() {
        return images;
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
    public void setEnable(boolean value) {
        enable = false;
    }

    @Override
    public boolean getEnable() {
        return false;
    }
}
