package com.skylong.modules.world;

import java.util.*;

import com.skylong.modules.Parent;
import com.skylong.modules.settings.*;

public class Nuker extends Parent {
    private Setting<Boolean> avoidLava = new Setting<>(
        "avoid lava", 
        "avoid_lava", 
        config.get("avoid_lava", true)
    );
    private Setting<Float> breakRange = new Setting<>(
        "break range",
        "break_range",
        config.get("break_range", 6.0f),
        1.0f, 6.0f
    );
    private TextSetting header = new TextSetting("\"/nuker blocksList\"");
    private ListSetting<String> blockMode = new ListSetting<>(
        "blocks mode",
        "block_mode",
        config.get("block_mode", "whitelist"),
        Arrays.asList("whitelist", "blacklist")
    );
    private Setting<Integer> breakDelay = new Setting<>(
        "break delay",
        "break_delay",
        config.get("break_delay", 20),
        0, 200
    );
    private BlockSelected targetBlocks = new BlockSelected(this);

    public Nuker() {
        super("nuker", "nuker", "world");
        System.out.println(targetBlocks.getValue());
    }
}
