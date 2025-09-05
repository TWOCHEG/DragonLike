package pon.main.utils.math;

import pon.main.Main;
import pon.main.modules.client.Gui;

public class GetAnimDiff {
    public static float get() {
        float diff = 30f / FrameRateCounter.INSTANCE.getFps();

        Gui gui = Main.MODULE_MANAGER.getModule(Gui.class);
        return diff * gui.animSpeed.getValue();
    }

    public static float get100X() {
        return get() * 100;
    }
}
