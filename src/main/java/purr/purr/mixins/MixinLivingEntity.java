package purr.purr.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import purr.purr.Purr;
import purr.purr.events.impl.EventJumpHeight;
import purr.purr.events.impl.EventPlayerTravel;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract float getJumpBoostVelocityModifier();

    @Shadow public abstract void remove(RemovalReason reason);

    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

//    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
//    public void modifyGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
//        if (ModuleList.handTweaks.isEnabled() && ModuleList.handTweaks.customSwingSpeed.getValue())
//            cir.setReturnValue(ModuleList.handTweaks.swingSpeed.getValue().intValue());
//    }

    @Shadow public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Shadow public abstract boolean removeStatusEffect(RegistryEntry<StatusEffect> effect);

    @SuppressWarnings({"ConstantValue", "UnreachableCode"})
    @Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
    public void modifyIsBaby(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && (Object) this != mc.player) {
            cir.setReturnValue(true);
        }
    }

    @SuppressWarnings({"UnreachableCode", "ConstantValue"})
    @Inject(method = "getJumpVelocity(F)F", at = @At("TAIL"), cancellable = true)
    public void modifyGetJumpVelocity(float strength, CallbackInfoReturnable<Float> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || (Object) this != mc.player) return;
        EventJumpHeight event = new EventJumpHeight((float) getAttributeValue(EntityAttributes.JUMP_STRENGTH) * strength * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier());

        Purr.EVENT_BUS.post(event);

        if (event.isCancelled())
            cir.setReturnValue(0f);
        else
            cir.setReturnValue(event.getJumpHeight());
    }

    @SuppressWarnings({"UnreachableCode", "ConstantValue"})
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void modifyTravelPre(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || (Object) this != mc.player) return;
        EventPlayerTravel event = new EventPlayerTravel();
        Purr.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            move(MovementType.SELF, getVelocity());
            ci.cancel();
        }
    }
}