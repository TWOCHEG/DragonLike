package pon.purr.modules.world;

import meteordevelopment.orbit.*;
import pon.purr.Purr;
import pon.purr.events.impl.EventPostTick;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.BlockSelected;
import pon.purr.modules.settings.Header;
import pon.purr.modules.settings.ListSetting;
import pon.purr.modules.settings.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import pon.purr.utils.BlockHighlight;
import pon.purr.utils.Rotations;
import pon.purr.utils.math.AnimHelper;

import java.util.*;

public class Nuker extends Parent {
    private Setting<Boolean> avoidLava = new Setting<>(
        "avoid lava",
        true
    );
    private Setting<Boolean> movePause = new Setting<>(
        "move pause",
        false
    );
    private Setting<Boolean> avoidGravel = new Setting<>(
        "avoid gravel",
        true
    );
    private Setting<Float> breakRange = new Setting<>(
        "break range",
        6f,
        1f, 6f
    );
    private Header header = new Header("\"/nuker blocksList\"");
    private ListSetting<String> blockMode = new ListSetting<>(
        "block find mode",
        Arrays.asList("blacklist", "whitelist")
    );
    private Setting<Integer> breakDelay = new Setting<>(
        "break delay",
        20,
        0, 1000
    );
    private ListSetting<String> rotate = new ListSetting<>(
        "rotate",
        Arrays.asList("not normal", "normal", "packet")
    );
    private BlockSelected targetBlocks = new BlockSelected(this);

    private BlockPos miningTarget = null;
    private BlockHitResult miningHit = null;
    private int delayTimer = 0;
    private long miningStartTime = 0;
    private long miningTime = 0;
    private int miningStage = 0;

    private float anim = 0;
    private boolean animReverse = false;

    public Nuker() {
        super("nuker", Purr.Categories.world);

        WorldRenderEvents.START.register(context -> {
            if (client.player != null && enable && client.world != null) {
                boolean isPlayerMining = (
                    net.fabricmc.api.EnvType.CLIENT == null ?
                        false :
                        org.lwjgl.glfw.GLFW.glfwGetMouseButton(client.getWindow().getHandle(), client.options.attackKey.getDefaultKey().getCode()) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                );
                boolean move = false;
                if (movePause.getValue()) {
                    Vec3d speed = client.player.getVelocity();
                    move = (speed.x > 0.0f || speed.z > 0.0f || speed.y > 0.0f);
                }

                if (!isPlayerMining && !move) {
                    process();
                }
            }
        });
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (miningTarget != null && miningHit != null && enable && client.player != null && client.world != null) {
                BlockHighlight.renderHighlight(context, miningTarget, 1.0f, 1.0f, 1.0f, anim / 100);
            }
        });
    }

    @EventHandler
    private void onTick(EventPostTick e) {
        anim = AnimHelper.handleAnimValue(animReverse, anim, null);
        if (anim == 100) {
            animReverse = true;
        } else if (anim == 0) {
            animReverse = false;
        }

        if (client.player == null || client.world == null) return;

        if (miningTarget != null && miningHit != null) {
            ClientPlayerEntity player = client.player;
            BlockState state = client.world.getBlockState(miningTarget);
            if (state.isAir() || !canReach(miningTarget)) {
                abortMining();
                return;
            }

            if (miningStage == 0) {
                float hardness = state.getBlock().getHardness();
                if (hardness < 0) {
                    abortMining();
                    return;
                }

                float speed = player.getBlockBreakingSpeed(state);
                if (speed <= 0) {
                    abortMining();
                    return;
                }

                miningTime = (long) (hardness / speed * 20);
                miningStartTime = System.currentTimeMillis();
                miningStage = 1;

                // Начало разрушения
                look(miningTarget);
                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, miningTarget, miningHit.getSide()
                ));
                return;
            }

            // Обновление прогресса
            if (miningStage == 1) {
                long elapsed = System.currentTimeMillis() - miningStartTime;
                if (elapsed > miningTime) {
                    miningStage = 2;
                } else {
                    float progress = (float) elapsed / miningTime;
                    client.interactionManager.updateBlockBreakingProgress(miningTarget, miningHit.getSide());
                    return;
                }
            }

            // Завершение разрушения
            if (miningStage == 2) {
                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, miningTarget, miningHit.getSide()
                ));
                player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                resetMining();
            }
        }
    }

    private void process() {
        if (client.player == null || client.world == null) return;
        ClientPlayerEntity player = client.player;

        if (miningTarget != null) return;

        if (delayTimer > 0) {
            delayTimer--;
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

                    if (checkAvoid(pos)) continue;
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

        if (bestPos != null) {
            miningTarget = bestPos;
            miningHit = bestHit;
            miningStage = 0;
        }
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

    private boolean checkAvoid(BlockPos pos) {
        if (avoidLava.getValue()) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.offset(dir);
                BlockState neighborState = client.world.getBlockState(neighborPos);
                if (neighborState.isOf(Blocks.LAVA) || neighborState.isOf(Blocks.LAVA_CAULDRON)) {
                    return true;
                }
                if (avoidGravel.getValue() && dir.equals(Direction.UP)) {
                    neighborPos = pos.offset(dir);
                    neighborState = client.world.getBlockState(neighborPos);
                    if (neighborState.isOf(Blocks.GRAVEL)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void look(BlockPos pos) {
        if (client.player == null || client.getNetworkHandler() == null) return;

        Vec3d eyesPos = client.player.getCameraPosVec(1.0f);
        Vec3d target = pos.toCenterPos();
        Vec3d diff = target.subtract(eyesPos);

        double diffX = diff.x;
        double diffY = diff.y;
        double diffZ = diff.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        float currentYaw = client.player.getYaw();
        float currentPitch = client.player.getPitch();

        yaw = currentYaw + MathHelper.wrapDegrees(yaw - currentYaw) * 0.4f;
        pitch = currentPitch + (pitch - currentPitch) * 0.4f;
        pitch = MathHelper.clamp(pitch, -90, 90);

        if (rotate.getValue().equals("not normal")) {
            Rotations.rotate(yaw, pitch);
        } else if (rotate.getValue().equals("normal")) {
            Rotations.normalRotate(yaw, pitch);
        } else {
            Rotations.packetRotate(yaw, pitch);
        }
    }

    private void abortMining() {
        if (miningTarget != null && miningHit != null) {
            client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                miningTarget,
                miningHit.getSide()
            ));
        }
        resetMining();
    }

    private void resetMining() {
        miningTarget = null;
        miningHit = null;
        miningStage = 0;
        delayTimer = breakDelay.getValue();
    }
}