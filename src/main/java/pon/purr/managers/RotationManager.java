package pon.purr.managers;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pon.purr.events.impl.EventFixVelocity;
import pon.purr.events.impl.EventKey;
import pon.purr.events.impl.EventMove;
import pon.purr.events.impl.EventTravel;

import static pon.purr.modules.Parent.mc;

public class RotationManager {
    public float renderYaw, renderPitch = 0;
    public boolean track = true;

    public RotationManager() {
    }

    @EventHandler
    private void onRender(EventMove e) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (track) {
                client.player.renderYaw = client.player.getYaw();
                client.player.renderPitch = client.player.getPitch();
            } else {
                client.player.renderYaw = renderYaw;
                client.player.renderPitch = renderPitch;
            }
        }
    }

    public void rotateHead(float yaw, float pitch) {
        renderYaw = yaw;
        renderPitch = pitch;
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

    @EventHandler
    public void onPlayerMove(EventFixVelocity event) {
        if (mc.player.isRiding()) return;
        event.setVelocity(fix(renderYaw, event.getMovementInput(), event.getSpeed()));
    }

    @EventHandler
    public void onKeyInput(EventKey e) {
        if (mc.player.isRiding()) return;

        float mF = mc.player.forwardSpeed;
        float mS = mc.player.sidewaysSpeed;
        float delta = (mc.player.getYaw() - renderYaw) * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        mc.player.sidewaysSpeed = Math.round(mS * cos - mF * sin);
        mc.player.forwardSpeed = Math.round(mF * cos + mS * sin);
    }

    private Vec3d fix(float yaw, Vec3d movementInput, float speed) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7)
            return Vec3d.ZERO;
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE);
        float g = MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE);
        return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
    }
}