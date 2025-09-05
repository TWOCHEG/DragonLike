package pon.main.injection;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import pon.main.Main;
import pon.main.events.impl.EventPositionCamera;
import pon.main.events.impl.EventRotateCamera;
import pon.main.modules.render.FreeCam;
import pon.main.utils.render.Render3D;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow
    protected abstract float clipToSpace(float desiredCameraDistance);

    @Shadow
    private boolean thirdPerson;

//    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V", ordinal = 0))
//    private void modifyCameraDistance(Args args) {
//        if (ModuleManager.noCameraClip.isEnabled()) {
//            args.set(0, -clipToSpace(ModuleManager.noCameraClip.getDistance()));
//        }
//    }
//
//    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
//    private void onClipToSpace(float f, CallbackInfoReturnable<Float> cir) {
//        if (ModuleManager.noCameraClip.isEnabled()) {
//            cir.setReturnValue(ModuleManager.noCameraClip.getDistance());
//        }
//    }

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

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosHook(Args args) {
        FreeCam freeCam = Main.MODULE_MANAGER.getModule(FreeCam.class);
        if (freeCam.getEnable())
            args.setAll(freeCam.getFakeX(), freeCam.getFakeY(), freeCam.getFakeZ());
    }
    @Inject(method = "update", at = @At("TAIL"))
    private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        FreeCam freeCam = Main.MODULE_MANAGER.getModule(FreeCam.class);
        if (freeCam.getEnable()) {
            this.thirdPerson = true;
        }
    }
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void setRotationHook(Args args) {
        FreeCam freeCam = Main.MODULE_MANAGER.getModule(FreeCam.class);
        if(freeCam.getEnable())
            args.setAll(freeCam.getFakeYaw(), freeCam.getFakePitch());
    }
}
