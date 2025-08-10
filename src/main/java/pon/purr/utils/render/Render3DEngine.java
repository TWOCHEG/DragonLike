package pon.purr.utils.render;

import static pon.purr.modules.Parent.mc;

public class Render3DEngine {
    public static float getTickDelta() {
        return mc.getRenderTickCounter().getDynamicDeltaTicks();
    }
}
