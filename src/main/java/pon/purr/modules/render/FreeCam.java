package pon.purr.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;
import pon.purr.Purr;
import pon.purr.events.impl.EventKey;
import pon.purr.events.impl.EventTick;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.math.MathUtils;
import pon.purr.utils.player.MovementUtility;
import pon.purr.utils.render.Render2DEngine;
import pon.purr.utils.render.Render3DEngine;

public class FreeCam extends Parent {
    public Setting<Float> horizontalSpeed = new Setting<>("horizontal speed", 1.0f, 0.1f, 3.0f);
    public Setting<Float> verticalSpeed = new Setting<>("vertical speed", 0.5f, 0.1f, 3.0f);

    private float freeYaw, freePitch;
    private float prevFreeYaw, prevFreePitch;

    private double freeX, freeY, freeZ;
    private double prevFreeX, prevFreeY, prevFreeZ;

    public FreeCam() {
        super("free cam", Purr.Categories.render);
        enable = false;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;

        prevFreeYaw = freeYaw;
        prevFreePitch = freePitch;

        freeYaw = mc.player.getYaw();
        freePitch = mc.player.getPitch();
    }

    @EventHandler
    public void onKeyboardTick(EventKey e) {
        if (mc.player == null) return;

        double[] motion = MovementUtility.forward(horizontalSpeed.getValue().doubleValue());

        prevFreeX = freeX;
        prevFreeY = freeY;
        prevFreeZ = freeZ;

        freeX += motion[0];
        freeZ += motion[1];

        if (mc.options.jumpKey.isPressed()) freeY += verticalSpeed.getValue().doubleValue();
        if (mc.options.sneakKey.isPressed()) freeY -= verticalSpeed.getValue().doubleValue();

        MovementUtility.setInput(mc.player.input.playerInput.forward(), mc.player.input.playerInput.backward(), mc.player.input.playerInput.left(), mc.player.input.playerInput.right(), false, false, false);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            enable = false;
            return;
        }

        mc.chunkCullingEnabled = false;

        freeYaw = prevFreeYaw = mc.player.getYaw();
        freePitch = prevFreePitch = mc.player.getPitch();

        freeX = prevFreeX = mc.player.getX();
        freeY = prevFreeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        freeZ = prevFreeZ = mc.player.getZ();
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) return;

        mc.chunkCullingEnabled = true;
    }

    public float getFreeYaw() {
        return (float) MathHelper.lerp(prevFreeYaw, freeYaw, Render3DEngine.getTickDelta());
    }

    public float getFreePitch() {
        return (float) MathHelper.lerp(prevFreePitch, freePitch, Render3DEngine.getTickDelta());
    }

    public double getFreeX() {
        return MathHelper.lerp(prevFreeX, freeX, Render3DEngine.getTickDelta());
    }

    public double getFreeY() {
        return MathHelper.lerp(prevFreeY, freeY, Render3DEngine.getTickDelta());
    }

    public double getFreeZ() {
        return MathHelper.lerp(prevFreeZ, freeZ, Render3DEngine.getTickDelta());
    }
}
