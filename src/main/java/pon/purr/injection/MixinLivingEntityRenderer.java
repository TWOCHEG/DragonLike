package pon.purr.injection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import pon.purr.Purr;
import pon.purr.managers.Managers;
import pon.purr.injection.accesors.IClientPlayerEntity;
import pon.purr.modules.Parent;
import pon.purr.modules.render.FreeCam;
import pon.purr.modules.render.NoRender;
import pon.purr.utils.ColorUtils;
import pon.purr.utils.render.Render2DEngine;
import pon.purr.utils.render.Render3DEngine;

import java.util.List;

import static pon.purr.modules.Parent.mc;

@Mixin(net.minecraft.client.render.entity.LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    private LivingEntity lastEntity;

    private float originalHeadYaw, originalPrevHeadYaw, originalPrevHeadPitch, originalHeadPitch;

    @Shadow
    protected M model;

    @Shadow @Final protected List<FeatureRenderer<S, M>> features;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;
        if (mc.player != null && livingEntity == mc.player && mc.player.getControllingVehicle() == null && !Purr.isFuturePresent()) {
            originalHeadYaw = livingEntity.headYaw;
            originalPrevHeadYaw = livingEntity.getYaw();
            originalPrevHeadPitch = livingEntity.getPitch();
            originalHeadPitch = livingEntity.getPitch();

            livingEntity.setPitch(((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastPitch());
            livingEntity.lastPitch = Managers.PLAYER.lastPitch;
            livingEntity.headYaw = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
            livingEntity.bodyYaw = Render2DEngine.interpolateFloat(Managers.PLAYER.prevBodyYaw, Managers.PLAYER.bodyYaw, Render3DEngine.getTickDelta());
            livingEntity.lastHeadYaw = Managers.PLAYER.lastYaw;
            livingEntity.lastBodyYaw = Render2DEngine.interpolateFloat(Managers.PLAYER.prevBodyYaw, Managers.PLAYER.bodyYaw, Render3DEngine.getTickDelta());
        }

        if (livingEntity != mc.player && Purr.moduleManager.getModuleByClass(FreeCam.class) instanceof FreeCam freeCam) {
            if (freeCam.getEnable() && freeCam.track.getValue() && freeCam.trackEntity != null && freeCam.trackEntity == livingEntity) {
                ci.cancel();
                return;
            }
        }

        lastEntity = livingEntity;

//        if (livingEntity instanceof PlayerEntity pe && ModuleManager.chams.isEnabled() && ModuleManager.chams.players.getValue()) {
//            ModuleManager.chams.renderPlayer(pe, f, g, matrixStack, i, model, ci, () -> postRender(livingEntity));
//            if (!pe.isSpectator()) {
//                float n;
//                Direction direction;
//                Entity entity;
//                matrixStack.push();
//                float h = MathHelper.lerpAngleDegrees(g, pe.lastBodyYaw, pe.bodyYaw);
//                float j = MathHelper.lerpAngleDegrees(g, pe.lastHeadYaw, pe.headYaw);
//                float k = j - h;
//                if (pe.hasVehicle() && (entity = pe.getVehicle()) instanceof LivingEntity) {
//                    LivingEntity livingEntity2 = (LivingEntity) entity;
//                    h = MathHelper.lerpAngleDegrees(g, livingEntity2.lastBodyYaw, livingEntity2.bodyYaw);
//                    k = j - h;
//                    float l = MathHelper.wrapDegrees(k);
//                    if (l < -85.0f) {
//                        l = -85.0f;
//                    }
//                    if (l >= 85.0f) {
//                        l = 85.0f;
//                    }
//                    h = j - l;
//                    if (l * l > 2500.0f) {
//                        h += l * 0.2f;
//                    }
//                    k = j - h;
//                }
//                float m = MathHelper.lerp(g, pe.lastPitch, pe.getPitch());
//                if (LivingEntityRenderer.shouldFlipUpsideDown(pe)) {
//                    m *= -1.0f;
//                    k *= -1.0f;
//                }
//                if (pe.isInPose(EntityPose.SLEEPING) && (direction = pe.getSleepingDirection()) != null) {
//                    n = pe.getEyeHeight(EntityPose.STANDING) - 0.1f;
//                    matrixStack.translate((float) (-direction.getOffsetX()) * n, 0.0f, (float) (-direction.getOffsetZ()) * n);
//                }
//                float l = pe.age + g;
//                ModuleManager.chams.setupTransforms1(pe, matrixStack, l, h, g);
//                matrixStack.scale(-1.0f, -1.0f, 1.0f);
//                matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
//                matrixStack.translate(0.0f, -1.501f, 0.0f);
//                n = 0.0f;
//                float o = 0.0f;
//                if (!pe.hasVehicle() && pe.isAlive()) {
//                    n = pe.limbAnimator.getSpeed();
//                    o = pe.limbAnimator.getPos(g);
//                    if (pe.isBaby())
//                        o *= 3.0f;
//
//                    if (n > 1.0f)
//                        n = 1.0f;
//                }
//
//                for (FeatureRenderer<T, M> featureRenderer : features) {
//                    featureRenderer.render(matrixStack, vertexConsumerProvider, i, livingEntity, o, n, g, l, k, m);
//                }
//                matrixStack.pop();
//            }
//        }
    }

    @Unique
    public void postRender(T livingEntity) {
        if (Parent.fullNullCheck()) return;
        if (mc.player != null && livingEntity == mc.player && mc.player.getControllingVehicle() == null && !Purr.isFuturePresent()) {
            livingEntity.lastPitch = originalPrevHeadPitch;
            livingEntity.setPitch(originalHeadPitch);
            livingEntity.headYaw = originalHeadYaw;
            livingEntity.lastHeadYaw = originalPrevHeadYaw;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;
        postRender(livingEntity);
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderHook(Args args) {
        if (Parent.fullNullCheck()) return;

        float alpha = -1f;

        if (Purr.moduleManager.getModuleByClass(NoRender.class) instanceof NoRender noRender) {
            if (noRender.getEnable() && noRender.antiPlayerCollision.getValue() && lastEntity != mc.player && lastEntity instanceof PlayerEntity pl && !pl.isInvisible())
                alpha = Math.clamp((float) (mc.player.squaredDistanceTo(lastEntity.getPos()) / 3f) + 0.2f, 0f, 1f);

            if (alpha != -1)
                args.set(4, ColorUtils.applyOpacity(0x26FFFFFF, alpha));
        }
    }
}
