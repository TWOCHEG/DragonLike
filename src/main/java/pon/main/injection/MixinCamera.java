package pon.main.injection;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import pon.main.Main;
import pon.main.events.impl.EventPositionCamera;
import pon.main.events.impl.EventRotateCamera;
import pon.main.utils.render.Render3D;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    public void modifyArgsSetRotationOnUpdate(Args args) {
        EventRotateCamera event = new EventRotateCamera(args.get(0), args.get(1));
        Main.EVENT_BUS.post(event);

        args.set(0, event.getYaw());
        args.set(1, event.getPitch());
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    public void modifyArgsSetPosOnUpdate(Args args) {
        EventPositionCamera event = new EventPositionCamera(args.get(0), args.get(1), args.get(2), Render3D.getTickDelta());
        Main.EVENT_BUS.post(event);

        args.set(0, event.getX());
        args.set(1, event.getY());
        args.set(2, event.getZ());
    }
}
