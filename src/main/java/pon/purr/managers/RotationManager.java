package pon.purr.managers;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;

import static pon.purr.modules.Parent.mc;

public class RotationManager {
    public float lastYaw, lastPitch;

    public RotationManager() {

    }

    public void rotateHead(float yaw, float pitch) {
        setLastRotations();

        mc.player.renderYaw = yaw;
        mc.player.renderPitch = pitch;
    }

    public void rotate(float yaw, float pitch, boolean silent) {
        rotateHead(yaw, pitch);

        if (silent) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                yaw, pitch,
                mc.player.isOnGround(),
                mc.player.horizontalCollision
            ));
        } else {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
    }
    public void rotate(float yaw, float pitch) {
        rotate(yaw, pitch, false);
    }

    public void setLastRotations() {
        lastYaw = mc.player.getYaw();
        lastPitch = mc.player.getPitch();
    }
}
