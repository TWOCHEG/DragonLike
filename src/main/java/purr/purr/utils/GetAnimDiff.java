package purr.purr.utils;

import net.minecraft.client.MinecraftClient;
import purr.purr.config.ConfigManager;

public class GetAnimDiff {
    public static int get() {
        ConfigManager cfg = new ConfigManager("click_gui");
        int speed = cfg.get("speed", 10);
        int animDiff;
        if (cfg.get("FPS delta", true)) {
            animDiff = Math.max(1, (10 / Math.max(1, MinecraftClient.getInstance().getCurrentFps()))) * speed;
        } else {
            animDiff = speed;
        }
        return animDiff;
    }
}
