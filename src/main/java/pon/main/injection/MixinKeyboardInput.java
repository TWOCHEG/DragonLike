package pon.main.injection;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pon.main.Main;
import pon.main.events.impl.EventKeyboardInput;
import pon.main.modules.Parent;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z",
            ordinal = 5
        )
    )
    private boolean onSneak(KeyBinding keyBinding) {
        if (Parent.fullNullCheck()) {
            return keyBinding.isPressed();
        }

        EventKeyboardInput event = new EventKeyboardInput();
        Main.EVENT_BUS.post(event);

        return event.isCancelled() ? false : keyBinding.isPressed();
    }
}
