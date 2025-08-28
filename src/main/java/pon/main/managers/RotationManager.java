package pon.main.managers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import pon.main.events.impl.EventKey;
import pon.main.events.impl.EventMove;
import pon.main.events.impl.EventRotateCamera;
import pon.main.modules.Parent;
import pon.main.utils.math.MathUtils;
import pon.main.utils.player.MovementUtility;

import static pon.main.modules.Parent.mc;

public class RotationManager {
    public float renderYaw, renderPitch = 0;
    public boolean track = true;

    public RotationManager() {
    }

    @EventHandler
    private void onRender(EventMove e) {
        // я хуй знает почему это не работает, но выглядит правдоподобно
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

    @EventHandler
    private void onRotate(EventRotateCamera e) {
        if (track) {
            renderYaw = e.getYaw();
            renderYaw = e.getPitch();
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
    public void onInputUpdate(EventKey e) {
        if (Parent.fullNullCheck()) return;

        float forward = (mc.player.input.playerInput.forward() ? 1 : mc.player.input.playerInput.backward() ? -1 : 0);
        float sideways = (mc.player.input.playerInput.left() ? 1 : mc.player.input.playerInput.right() ? -1 : 0);

        Matrix4f matrix = new Matrix4f();
        matrix.rotate((float) Math.toRadians(mc.player.getYaw() - renderYaw), 0, 1, 0);
        Vec3d updatedInput = MathUtils.transformPos(matrix, sideways, 0, forward);

        forward = (float) Math.round(updatedInput.getZ()) * (mc.player.isSneaking() ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);
        sideways = (float) Math.round(updatedInput.getX()) * (mc.player.isSneaking() ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);

        MovementUtility.setForward(forward > 0.0f);
        MovementUtility.setBackward(forward < 0.0f);
        MovementUtility.setLeft(sideways > 0.0f);
        MovementUtility.setRight(sideways < 0.0f);

        mc.player.forwardSpeed = forward;
        mc.player.sidewaysSpeed = sideways;
    }
}