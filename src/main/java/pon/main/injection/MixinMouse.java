package pon.main.injection;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.main.Main;
import pon.main.events.impl.EventMouse;

import static pon.main.modules.Parent.mc;


@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            Main.EVENT_BUS.post(new EventMouse(button, action));
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            Main.EVENT_BUS.post(new EventMouse((int) vertical, 2));
        }
    }
}
