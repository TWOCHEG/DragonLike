package pon.main.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import pon.main.Main;
import pon.main.events.impl.*;
import pon.main.modules.Parent;
import pon.main.modules.client.Rotations;
import pon.main.modules.settings.BlockSelectCmd;
import pon.main.modules.settings.Setting;
import pon.main.utils.math.Timer;
import pon.main.utils.player.InteractionUtility;
import pon.main.utils.player.PlayerUtility;
import pon.main.utils.render.Render3D;
import pon.main.utils.world.ExplosionUtility;

import static net.minecraft.block.Blocks.*;

public class Nuker extends Parent {
    public Nuker() {
        super("nuker", Main.Categories.misc);
        setEnable(false, false);

        WorldRenderEvents.START.register(context -> {
            if (!getEnable()) {
                if (blockData != null) {
                    blockData = null;
                }
                return;
            }
            if (mode.getValue() == Mode.fast && breakTimer.passedMs(delay.getValue())) {
                breakBlock();
                breakTimer.reset();
            }
        });
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            if (blockData != null) {
                Render3D.highlightBlock(context, blockData.bp, 1, 1, 1);
            }
        });
    }

    private final BlockSelectCmd targetBlocks = (BlockSelectCmd) new BlockSelectCmd("/nuker block list", "nuker").onSet(
        (s) -> {
            if (mc.world != null) {
                mc.setScreen(new ChatScreen("/nuker blocksList"));
            }
        }
    );
    private final Setting<BlockSelection> blocks = new Setting<>("blocks", BlockSelection.blackList);

    private final Setting<Mode> mode = new Setting<>("mode", Mode.normal);
    private final Setting<Integer> delay = new Setting<>("delay", 25, 0, 100);
    private final Setting<Boolean> ignoreWalls = new Setting<>("ignore walls", false);
    private final Setting<Boolean> flatten = new Setting<>("flatten", true);
    private final Setting<Boolean> creative = new Setting<>("creative", false);
    private final Setting<Boolean> avoidLava = new Setting<>("avoid lava", true);
    private final Setting<Boolean> avoidGravel = new Setting<>("avoid gravel", true);
    private final Setting<Float> range = new Setting<>("range", 6f, 1f, 25f);

    private BlockData blockData;
    private Timer breakTimer = new Timer();

    private NukerThread nukerThread = new NukerThread();
    private float rotationYaw, rotationPitch;

    @Override
    public void onEnable() {
        nukerThread = new NukerThread();
        nukerThread.setName("NukerThread");
        nukerThread.setDaemon(true);
        nukerThread.start();
    }

    @Override
    public void onDisable() {
        nukerThread.interrupt();
    }

    @Override
    public void onSettingUpdate(Setting s) {
        if (!nukerThread.isAlive()) {
            nukerThread = new NukerThread();
            nukerThread.setName("NukerThread");
            nukerThread.setDaemon(true);
            nukerThread.start();
        }
    }

    @EventHandler
    public void onBlockDestruct(EventSetBlockState e) {
        if (blockData != null && e.getPos() == blockData.bp && e.getState().isAir()) {
            blockData = null;
            new Thread(() -> {
                if ((blocks.getValue().equals(BlockSelection.blackList)) && !mc.options.attackKey.isPressed() && blockData == null) {
                    blockData = getNukerBlockPos();
                }
            }).start();
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if(rotationYaw != -999) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
            rotationYaw = -999;
        }
    }


    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        if (blockData != null) {
            if (PlayerUtility.squaredDistanceFromEyes(blockData.bp.toCenterPos()) > range.getPow2Value()
                    || mc.world.isAir(blockData.bp)
                    || (!ignoreWalls.getValue() && !isBlockVisible(blockData.bp))) {
                blockData = null;
            }
        }

        if (blockData == null || mc.options.attackKey.isPressed()) return;

        float[] angle = InteractionUtility.calculateAngle(blockData.vec3d);
        rotationYaw = angle[0];
        rotationPitch = angle[1];
        Main.MODULE_MANAGER.getModule(Rotations.class).fixRotation = rotationYaw;

        if (mode.getValue() == Mode.normal) {
            breakBlock();
        }

        if (mode.getValue() == Mode.fastAF) {
            int intRange = (int) (Math.floor(range.getValue()) + 1);
            Iterable<BlockPos> blocks_ = BlockPos.iterateOutwards(new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange);

            for (BlockPos b : blocks_) {
                if (flatten.getValue() && b.getY() < mc.player.getY())
                    continue;

                if (avoidLava.getValue() && checkLava(b))
                    continue;

                BlockState state = mc.world.getBlockState(b);

                if (PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= range.getPow2Value()) {
                    if (isAllowed(state.getBlock())) {
                        try {
                            sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, b, Direction.UP, id));
                            mc.interactionManager.breakBlock(b);
                            mc.player.swingHand(Hand.MAIN_HAND);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private boolean isBlockVisible(BlockPos bp) {
        if (ignoreWalls.getValue()) return true;

        for (float x1 = 0f; x1 <= 1f; x1 += 0.2f) {
            for (float y1 = 0f; y1 <= 1f; y1 += 0.2f) {
                for (float z1 = 0f; z1 <= 1f; z1 += 0.2f) {
                    Vec3d p = new Vec3d(bp.getX() + x1, bp.getY() + y1, bp.getZ() + z1);
                    BlockHitResult bhr = mc.world.raycast(new RaycastContext(
                        InteractionUtility.getEyesPos(mc.player),
                        p,
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        mc.player
                    ));
                    if (bhr.getType() == HitResult.Type.BLOCK && bhr.getBlockPos().equals(bp)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public synchronized void breakBlock() {
        if (blockData == null || mc.options.attackKey.isPressed()) return;
        boolean success = mc.interactionManager.updateBlockBreakingProgress(blockData.bp, blockData.dir);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (creative.getValue()) {
            mc.interactionManager.breakBlock(blockData.bp);
        }
        if (!success) {
            blockData = null;
        }
    }

    public BlockData getNukerBlockPos() {
        int intRange = (int) (Math.floor(range.getValue()) + 1);
        Iterable<BlockPos> blocks_ = BlockPos.iterateOutwards(new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange);

        for (BlockPos b : blocks_) {
            BlockState state = mc.world.getBlockState(b);
            if (flatten.getValue() && b.getY() < mc.player.getY())
                continue;
            if (PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= range.getPow2Value()) {
                if (avoidLava.getValue() && checkLava(b))
                    continue;
                if (avoidGravel.getValue() && checkGravel(b))
                    continue;
                if (isAllowed(state.getBlock())) {
                    if (ignoreWalls.getValue()) {
                        BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(InteractionUtility.getEyesPos(mc.player), b.toCenterPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), b);
                        if(result != null)
                            return new BlockData(b, result.getPos(), result.getSide());
                    } else {
                        for (float x1 = 0f; x1 <= 1f; x1 += 0.2f) {
                            for (float y1 = 0f; y1 <= 1; y1 += 0.2f) {
                                for (float z1 = 0f; z1 <= 1; z1 += 0.2f) {
                                    Vec3d p = new Vec3d(b.getX() + x1, b.getY() + y1, b.getZ() + z1);
                                    BlockHitResult bhr = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), p, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
                                    if (bhr != null && bhr.getType() == HitResult.Type.BLOCK && bhr.getBlockPos().equals(b))
                                        return new BlockData(b, p, bhr.getSide());
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean checkLava(BlockPos base) {
        for (Direction dir : Direction.values())
            if (mc.world.getBlockState(base.offset(dir)).getBlock() == Blocks.LAVA)
                return true;
        return false;
    }
    private boolean checkGravel(BlockPos base) {
        for (Direction dir : Direction.values())
            if (mc.world.getBlockState(base.offset(dir)).getBlock() == GRAVEL)
                return true;
        return false;
    }

    public class NukerThread extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (!getEnable()) continue;
                try {
                    if (!Parent.fullNullCheck()) {
                        if (!mc.options.attackKey.isPressed() && blockData == null) {
                            blockData = getNukerBlockPos();
                        }
                    } else {
                        Thread.yield();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private boolean isAllowed(Block block) {
        boolean allowed = targetBlocks.getValue().contains(BlockSelectCmd.convertId(block.getTranslationKey()));
        return switch (blocks.getValue()) {
            case blackList -> !allowed;
            case whiteList -> allowed;
        };
    }

    private enum Mode {
        normal, fast, fastAF
    }

    private enum BlockSelection {
        blackList, whiteList
    }

    public record BlockData(BlockPos bp, Vec3d vec3d, Direction dir) {
    }
}
