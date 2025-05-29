package com.skylong.modules.world;

import com.mojang.authlib.GameProfile;
import com.skylong.modules.Parent;
import com.skylong.modules.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.data.TrackedData;

import java.lang.reflect.Field;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.skylong.modules.settings.*;

public class FakePlayer extends Parent {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private OtherClientPlayerEntity fakePlayerEntity;
    private final String fakeName = "fakePlayer";
    private final float health = 20.0f;
    private final Setting<Boolean> copyInv = new Setting<>("copy inv", "copy_inv", config.get("copy_inv", false));

    public FakePlayer() {
        super("fake player", "fake_player", "world");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (getEnable() && fakePlayerEntity == null) {
                spawn();
            }
        });
    }

    private void spawn() {
        if (mc.world == null || mc.player == null) return;
        spawn(mc.player);
    }

    private void spawn(PlayerEntity sourcePlayer) {
        fakePlayerEntity = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), fakeName));
        fakePlayerEntity.copyPositionAndRotation(sourcePlayer);

        fakePlayerEntity.lastYaw = fakePlayerEntity.getYaw();
        fakePlayerEntity.lastPitch = fakePlayerEntity.getPitch();
        fakePlayerEntity.headYaw = sourcePlayer.headYaw;
        fakePlayerEntity.lastHeadYaw = sourcePlayer.headYaw;
        fakePlayerEntity.bodyYaw = sourcePlayer.bodyYaw;
        fakePlayerEntity.lastBodyYaw = sourcePlayer.bodyYaw;

        try {
            Field field = PlayerEntity.class.getDeclaredField("PLAYER_MODEL_PARTS");
            field.setAccessible(true);
            TrackedData<Byte> modelPartsData = (TrackedData<Byte>) field.get(null);
            Byte modelParts = sourcePlayer.getDataTracker().get(modelPartsData);
            fakePlayerEntity.getDataTracker().set(modelPartsData, modelParts);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        fakePlayerEntity.getAttributes().setFrom(sourcePlayer.getAttributes());
        fakePlayerEntity.setPose(sourcePlayer.getPose());

        fakePlayerEntity.setHealth(Math.min(20, health));
        if (health > 20) {
            fakePlayerEntity.setAbsorptionAmount(health - 20);
        }

        if (copyInv.getValue()) fakePlayerEntity.getInventory().clone(sourcePlayer.getInventory());

        mc.world.addEntity(fakePlayerEntity);
    }

    @Override
    public void setEnable(boolean value) {
        super.setEnable(value);
        if (!value) {
            despawn();
        }
    }

    public void despawn() {
        if (fakePlayerEntity != null && mc.world != null) {
            mc.world.removeEntity(fakePlayerEntity.getId(), OtherClientPlayerEntity.RemovalReason.DISCARDED);
            fakePlayerEntity = null;
        }
    }

    public boolean isSpawned() {
        return fakePlayerEntity != null;
    }
}