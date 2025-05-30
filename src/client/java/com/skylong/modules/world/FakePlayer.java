package com.skylong.modules.world;

import com.mojang.authlib.GameProfile;
import com.skylong.modules.Parent;
import com.skylong.modules.settings.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.data.TrackedData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.lang.reflect.Field;
import java.util.*;

public class FakePlayer extends Parent {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private OtherClientPlayerEntity fakePlayerEntity;
    private final String nickName = "fakePlayer";
    private final float health = 20.0f;
    private long lastMoveTime = 0;

    private final Setting<Boolean> copyInv = new Setting<>("copy inv", "copy_inv", config.get("copy_inv", false));
    private final Setting<Boolean> look = new Setting<>("look", "look", config.get("look", true));
    private final Setting<Boolean> move = new Setting<>("move", "move", config.get("move", false));
    public Setting<Float> moveSpeed = new Setting<>("move speed", "move_speed", config.get("move_speed", 3.0f), 0.1f, 6.0f);

    public FakePlayer() {
        super("fake player", "fake_player", "world");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!getEnable()) return;

            if (fakePlayerEntity == null) {
                spawn();
            } else {
                if (look.getValue()) {
                    updateLook();
                }
                if (move.getValue()) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastMoveTime >= (long)(moveSpeed.getValue() * 1000f)) {
                        randomMove();
                        lastMoveTime = currentTime;
                    }
                }
            }
        });
    }

    private void spawn() {
        if (mc.world == null || mc.player == null) return;
        spawn(mc.player);
    }

    private void spawn(PlayerEntity sourcePlayer) {
        fakePlayerEntity = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), nickName));
        fakePlayerEntity.copyPositionAndRotation(sourcePlayer);
        copyRotations(sourcePlayer);
        copyModelParts(sourcePlayer);
        fakePlayerEntity.getAttributes().setFrom(sourcePlayer.getAttributes());
        fakePlayerEntity.setPose(sourcePlayer.getPose());
        fakePlayerEntity.setHealth(Math.min(20, health));
        if (health > 20) {
            fakePlayerEntity.setAbsorptionAmount(health - 20);
        }
        if (copyInv.getValue()) fakePlayerEntity.getInventory().clone(sourcePlayer.getInventory());

        mc.world.addEntity(fakePlayerEntity);
    }

    public void despawn() {
        if (fakePlayerEntity != null && mc.world != null) {
            mc.world.removeEntity(fakePlayerEntity.getId(), OtherClientPlayerEntity.RemovalReason.DISCARDED);
            fakePlayerEntity = null;
        }
    }

    private void copyRotations(PlayerEntity source) {
        fakePlayerEntity.lastYaw = fakePlayerEntity.getYaw();
        fakePlayerEntity.lastPitch = fakePlayerEntity.getPitch();
        fakePlayerEntity.headYaw = source.headYaw;
        fakePlayerEntity.lastHeadYaw = source.headYaw;
        fakePlayerEntity.bodyYaw = source.bodyYaw;
        fakePlayerEntity.lastBodyYaw = source.bodyYaw;
    }

    @SuppressWarnings("unchecked")
    private void copyModelParts(PlayerEntity source) {
        try {
            Field field = PlayerEntity.class.getDeclaredField("PLAYER_MODEL_PARTS");
            field.setAccessible(true);
            TrackedData<Byte> modelPartsData = (TrackedData<Byte>) field.get(null);
            Byte modelParts = source.getDataTracker().get(modelPartsData);
            fakePlayerEntity.getDataTracker().set(modelPartsData, modelParts);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateLook() {
        if (mc.player == null || fakePlayerEntity == null) return;

        double dx = mc.player.getX() - fakePlayerEntity.getX();
        double dy = mc.player.getEyeY() - fakePlayerEntity.getEyeY();
        double dz = mc.player.getZ() - fakePlayerEntity.getZ();
        double distXZ = Math.sqrt(dx*dx + dz*dz);

        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx))) - 90.0f;
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));

        fakePlayerEntity.setPitch(targetPitch);

        float headYaw = wrapDegrees(fakePlayerEntity.headYaw, targetYaw, 75.0f);
        fakePlayerEntity.headYaw = headYaw;

        float bodyYaw = fakePlayerEntity.bodyYaw;
        float yawDifference = wrapDegrees(headYaw - bodyYaw);
        if (Math.abs(yawDifference) > 75.0f) {
            float bodyYawSpeed = 10.0f;
            bodyYaw += Math.signum(yawDifference) * bodyYawSpeed;
        }
        fakePlayerEntity.bodyYaw = bodyYaw;
        fakePlayerEntity.setYaw(bodyYaw);
    }

    private float wrapDegrees(float current, float target, float maxChange) {
        float delta = wrapDegrees(target - current);
        if (delta > maxChange) delta = maxChange;
        if (delta < -maxChange) delta = -maxChange;
        return current + delta;
    }

    private float wrapDegrees(float angle) {
        angle = angle % 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    private void randomMove() {
        if (fakePlayerEntity == null) return;

        double angle = Math.random() * 2 * Math.PI;
        double distance = 0.5 + Math.random(); // между 0.5 и 1.5 блоками

        double dx = Math.cos(angle) * distance;
        double dz = Math.sin(angle) * distance;

        fakePlayerEntity.updatePosition(
                fakePlayerEntity.getX() + dx,
                fakePlayerEntity.getY(),
                fakePlayerEntity.getZ() + dz
        );
    }


    public boolean isSpawned() {
        return fakePlayerEntity != null;
    }

    @Override
    public void setEnable(boolean value) {
        super.setEnable(value);
        if (!value) {
            despawn();
        }
    }
}
