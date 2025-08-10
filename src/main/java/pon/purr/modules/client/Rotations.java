package pon.purr.modules.client;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import pon.purr.Purr;
import pon.purr.events.impl.*;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.*;
import pon.purr.utils.math.MathUtils;
import pon.purr.utils.player.InputUtils;

import java.util.*;

public class Rotations extends Parent {
    public Rotations() {
        super("Rotations", Purr.Categories.client);
        enable = true;
    }

    private enum MoveFix {
        Off, Focused, Free
    }

    private final ListSetting<String> moveFix = new ListSetting<>("move fix", List.of("off", "focused", "free"));
    public final Setting<Boolean> clientLook = new Setting<>("ClientLook", false);

    public float fixRotation;
    private float prevYaw, prevPitch;

    public void normalRotate(float yaw, float pitch) {
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    public void onJump(EventPlayerJump e) {
        if (Float.isNaN(fixRotation) || moveFix.equals(MoveFix.Off) || mc.player.isRiding())
            return;

        if (e.isPre()) {
            prevYaw = mc.player.getYaw();
            mc.player.setYaw(fixRotation);
        } else mc.player.setYaw(prevYaw);
    }

    public void onPlayerMove(EventFixVelocity event) {
        if (moveFix.equals(MoveFix.Free)) {
            if (Float.isNaN(fixRotation) || mc.player.isRiding())
                return;
            event.setVelocity(fix(fixRotation, event.getMovementInput(), event.getSpeed()));
        }
    }

    public void modifyVelocity(EventPlayerTravel e) {
//        if (ModuleManager.aura.isEnabled() && ModuleManager.aura.target != null && ModuleManager.aura.rotationMode.not(Aura.Mode.None)
//                && ModuleManager.aura.elytraTarget.getValue() && Managers.PLAYER.ticksElytraFlying > 5) {
//            if (e.isPre()) {
//                prevYaw = mc.player.getYaw();
//                prevPitch = mc.player.getPitch();
//
//                mc.player.setYaw(fixRotation);
//                mc.player.setPitch(ModuleManager.aura.rotationPitch);
//            } else {
//                mc.player.setYaw(prevYaw);
//                mc.player.setPitch(prevPitch);
//            }
//            return;
//        }

        if (moveFix.equals(MoveFix.Focused) && !Float.isNaN(fixRotation) && !mc.player.isRiding()) {
            if (e.isPre()) {
                prevYaw = mc.player.getYaw();
                mc.player.setYaw(fixRotation);
            } else {
                mc.player.setYaw(prevYaw);
            }
        }
    }

    public void onKeyInput(EventKeyboardInput e) {
        if (moveFix.equals(MoveFix.Free)) {
            float forward = (mc.player.input.playerInput.forward() ? 1 : mc.player.input.playerInput.backward() ? -1 : 0);
            float sideways = (mc.player.input.playerInput.left() ? 1 : mc.player.input.playerInput.right() ? -1 : 0);

            Matrix4f matrix = new Matrix4f();
            matrix.rotate((float) Math.toRadians(mc.player.getYaw() - prevYaw), 0, 1, 0);
            Vec3d updatedInput = MathUtils.transformPos(matrix, sideways, 0, forward);

            forward = (float) (Math.round(updatedInput.getZ())) * (mc.player.isSneaking() ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);
            sideways = (float) (Math.round(updatedInput.getX())) * (mc.player.isSneaking() ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);

            InputUtils.setForward(forward > 0.0f);
            InputUtils.setBackward(forward < 0.0f);
            InputUtils.setLeft(sideways > 0.0f);
            InputUtils.setRight(sideways < 0.0f);

            mc.player.travel(new Vec3d(sideways, 0, forward));
        }
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

    @Override
    public void setEnable(boolean value, boolean showNotify) {
        return;
    }
}
