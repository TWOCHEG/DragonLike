package pon.main.injection;

import net.minecraft.client.MinecraftClient;
import pon.main.Main;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.main.events.impl.EventKey;
import pon.main.events.impl.EventKeyPress;
import pon.main.events.impl.EventKeyRelease;
import pon.main.gui.ModulesGui;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        boolean whitelist = mc.currentScreen == null || mc.currentScreen instanceof ModulesGui || mc.currentScreen instanceof TitleScreen;
        if (!whitelist) return;

        Main.EVENT_BUS.post(new EventKey(key, scanCode, modifiers));
        switch (action) {
            case 0 -> {
                EventKeyRelease event = new EventKeyRelease(key, scanCode, modifiers);
                Main.EVENT_BUS.post(event);
                if (event.isCancelled()) ci.cancel();
            }
            case 1 -> {
                EventKeyPress event = new EventKeyPress(key, scanCode, modifiers);
                Main.EVENT_BUS.post(event);
                if (event.isCancelled()) ci.cancel();
            }
        }
    }
}