package com.skylong.modules.world;

import com.skylong.modules.Parent;
import com.skylong.modules.settings.*;

import java.util.*;

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
            config.get("break_delay", 20F).intValue(),
            0, 200
    );
    private BlockSelected targetBlocks = new BlockSelected(this);

    public Nuker() {
        super("nuker", "nuker", "world");

//        WorldRenderEvents.START.register(context -> {
//            if (client.player != null && enable) {
//                long window = client.getWindow().getHandle();
//                boolean isPlayerMining = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
//
//                if (!isPlayerMining) {
//                    process();
//                }
//            }
//        });
    }
}
