package pon.main.injection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import pon.main.Main;
import pon.main.injection.accesors.IClientPlayerEntity;
import pon.main.managers.Managers;
import pon.main.modules.Parent;
import pon.main.modules.render.FreeCam;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.render.Render3D;

import static pon.main.modules.Parent.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    private float originalHeadYaw, originalPrevHeadYaw, originalPrevHeadPitch, originalHeadPitch;

    @Inject(method = "updateRenderState", at = @At("HEAD"))
    public void onUpdateRenderStatePre(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;

        if (mc.player != null && livingEntity == mc.player && mc.player.getControllingVehicle() == null && !Main.isFuturePresent()) {
            originalHeadYaw = livingEntity.getYaw();
            originalPrevHeadYaw = livingEntity.lastYaw;
            originalPrevHeadPitch = livingEntity.lastPitch;
            originalHeadPitch = livingEntity.getPitch();

            livingEntity.setPitch(((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastPitch());
            livingEntity.lastPitch = Managers.PLAYER.lastPitch;
            livingEntity.headYaw = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.bodyYaw = Render2D.interpolateFloat(Managers.PLAYER.prevBodyYaw, Managers.PLAYER.bodyYaw, Render3D.getTickDelta());
            livingEntity.lastHeadYaw = Managers.PLAYER.lastYaw;
            livingEntity.lastBodyYaw = Render2D.interpolateFloat(Managers.PLAYER.prevBodyYaw, Managers.PLAYER.bodyYaw, Render3D.getTickDelta());
        }

        FreeCam freeCam = Managers.MODULE_MANAGER.getModule(FreeCam.class);
        if (livingEntity != mc.player && freeCam.getEnable() && freeCam.track.getValue() && freeCam.trackEntity != null && freeCam.trackEntity == livingEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    public void onUpdateRenderStatePost(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;

        if (mc.player != null && livingEntity == mc.player && mc.player.getControllingVehicle() == null && !Main.isFuturePresent()) {
            livingEntity.lastPitch = originalPrevHeadPitch;
            livingEntity.setPitch(originalHeadPitch);
            livingEntity.headYaw = originalHeadYaw;
            livingEntity.lastYaw = originalPrevHeadYaw;
        }
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderHook(Args args) {
        if (Parent.fullNullCheck()) return;

        float alpha = -1f;

        if (alpha != -1)
            args.set(4, ColorUtils.applyOpacity(0x26FFFFFF, alpha));
    }
}