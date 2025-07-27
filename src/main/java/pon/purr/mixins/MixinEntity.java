package pon.purr.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.purr.Purr;
import pon.purr.events.Event;
import pon.purr.events.impl.EventChangePlayerLook;
import pon.purr.events.impl.EventSetVelocity;


@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public Vec3d velocity;

    @Shadow public abstract Text getDisplayName();

    @Shadow public float yaw;

    @Shadow public abstract World getWorld();

    @Shadow public abstract Vec3d getVelocity();

    @Shadow public abstract void setVelocity(Vec3d velocity);

    @Shadow public float pitch;

    @Inject(method = "setVelocity(DDD)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"ConstantConditions", "UnreachableCode"})
    public void modifySetVelocity(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        EventSetVelocity event = new EventSetVelocity(new Vec3d(x, y, z));

        Purr.EVENT_BUS.post(event);

        if (!event.isCancelled())
            this.velocity = event.getVelocity();
        ci.cancel();
    }

    @Inject(method = "setVelocity(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"ConstantConditions", "UnreachableCode"})
    public void modifySetVelocityVec3d(Vec3d velocity, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        EventSetVelocity event = new EventSetVelocity(velocity);

        Purr.EVENT_BUS.post(event);

        if (!event.isCancelled())
            this.velocity = event.getVelocity();
        ci.cancel();
    }

//    @Inject(method = "isSprinting", at = @At("HEAD"), cancellable = true)
//    public void modifyIsSprinting(CallbackInfoReturnable<Boolean> cir) {
//        if (ModuleList.elytraFlight.isEnabled() && ModuleList.elytraFlight.mode.getValue().equals("Bounce") && (ModuleList.elytraFlight.alwaysPress.getValue().equals("Sprint") || ModuleList.elytraFlight.alwaysPress.getValue().equals("Multi")))
//            cir.setReturnValue(true);
//    }

//    @Inject(method = "setYaw", at = @At("HEAD"), cancellable = true)
//    @SuppressWarnings("ConstantConditions")
//    public void modifySetYaw(float yaw, CallbackInfo ci) {
//        MinecraftClient mc = MinecraftClient.getInstance();
//        if ((Object) this != mc.player) return;
//        if (ModuleList.noRotate.isEnabled()) {
//            ci.cancel();
//            this.yaw = NoRotateMathUtils.getNearestYawAxis(mc.player);
//        }
//    }

//    @Inject(method = "setPitch", at = @At("HEAD"), cancellable = true)
//    @SuppressWarnings("ConstantConditions")
//    public void modifySetPitch(float pitch, CallbackInfo ci) {
//        MinecraftClient mc = MinecraftClient.getInstance();
//        if ((Object) this != mc.player) return;
//        if (ModuleList.noRotate.isEnabled() && ModuleList.noRotate.blockPitch.getValue()) {
//            if (ModuleList.elytraFlight.isEnabled() && ModuleList.elytraFlight.mode.getValue().equals("Pitch40")) return;
//            ci.cancel();
//            this.pitch = NoRotateMathUtils.getNearestPitchAxis(mc.player);
//        }
//    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void modifyChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        Event event = new EventChangePlayerLook(cursorDeltaX, cursorDeltaY);
        Purr.EVENT_BUS.post(event);
        if (event.isCancelled())
            ci.cancel();
    }

//    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
//    @SuppressWarnings("ConstantConditions")
//    public void modifyPushAwayFrom(Entity entity, CallbackInfo ci) {
//        if ((Object) this == MinecraftClient.getInstance().player)
//            if (ModuleList.noPush.isEnabled())
//                if (ModuleList.noPush.entities.getValue())
//                    ci.cancel();
//    }

//    @SuppressWarnings({"ConstantValue", "UnreachableCode"})
//    @ModifyVariable(method = "updateMovementInFluid", at = @At("STORE"), ordinal = 1)
//    public Vec3d modifyGetFluidStateVelocityOnUpdateMovementInFluid(Vec3d vec3d) {
//        if ((Object) this == MinecraftClient.getInstance().player && ModuleList.noPush.isEnabled() && ModuleList.noPush.liquids.getValue())
//            return new Vec3d(0, vec3d.getY(), 0);
//        else return vec3d;
//    }
}
