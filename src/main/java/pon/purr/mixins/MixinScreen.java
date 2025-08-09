package pon.purr.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.purr.Purr;
import pon.purr.events.impl.EventFramebufferResize;
import pon.purr.events.impl.EventResizeScreen;

@Mixin(Window.class)
public class MixinScreen {
    @Inject(method = "onFramebufferSizeChanged", at = @At("HEAD"))
    private void onFramebufferSizeChanged(long window, int width, int height, CallbackInfo ci) {
        Purr.EVENT_BUS.post(new EventFramebufferResize(width, height));
    }

    @Inject(method = "onWindowSizeChanged", at = @At("HEAD"))
    private void onWindowSizeChanged(long window, int width, int height, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Purr.EVENT_BUS.post(new EventResizeScreen(width, height, mc != null ? mc.getWindow().getScaleFactor() : 1));
    }
}
