package com.skylong.modules.ui;

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

    public Setting<Boolean> mouseMove = new Setting<>("move", "mouse_move", config.get("mouse_move", true));
    public ListSetting<String> image = new ListSetting<>(
        "image",
        "image",
        config.get("image", "none"),
        new ArrayList<>(images.keySet())
    );

    public Gui() {
        super("click gui", "click_gui", "ui");
    }

    public Map<String, String> getImages() {
        return images;
    }

    @Override
    public void setKeybind(int code) {
        if (code != -1) {
            config.set("keybind", code);
            keybindCode = code;
        }
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
