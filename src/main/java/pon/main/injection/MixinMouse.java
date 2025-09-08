package pon.main.injection;

import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.main.Main;
import pon.main.events.impl.EventMouseKey;
import pon.main.events.impl.EventMouseMove;
import pon.main.events.impl.EventMouseScroll;
import pon.main.utils.math.MouseUtils;

@Mixin(Mouse.class)
public class MixinMouse {
    @Unique
    private static double lastX = 0, lastY = 0;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
            double[] pos = MouseUtils.getPos();
            Main.EVENT_BUS.post(new EventMouseKey(
                button, action,
                pos[0], pos[1]
            ));
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
        double[] pos = MouseUtils.getPos();
        Main.EVENT_BUS.post(new EventMouseScroll(
            pos[0], pos[1], horizontal, vertical
        ));
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void onCursorPosHook(long window, double x, double y, CallbackInfo ci) {
        double[] pos = MouseUtils.getPos();

        double deltaX = pos[0] - lastX;
        double deltaY = pos[1] - lastY;

        lastX = pos[0];
        lastY = pos[1];

        Main.EVENT_BUS.post(new EventMouseMove(
            pos[0], pos[1], deltaX, deltaY
        ));
    }
}