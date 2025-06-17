package purr.purr.mixins;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import purr.purr.Purr;
import purr.purr.events.impl.EventPositionCamera;
import purr.purr.events.impl.EventRotateCamera;
import purr.purr.modules.Player.FreeCam;

@Mixin(Camera.class)
public abstract class MixinCamera {
    private float lastTickDelta;

    protected MixinCamera(float lastTickDelta) {
        this.lastTickDelta = lastTickDelta;
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    public void modifyArgsSetRotationOnUpdate(Args args) {
        EventRotateCamera event = new EventRotateCamera(args.get(0), args.get(1));
        Purr.EVENT_BUS.post(event);

        args.set(0, args.get(0));
        args.set(1, args.get(1));
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    public void modifyArgsSetPosOnUpdate(Args args) {
        EventPositionCamera event = new EventPositionCamera(args.get(0), args.get(1), args.get(2), lastTickDelta);
        Purr.EVENT_BUS.post(event);

        args.set(0, event.getX());
        args.set(1, event.getY());
        args.set(2, event.getZ());
    }

    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    public void modifyIsThirdPerson(CallbackInfoReturnable<Boolean> cir) {
        if ((Purr.moduleManager.getModuleByClass(FreeCam.class)).getEnable()) cir.setReturnValue(true);
    }
}