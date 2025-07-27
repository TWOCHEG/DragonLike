package pon.purr.utils;

import net.minecraft.client.MinecraftClient;
import pon.purr.Purr;
import pon.purr.modules.ui.Gui;

public class GetAnimDiff {
    public static int get() {
        if (Purr.moduleManager == null) return 10;
        Gui gui = (Gui) Purr.moduleManager.getModuleByClass(Gui.class);
        if (gui == null) return 10;
        int animDiff = Math.max(1, (10 / Math.max(1, MinecraftClient.getInstance().getCurrentFps()))) * gui.animSpeed.getValue();
        return animDiff;
    }
}
