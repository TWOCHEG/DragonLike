package purr.purr.mixins;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import purr.purr.Purr;
import purr.purr.events.impl.EventUpdateInput;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    @Inject(method = "tick", at = @At("RETURN"))
    public void modifyTick(CallbackInfo ci) {
        Purr.EVENT_BUS.post(new EventUpdateInput());
    }
}
