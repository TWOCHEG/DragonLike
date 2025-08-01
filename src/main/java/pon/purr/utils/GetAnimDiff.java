package pon.purr.utils;

import net.minecraft.client.MinecraftClient;
import pon.purr.Purr;
import pon.purr.modules.ui.Gui;

public class GetAnimDiff {
    public static float get() {
        float d = 0.01f;
        if (Purr.moduleManager == null) return d;
        Gui gui = (Gui) Purr.moduleManager.getModuleByClass(Gui.class);
        if (gui == null) return d;
        float diff = Math.max(d, (1 / Math.max(1, MinecraftClient.getInstance().getCurrentFps()))) * gui.animSpeed.getValue();
        return diff;
    }

    public static float get100X() {
        return get() * 100;
    }
}
