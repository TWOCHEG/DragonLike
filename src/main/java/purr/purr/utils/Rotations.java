package purr.purr.utils;

import purr.purr.modules.Parent;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import purr.purr.modules.settings.*;
import purr.purr.events.impl.*;

import java.util.List;

public class Rotations extends Parent {
    public Rotations() {
        super("rotations", "ui");
        enable = true;
    }

    private final ListSetting<String> moveFix = new ListSetting<>("move fix", List.of("free", "off", "focused"));
    public final Setting<Boolean> clientLook = new Setting<>("client look", false);

    private enum MoveFix {
        Off, Focused, Free
    }

    public float fixRotation;
    private float prevYaw, prevPitch;

    public void onJump(EventPlayerJump e) {
        if (Float.isNaN(fixRotation) || moveFix.getValue().equals("off") || client.player.isRiding())
            return;

        if (e.isPre()) {
            prevYaw = client.player.getYaw();
            client.player.setYaw(fixRotation);
        } else client.player.setYaw(prevYaw);
    }

    public void onPlayerMove(EventFixVelocity event) {
        if (moveFix.getValue().equals("free")) {
            if (Float.isNaN(fixRotation) || client.player.isRiding())
                return;
            event.setVelocity(fix(fixRotation, event.getMovementInput(), event.getSpeed()));
        }
    }

    public void modifyVelocity(EventPlayerTravel e) {
        if (ModuleManager.aura.isEnabled() && ModuleManager.aura.target != null && ModuleManager.aura.rotationMode.not(Aura.Mode.None)
                && ModuleManager.aura.elytraTarget.getValue() && Managers.PLAYER.ticksElytraFlying > 5) {
            if (e.isPre()) {
                prevYaw = client.player.getYaw();
                prevPitch = client.player.getPitch();

                client.player.setYaw(fixRotation);
                client.player.setPitch(ModuleManager.aura.rotationPitch);
            } else {
                client.player.setYaw(prevYaw);
                client.player.setPitch(prevPitch);
            }
            return;
        }

        if (moveFix.getValue().equals("focused") && !Float.isNaN(fixRotation) && !client.player.isRiding()) {
            if (e.isPre()) {
                prevYaw = client.player.getYaw();
                client.player.setYaw(fixRotation);
            } else {
                client.player.setYaw(prevYaw);
            }
        }
    }

    public void onKeyInput(EventKeyboardInput e) {
        if (moveFix.getValue().equals("free")) {
            if (Float.isNaN(fixRotation) || client.player.isRiding())
                return;

            float mF = client.player.input.movementForward;
            float mS = client.player.input.movementSideways;
            float delta = (client.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
            float cos = MathHelper.cos(delta);
            float sin = MathHelper.sin(delta);
            client.player.input.movementSideways = Math.round(mS * cos - mF * sin);
            client.player.input.movementForward = Math.round(mF * cos + mS * sin);
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
