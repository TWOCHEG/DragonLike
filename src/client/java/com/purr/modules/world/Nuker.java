package com.purr.modules.world;

import com.purr.modules.Parent;
import com.purr.modules.settings.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.*;

public class Nuker extends Parent {
    private Setting<Boolean> avoidLava = new Setting<>(
            "avoid lava",
            "avoid_lava",
            config.get("avoid_lava", true)
    );
    private Setting<Boolean> randomDelay = new Setting<>(
            "randomization delay",
            "random_delay",
            config.get("random_delay", false)
    );
    private Setting<Float> breakRange = new Setting<>(
            "break range",
            "break_range",
            config.get("break_range", 6.0f),
            1.0f, 6.0f
    );
    private TextSetting header = new TextSetting("\"/nuker blocksList\"");
    private ListSetting<String> blockMode = new ListSetting<>(
            "blocks mode",
            "block_mode",
            config.get("block_mode", "blacklist"),
            Arrays.asList("whitelist", "blacklist")
    );
    private Setting<Integer> breakDelay = new Setting<>(
            "break delay",
            "break_delay",
            config.get("break_delay", 20F).intValue(),
            0, 200
    );
    private BlockSelected targetBlocks = new BlockSelected(this);

    private BlockPos currentTarget = null;
    private BlockHitResult currentHit = null;
    private int delayTimer = 0;

    public Nuker() {
        super("nuker", "nuker", "world");

        WorldRenderEvents.START.register(context -> {
            if (client.player != null && enable) {
                boolean isPlayerMining = client.options.attackKey.isPressed();
                if (!isPlayerMining) {
                    process();
                }
            }
        });
    }

    private void process() {
        if (client.player == null || client.world == null) return;
        ClientPlayerEntity player = client.player;

        // продолжаем ломать
        if (currentTarget != null && currentHit != null) {
            BlockState state = client.world.getBlockState(currentTarget);
            if (state.isAir() || !canReach(currentTarget)) {
                client.options.attackKey.setPressed(false);
                resetNuker();
                return;
            }

            look(currentTarget);
            client.options.attackKey.setPressed(true);
            player.swingHand(Hand.MAIN_HAND);
            return;
        }

        if (delayTimer > 0) {
            int n = 1;
            if (randomDelay.getValue()) {
                Random rnd = new Random();
                n = rnd.nextInt(-5, 0);
            }
            delayTimer -= n;
            return;
        }

        float range = breakRange.getValue();
        String mode = blockMode.getValue();
        List<String> configured = targetBlocks.getValue();
        Vec3d eyePos = player.getCameraPosVec(1.0f);

        BlockPos bestPos = null;
        BlockHitResult bestHit = null;
        double bestDistSq = Double.MAX_VALUE;

        int intRange = (int) Math.ceil(range);
        BlockPos playerBlockPos = player.getBlockPos();
        int playerY = playerBlockPos.getY();

        for (int dx = -intRange; dx <= intRange; dx++) {
            for (int dy = -intRange; dy <= intRange; dy++) {
                for (int dz = -intRange; dz <= intRange; dz++) {
                    BlockPos pos = playerBlockPos.add(dx, dy, dz);
                    if (pos.getY() < playerY) continue;

                    double centerX = pos.getX() + 0.5;
                    double centerY = pos.getY() + 0.5;
                    double centerZ = pos.getZ() + 0.5;
                    double distSq = player.getPos().squaredDistanceTo(centerX, centerY, centerZ);
                    if (distSq > breakRange.getValue() * breakRange.getValue()) continue;

                    BlockState state = client.world.getBlockState(pos);
                    if (state.isAir()) continue;
                    FluidState fluid = state.getFluidState();
                    if (!fluid.isEmpty()) continue;

                    String idStr = Registries.BLOCK.getId(state.getBlock()).toString();
                    if (mode.equals("whitelist") && !configured.contains(idStr)) continue;
                    if (mode.equals("blacklist") && configured.contains(idStr)) continue;

                    if (checkLava(pos)) continue;
                    if (!canReach(pos)) continue;

                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        bestPos = pos;
                        Vec3d targetCenter = new Vec3d(centerX, centerY, centerZ);
                        bestHit = client.world.raycast(new RaycastContext(
                            eyePos,
                            targetCenter,
                            RaycastContext.ShapeType.OUTLINE,
                            RaycastContext.FluidHandling.NONE,
                            player
                        ));
                    }
                }
            }
        }

        if (bestPos != null && bestHit != null) {
            currentTarget = bestPos;
            currentHit = bestHit;
        }
    }

    private boolean checkLava(BlockPos pos) {
        if (avoidLava.getValue()) {
            boolean adjacentToLava = false;
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.offset(dir);
                BlockState neighborState = client.world.getBlockState(neighborPos);
                if (neighborState.isOf(Blocks.LAVA) || neighborState.isOf(Blocks.LAVA_CAULDRON)) {
                    adjacentToLava = true;
                    break;
                }
            }

            // Если вокруг блока есть лава или лавный котёл, пропускаем его
            if (adjacentToLava) {
                return true;
            }
        }
        return false;
    }

    private void look(BlockPos pos) {
        Vec3d targetCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3d diff = targetCenter.subtract(client.player.getCameraPosVec(1.0f));
        double dx = diff.x;
        double dy = diff.y;
        double dz = diff.z;
        double distHoriz = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, distHoriz)));

        client.player.setYaw(yaw);
        client.player.setPitch(pitch);
    }

    private void resetNuker() {
        currentTarget = null;
        currentHit = null;
        delayTimer = breakDelay.getValue();
    }

    private boolean canReach(BlockPos pos) {
        ClientPlayerEntity player = client.player;
        Vec3d eyePos = player.getCameraPosVec(1.0f);

        double effectiveRange = breakRange.getValue();

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        double distSq = player.getPos().squaredDistanceTo(centerX, centerY, centerZ);
        if (distSq > effectiveRange * effectiveRange) {
            return false;
        }

        Vec3d targetCenter = new Vec3d(centerX, centerY, centerZ);
        BlockHitResult ray = client.world.raycast(new RaycastContext(
            eyePos,
            targetCenter,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));

        return (
            ray.getType() == HitResult.Type.BLOCK &&
            ray.getBlockPos().equals(pos) &&
            client.world.getBlockState(pos).getBlock().getHardness() != -1
        );
    }
}
