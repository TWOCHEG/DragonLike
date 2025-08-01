package pon.purr.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pon.purr.Purr;
import pon.purr.events.impl.*;
import pon.purr.modules.Parent;


@Mixin(value = ClientPlayerEntity.class, priority = 800)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    private static MinecraftClient mc = MinecraftClient.getInstance();
    @Unique
    boolean pre_sprint_state = false;
    @Unique
    private boolean updateLock = false;
    @Unique
    private Runnable postAction;

    @Shadow
    public abstract float getPitch(float tickDelta);
    @Shadow
    protected abstract void sendMovementPackets();

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickHook(CallbackInfo info) {
        if (Parent.fullNullCheck()) return;
        Purr.EVENT_BUS.post(new EventPlayerUpdate());
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    private boolean tickMovementHook(ClientPlayerEntity player) {
//        if (ModuleManager.noSlow.isEnabled() && ModuleManager.noSlow.canNoSlow())
//            return false;
        return player.isUsingItem();
    }

//    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
//    public void shouldSlowDownHook(CallbackInfoReturnable<Boolean> cir) {
//        if (ModuleManager.noSlow.isEnabled()) {
//            if (isCrawling()) {
//                if (ModuleManager.noSlow.crawl.getValue())
//                    cir.setReturnValue(false);
//            } else {
//                if (ModuleManager.noSlow.sneak.getValue())
//                    cir.setReturnValue(false);
//            }
//        }
//    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;
        EventMove event = new EventMove(movement.x, movement.y, movement.z);
        Purr.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void sendMovementPacketsHook(CallbackInfo info) {
        if (Parent.fullNullCheck()) return;
        EventSync event = new EventSync(getYaw(), getPitch());
        Purr.EVENT_BUS.post(event);
        postAction = event.getPostAction();
        EventSprint e = new EventSprint(isSprinting());
        Purr.EVENT_BUS.post(e);
        Purr.EVENT_BUS.post(new EventAfterRotate());
        if (e.getSprintState() != mc.player.isSprinting()) {
            if (e.getSprintState())
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_SPRINTING));
            else
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

            mc.player.setSprinting(e.getSprintState());
        }
        pre_sprint_state = mc.player.isSprinting();

        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"), cancellable = true)
    private void sendMovementPacketsPostHook(CallbackInfo info) {
        if (Parent.fullNullCheck()) return;
        mc.player.setSprinting(pre_sprint_state);
        EventPostSync event = new EventPostSync();
        Purr.EVENT_BUS.post(event);
        if(postAction != null) {
            postAction.run();
            postAction = null;
        }
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void PostUpdateHook(CallbackInfo info) {
        if(Parent.fullNullCheck()) return;
        if (updateLock) {
            return;
        }
        PostPlayerUpdateEvent playerUpdateEvent = new PostPlayerUpdateEvent();
        Purr.EVENT_BUS.post(playerUpdateEvent);
        if (playerUpdateEvent.isCancelled()) {
            info.cancel();
            if (playerUpdateEvent.getIterations() > 0) {
                for (int i = 0; i < playerUpdateEvent.getIterations(); i++) {
                    updateLock = true;
                    tick();
                    updateLock = false;
                    sendMovementPackets();
                }
            }
        }
    }

//    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
//    private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
//        if (ModuleManager.noPush.isEnabled() && ModuleManager.noPush.blocks.getValue()) {
//            info.cancel();
//        }
//    }

//    @Inject(method = "tickNausea", at = @At("HEAD"), cancellable = true)
//    private void updateNauseaHook(CallbackInfo ci) {
//        if(ModuleManager.portalInventory.isEnabled())
//            ci.cancel();
//    }
}