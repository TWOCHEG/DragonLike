package com.skylong.modules.world;

import java.util.Arrays;

import com.skylong.modules.Parent;
import com.skylong.modules.settings.ListSetting;
import com.skylong.modules.settings.Setting;

public class Nuker extends Parent {
    public Setting<Boolean> avoidLava = new Setting<>(
        "avoid lava", 
        "avoid_lava", 
        config.get("avoid_lava", true)
    );
    public ListSetting<String> worldMode = new ListSetting<>(
        "break bloks",
        "break_bloks",
        "auto",
        Arrays.asList("nether", "overworld", "auto world")
    );
    public Setting<Float> breakRange = new Setting<>(
        "break range",
        "break_range", 
        config.get("break_range", 6.0f), 
        1.0f, 6.0f
    );

    public Nuker() {
        super("nuker", "nuker", "world");
    }
}
