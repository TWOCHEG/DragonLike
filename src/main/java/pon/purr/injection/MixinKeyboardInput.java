package pon.purr.injection;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.purr.Purr;
import pon.purr.events.impl.EventKeyboardInput;
import pon.purr.modules.Parent;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/KeyboardInput;sneaking:Z", shift = At.Shift.BEFORE), cancellable = true)
    private void onSneak(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;
        EventKeyboardInput event = new EventKeyboardInput();
        Purr.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }
}
