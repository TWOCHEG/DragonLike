package pon.purr.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.purr.Purr;
import pon.purr.modules.render.FreeCam;
import pon.purr.modules.render.NoCameraClip;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow
    protected abstract float clipToSpace(float desiredCameraDistance);

    @Shadow
    private boolean thirdPerson;

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V", ordinal = 0))
    private void modifyCameraDistance(Args args) {
        if (Purr.moduleManager.getModuleByClass(NoCameraClip.class) instanceof NoCameraClip ncp) {
            if (ncp.getEnable()) {
                args.set(0, -clipToSpace(ncp.getDistance()));
            }
        }
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float f, CallbackInfoReturnable<Float> cir) {
        if (Purr.moduleManager.getModuleByClass(NoCameraClip.class) instanceof NoCameraClip ncp) {
            if (ncp.getEnable()) {
                cir.setReturnValue(ncp.getDistance());
            }
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (Purr.moduleManager.getModuleByClass(FreeCam.class) instanceof FreeCam f) {
            if (f.getEnable()) {
                this.thirdPerson = true;
            }
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void setRotationHook(Args args) {
        if (Purr.moduleManager.getModuleByClass(FreeCam.class) instanceof FreeCam f) {
            if (f.getEnable()) {
                args.setAll(f.getFakeYaw(), f.getFakePitch());
            }
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosHook(Args args) {
        if (Purr.moduleManager.getModuleByClass(FreeCam.class) instanceof FreeCam f) {
            if (f.getEnable()) {
                args.setAll(f.getFakeX(), f.getFakeY(),f.getFakeZ());
            }
        }
    }
}
