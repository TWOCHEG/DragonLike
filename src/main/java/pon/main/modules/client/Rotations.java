package pon.main.modules.client;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pon.main.Main;
import pon.main.events.impl.EventFixVelocity;
import pon.main.events.impl.EventKey;
import pon.main.events.impl.EventPlayerJump;
import pon.main.events.impl.EventSetVelocity;
import pon.main.modules.Parent;
import pon.main.modules.settings.Setting;

public class Rotations extends Parent {
    public Rotations() {
        super("rotations", Main.Categories.client, true);
    }

    private final Setting<MoveFix> moveFix = new Setting<>(MoveFix.off);
    public final Setting<Boolean> clientLook = new Setting<>("client look", false);

    private enum MoveFix {
        off, focused, free
    }

    public float fixRotation;
    private float prevYaw, prevPitch;

    public void onJump(EventPlayerJump e) {
        if (Float.isNaN(fixRotation) || moveFix.getValue() == MoveFix.off || mc.player.isRiding())
            return;

        if (e.isPre()) {
            prevYaw = mc.player.getYaw();
            mc.player.setYaw(fixRotation);
        } else mc.player.setYaw(prevYaw);
    }

    public void onPlayerMove(EventFixVelocity event) {
        if (moveFix.getValue() == MoveFix.free) {
            if (Float.isNaN(fixRotation) || mc.player.isRiding())
                return;
            event.setVelocity(fix(fixRotation, event.getMovementInput(), event.getSpeed()));
        }
    }

    public void modifyVelocity(EventSetVelocity e) {
//        if (ModuleManager.aura.isEnabled() && ModuleManager.aura.target != null && ModuleManager.aura.rotationMode.not(Aura.Mode.None)
//                && ModuleManager.aura.elytraTarget.getValue() && Managers.PLAYER.ticksElytraFlying > 5) {
//            mc.player.setYaw(prevYaw);
//            mc.player.setPitch(prevPitch);
//            return;
//        }

        if (moveFix.getValue() == MoveFix.focused && !Float.isNaN(fixRotation) && !mc.player.isRiding()) {
            mc.player.setYaw(prevYaw);
        }
    }

    public void onKeyInput(EventKey e) {
        if (fullNullCheck()) return;

        if (moveFix.getValue() == MoveFix.free) {
            if (Float.isNaN(fixRotation) || mc.player.isRiding())
                return;

            float mF = mc.player.forwardSpeed;
            float mS = mc.player.sidewaysSpeed;
            float delta = (mc.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
            float cos = MathHelper.cos(delta);
            float sin = MathHelper.sin(delta);
            mc.player.sidewaysSpeed = Math.round(mS * cos - mF * sin);
            mc.player.forwardSpeed = Math.round(mF * cos + mS * sin);
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
    public boolean isToggleable() {
        return false;
    }
}
