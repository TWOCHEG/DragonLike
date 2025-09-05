package pon.main.utils.player;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static pon.main.modules.Parent.mc;

public final class InteractionUtility {
    private static final List<Block> SHIFT_BLOCKS = Arrays.asList(
            Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE,
            Blocks.BIRCH_TRAPDOOR, Blocks.BAMBOO_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.CHERRY_TRAPDOOR,
            Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
            Blocks.ACACIA_TRAPDOOR, Blocks.ENCHANTING_TABLE, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX
    );

    public static Map<BlockPos, Long> awaiting = new HashMap<>();

    public static Vec3d getEyesPos(@NotNull Entity entity) {
        return entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }

    public static float @NotNull [] calculateAngle(Vec3d to) {
        return calculateAngle(getEyesPos(mc.player), to);
    }

    public static float @NotNull [] calculateAngle(@NotNull Vec3d from, @NotNull Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));

        float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float) MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))), -90f, 90f);

        return new float[]{yD, pD};
    }

    public static @Nullable BlockPosWithFacing checkNearBlocks(@NotNull BlockPos blockPos) {
        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).isSolid())
            return new BlockPosWithFacing(blockPos.add(0, -1, 0), Direction.UP);

        else if (mc.world.getBlockState(blockPos.add(-1, 0, 0)).isSolid())
            return new BlockPosWithFacing(blockPos.add(-1, 0, 0), Direction.EAST);

        else if (mc.world.getBlockState(blockPos.add(1, 0, 0)).isSolid())
            return new BlockPosWithFacing(blockPos.add(1, 0, 0), Direction.WEST);

        else if (mc.world.getBlockState(blockPos.add(0, 0, 1)).isSolid())
            return new BlockPosWithFacing(blockPos.add(0, 0, 1), Direction.NORTH);

        else if (mc.world.getBlockState(blockPos.add(0, 0, -1)).isSolid())
            return new BlockPosWithFacing(blockPos.add(0, 0, -1), Direction.SOUTH);
        return null;
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d vec) {
        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static float squaredDistanceFromEyes2d(@NotNull Vec3d vec) {
        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        return (float) (d0 * d0 + d1 * d1);
    }

    public static @NotNull List<Direction> getStrictDirections(@NotNull BlockPos bp) {
        List<Direction> visibleSides = new ArrayList<>();
        Vec3d positionVector = bp.toCenterPos();

        double westDelta = getEyesPos(mc.player).x - (positionVector.add(0.5, 0, 0).x);
        double eastDelta = getEyesPos(mc.player).x - (positionVector.add(-0.5, 0, 0).x);
        double northDelta = getEyesPos(mc.player).z - (positionVector.add(0, 0, 0.5).z);
        double southDelta = getEyesPos(mc.player).z - (positionVector.add(0, 0, -0.5).z);
        double upDelta = getEyesPos(mc.player).y - (positionVector.add(0, 0.5, 0).y);
        double downDelta = getEyesPos(mc.player).y - (positionVector.add(0, -0.5, 0).y);

        if (westDelta > 0 && isSolid(bp.west()))
            visibleSides.add(Direction.EAST);
        if (westDelta < 0 && isSolid(bp.east()))
            visibleSides.add(Direction.WEST);
        if (eastDelta < 0 && isSolid(bp.east()))
            visibleSides.add(Direction.WEST);
        if (eastDelta > 0 && isSolid(bp.west()))
            visibleSides.add(Direction.EAST);

        if (northDelta > 0 && isSolid(bp.north()))
            visibleSides.add(Direction.SOUTH);
        if (northDelta < 0 && isSolid(bp.south()))
            visibleSides.add(Direction.NORTH);
        if (southDelta < 0 && isSolid(bp.south()))
            visibleSides.add(Direction.NORTH);
        if (southDelta > 0 && isSolid(bp.north()))
            visibleSides.add(Direction.SOUTH);

        if (upDelta > 0 && isSolid(bp.down()))
            visibleSides.add(Direction.UP);
        if (upDelta < 0 && isSolid(bp.up()))
            visibleSides.add(Direction.DOWN);
        if (downDelta < 0 && isSolid(bp.up()))
            visibleSides.add(Direction.DOWN);
        if (downDelta > 0 && isSolid(bp.down()))
            visibleSides.add(Direction.UP);

        return visibleSides;
    }

    public static boolean isSolid(BlockPos bp) {
        return mc.world.getBlockState(bp).isSolid() || awaiting.containsKey(bp);
    }

    public static @NotNull List<Direction> getStrictBlockDirections(@NotNull BlockPos bp) {
        List<Direction> visibleSides = new ArrayList<>();
        Vec3d pV = bp.toCenterPos();

        double westDelta = getEyesPos(mc.player).x - (pV.add(0.5, 0, 0).x);
        double eastDelta = getEyesPos(mc.player).x - (pV.add(-0.5, 0, 0).x);
        double northDelta = getEyesPos(mc.player).z - (pV.add(0, 0, 0.5).z);
        double southDelta = getEyesPos(mc.player).z - (pV.add(0, 0, -0.5).z);
        double upDelta = getEyesPos(mc.player).y - (pV.add(0, 0.5, 0).y);
        double downDelta = getEyesPos(mc.player).y - (pV.add(0, -0.5, 0).y);

        if (westDelta > 0 && mc.world.getBlockState(bp.east()).isReplaceable())
            visibleSides.add(Direction.EAST);

        if (eastDelta < 0 && mc.world.getBlockState(bp.west()).isReplaceable())
            visibleSides.add(Direction.WEST);

        if (northDelta > 0 && mc.world.getBlockState(bp.south()).isReplaceable())
            visibleSides.add(Direction.SOUTH);

        if (southDelta < 0 && mc.world.getBlockState(bp.north()).isReplaceable())
            visibleSides.add(Direction.NORTH);

        if (upDelta > 0 && mc.world.getBlockState(bp.up()).isReplaceable())
            visibleSides.add(Direction.UP);

        if (downDelta < 0 && mc.world.getBlockState(bp.down()).isReplaceable())
            visibleSides.add(Direction.DOWN);

        return visibleSides;
    }

    public static @Nullable Vec3d getVisibleDirectionPoint(@NotNull Direction dir, @NotNull BlockPos bp, float wallRange, float range) {
        Box brutBox = getDirectionBox(dir);

        // EAST, WEST
        if (brutBox.maxX - brutBox.minX == 0)
            for (double y = brutBox.minY; y < brutBox.maxY; y += 0.1f)
                for (double z = brutBox.minZ; z < brutBox.maxZ; z += 0.1f) {
                    Vec3d point = new Vec3d(bp.getX() + brutBox.minX, bp.getY() + y, bp.getZ() + z);

                    if (shouldSkipPoint(point, bp, dir, wallRange, range))
                        continue;

                    return point;
                }


        // DOWN, UP
        if (brutBox.maxY - brutBox.minY == 0)
            for (double x = brutBox.minX; x < brutBox.maxX; x += 0.1f)
                for (double z = brutBox.minZ; z < brutBox.maxZ; z += 0.1f) {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + brutBox.minY, bp.getZ() + z);

                    if (shouldSkipPoint(point, bp, dir, wallRange, range))
                        continue;

                    return point;
                }


        // NORTH, SOUTH
        if (brutBox.maxZ - brutBox.minZ == 0)
            for (double x = brutBox.minX; x < brutBox.maxX; x += 0.1f)
                for (double y = brutBox.minY; y < brutBox.maxY; y += 0.1f) {
                    Vec3d point = new Vec3d(bp.getX() + x, bp.getY() + y, bp.getZ() + brutBox.minZ);

                    if (shouldSkipPoint(point, bp, dir, wallRange, range))
                        continue;

                    return point;
                }


        return null;
    }

    private static @NotNull Box getDirectionBox(Direction dir) {
        return switch (dir) {
            case UP -> new Box(.15f, 1f, .15f, .85f, 1f, .85f);
            case DOWN -> new Box(.15f, 0f, .15f, .85f, 0f, .85f);

            case EAST -> new Box(1f, .15f, .15f, 1f, .85f, .85f);
            case WEST -> new Box(0f, .15f, .15f, 0f, .85f, .85f);

            case NORTH -> new Box(.15f, .15f, 0f, .85f, .85f, 0f);
            case SOUTH -> new Box(.15f, .15f, 1f, .85f, .85f, 1f);
        };
    }

    private static boolean shouldSkipPoint(Vec3d point, BlockPos bp, Direction dir, float wallRange, float range) {
        RaycastContext context = new RaycastContext(InteractionUtility.getEyesPos(mc.player), point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
        BlockHitResult result = mc.world.raycast(context);

        float dst = InteractionUtility.squaredDistanceFromEyes(point);

        if (result != null
                && result.getType() == HitResult.Type.BLOCK
                && !result.getBlockPos().equals(bp)
                && dst > wallRange * wallRange)
            return true;

        return dst > range * range;
    }

    public static boolean needSneak(Block in) {
        return SHIFT_BLOCKS.contains(in);
    }

    public static void lookAt(BlockPos bp) {
        if (bp != null) {
            float[] angle = calculateAngle(bp.toCenterPos());
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }

    public static boolean isVecInFOV(Vec3d pos, Integer fov) {
        double deltaX = pos.getX() - mc.player.getX();
        double deltaZ = pos.getZ() - mc.player.getZ();
        float yawDelta = MathHelper.wrapDegrees((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0) - MathHelper.wrapDegrees(mc.player.getYaw()));
        return Math.abs(yawDelta) <= fov;
    }

    public record BlockPosWithFacing(BlockPos position, Direction facing) {
    }

    public record BreakData(Direction dir, Vec3d vector) {
    }

    public enum PlaceMode {
        Packet,
        Normal
    }

    public enum Rotate {
        None,
        Default,
        Grim
    }

    public enum Interact {
        Vanilla,
        Strict,
        Legit,
        AirPlace
    }
}

