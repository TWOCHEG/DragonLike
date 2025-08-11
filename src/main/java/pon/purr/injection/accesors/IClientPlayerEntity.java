package pon.purr.injection.accesors;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface IClientPlayerEntity {
    @Invoker(value = "sendMovementPackets")
    void iSendMovementPackets();

    @Accessor(value = "lastRenderYaw")
    float getLastYaw();

    @Accessor(value = "lastRenderPitch")
    float getLastPitch();

    @Accessor(value = "lastRenderYaw")
    void setLastYaw(float yaw);

    @Accessor(value = "lastRenderPitch")
    void setLastPitch(float pitch);

    @Accessor(value = "mountJumpStrength")
    void setMountJumpStrength(float v);
}
