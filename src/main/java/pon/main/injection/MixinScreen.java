package pon.main.injection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.main.Main;
import pon.main.events.impl.EventFramebufferResize;
import pon.main.events.impl.EventResizeScreen;

@Mixin(Window.class)
public class MixinScreen {
    @Inject(method = "onFramebufferSizeChanged", at = @At("HEAD"))
    private void onFramebufferSizeChanged(long window, int width, int height, CallbackInfo ci) {
        Main.EVENT_BUS.post(new EventFramebufferResize(width, height));
    }

    @Inject(method = "onWindowSizeChanged", at = @At("HEAD"))
    private void onWindowSizeChanged(long window, int width, int height, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Main.EVENT_BUS.post(new EventResizeScreen(width, height, mc != null ? mc.getWindow().getScaleFactor() : 1));
    }
}
