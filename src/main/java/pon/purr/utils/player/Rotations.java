package pon.purr.utils.player;

import static pon.purr.modules.Parent.mc;

public class Rotations {
    public static void rotateCamera(float yaw, float pitch) {
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
