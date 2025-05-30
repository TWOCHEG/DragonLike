package com.skylong.modules.world;

import com.skylong.modules.*;
import com.skylong.modules.settings.*;

import java.util.*;

public class Nuker extends Parent {
    public Setting<Boolean> avoidLava = new Setting<>("avoid lava", "avoid_lava", config.get("avoid_lava", true));
    public ListSetting<String> worldMode = new ListSetting<>(
        "world mode",
        "world_mode",
        "nether",
        Arrays.asList("nether", "overworld")
    );
    public Setting<Float> breakRange = new Setting<>("break range", "break_range", 6.0f, 1.0f, 6.0f);

    public Nuker() {
        super("nuker", "nuker", "world");
    }
}
