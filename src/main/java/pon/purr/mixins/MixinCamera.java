package pon.purr.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import pon.purr.Purr;
import pon.purr.events.impl.EventPositionCamera;
import pon.purr.events.impl.EventRotateCamera;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow protected abstract float clipToSpace(float desiredCameraDistance);

//    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V", ordinal = 0))
//    public void modifyArgsMoveBy(Args args) {
//        ModifyCamera modifyCamera = (ModifyCamera) Purr.moduleManager.getModuleByClass(ModifyCamera.class);
//        if (modifyCamera.getEnable() && modifyCamera.rewriteDistance.getValue())
//            args.set(0, -clipToSpace(modifyCamera.distance.getValue()));
//    }

//    @Inject(method = "clipToSpace", at = @At(value = "HEAD"), cancellable = true)
//    public void modifyClipToSpace(float defaultDistance, CallbackInfoReturnable<Float> cir) {
//        if (ModuleList.cameraClip.isEnabled()) {
//            cir.setReturnValue(defaultDistance);
//        }
//    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    public void modifyArgsSetRotationOnUpdate(Args args) {
        EventRotateCamera event = new EventRotateCamera(args.get(0), args.get(1));
        Purr.EVENT_BUS.post(event);

        args.set(0, event.getYaw());
        args.set(1, event.getPitch());
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    public void modifyArgsSetPosOnUpdate(Args args) {
        EventPositionCamera event = new EventPositionCamera(args.get(0), args.get(1), args.get(2), MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks());
        Purr.EVENT_BUS.post(event);

        args.set(0, event.getX());
        args.set(1, event.getY());
        args.set(2, event.getZ());
    }

//    @Inject(method = "getSubmersionType", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
//    public void modifyGetSubmersionTypeWithWaterReturn(CallbackInfoReturnable<CameraSubmersionType> cir) {
//        if (Client.isOptionActivated(ModuleList.noRender, ModuleList.noRender.waterFog))
//            cir.setReturnValue(CameraSubmersionType.NONE);
//        else cir.cancel();
//    }
//
//    @Inject(method = "getSubmersionType", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
//    public void modifyGetSubmersionTypeWithLavaReturn(CallbackInfoReturnable<CameraSubmersionType> cir) {
//        if (Client.isOptionActivated(ModuleList.noRender, ModuleList.noRender.lavaFog))
//            cir.setReturnValue(CameraSubmersionType.NONE);
//        else cir.cancel();
//    }
//
//    @Inject(method = "getSubmersionType", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
//    public void modifyGetSubmersionTypeWithPowderSnowReturn(CallbackInfoReturnable<CameraSubmersionType> cir) {
//        if (Client.isOptionActivated(ModuleList.noRender, ModuleList.noRender.powderSnowFog))
//            cir.setReturnValue(CameraSubmersionType.NONE);
//        else cir.cancel();
//    }
//
//    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
//    public void modifyIsThirdPerson(CallbackInfoReturnable<Boolean> cir) {
//        if (ModuleList.freeCam.isEnabled()) cir.setReturnValue(true);
//    }
}
