package purr.purr.mixins;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import purr.purr.Purr;
import purr.purr.events.impl.EventKeyboardInput;

@Mixin(Keyboard.class)
public class MixinKeyboardInput {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            if (MinecraftClient.getInstance().player != null) {
                EventKeyboardInput e = new EventKeyboardInput();
                Purr.EVENT_BUS.post(e);
            }
        }
    }
}