package pon.purr.managers;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.NotNull;
import pon.purr.Purr;
import pon.purr.events.impl.*;
import pon.purr.modules.Parent;
import pon.purr.utils.math.Timer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pon.purr.modules.Parent.mc;

public final class Core {
    public static boolean lockSprint, serverSprint, hold_mouse0, showSkull;
    public static final Map<String, Identifier> HEADS = new ConcurrentHashMap<>();
    public ArrayList<Packet<?>> silentPackets = new ArrayList<>();
    private final Timer skullTimer = new Timer();
    private final Timer lastPacket = new Timer();
    private final Timer autoSave = new Timer();
    private final Timer setBackTimer = new Timer();

    @EventHandler
    @SuppressWarnings("unused")
    public void onTick(EventPlayerUpdate event) {
        if (Parent.fullNullCheck()) return;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p.isDead() || p.getHealth() == 0)
                Purr.EVENT_BUS.post(new EventDeath(p));
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket && !(e.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly))
            lastPacket.reset();

        if (e.getPacket() instanceof ClientCommandC2SPacket c) {
            if (c.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING || c.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
                if (lockSprint) {
                    e.cancel();
                    return;
                }

                switch (c.getMode()) {
                    case START_SPRINTING -> serverSprint = true;
                    case STOP_SPRINTING -> serverSprint = false;
                }
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (Parent.fullNullCheck()) return;

        if (e.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = e.getPacket();
            if (packet.content().getString().contains("skull")) {
                showSkull = true;
                skullTimer.reset();
                mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_SKELETON_DEATH, SoundCategory.BLOCKS, 1f, 1f);
            }
        }

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            setBackTimer.reset();
        }
    }

    /*
    @EventHandler
    @SuppressWarnings("unused")
    public void onEntitySpawn(EventEntitySpawn e) {
        new ArrayList<>(InteractionUtility.awaiting.keySet()).forEach(bp -> {
            if (e.getEntity() != null && bp.getSquaredDistance(e.getEntity().getPos()) < 4.)
                InteractionUtility.awaiting.remove(bp);
        });
    }*/

//    public void drawSkull(DrawContext e) {
//        if (showSkull && !skullTimer.passedMs(3000) && ClientSettings.skullEmoji.getValue()) {
//            int xPos = (int) (mc.getWindow().getScaledWidth() / 2f - 150);
//            int yPos = (int) (mc.getWindow().getScaledHeight() / 2f - 150);
//            float alpha = (1f - (skullTimer.getPassedTimeMs() / 3000f));
//            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
//            e.drawTexture(TextureStorage.skull, xPos, yPos, 0, 0, 300, 300, 300, 300);
//            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//        } else showSkull = false;
//    }

//    public void drawGps(DrawContext e) {
//        if (ThunderHack.gps_position != null) {
//            float dst = getDistance(ThunderHack.gps_position);
//            float xOffset = mc.getWindow().getScaledWidth() / 2f;
//            float yOffset = mc.getWindow().getScaledHeight() / 2f;
//            float yaw = getRotations(new Vec2f(ThunderHack.gps_position.getX(), ThunderHack.gps_position.getZ())) - mc.player.getYaw();
//            e.getMatrices().translate(xOffset, yOffset, 0.0F);
//            e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
//            e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
//            Render2DEngine.drawTracerPointer(e.getMatrices(), xOffset, yOffset - 50, 12.5f, 0.5f, 3.63f, true, true, HudEditor.getColor(1).getRGB());
//            e.getMatrices().translate(xOffset, yOffset, 0.0F);
//            e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
//            e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
//            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
//            FontRenderers.modules.drawCenteredString(e.getMatrices(), "gps (" + dst + "m)", (float) (Math.sin(Math.toRadians(yaw)) * 50f) + xOffset, (float) (yOffset - (Math.cos(Math.toRadians(yaw)) * 50f)) - 23, -1);
//
//            if (dst < 10)
//                ThunderHack.gps_position = null;
//        }
//    }

    @EventHandler
    public void onMouse(EventMouse event) {
        if (event.getAction() == 0) hold_mouse0 = false;
        if (event.getAction() == 1) hold_mouse0 = true;
    }

    public int getDistance(BlockPos bp) {
        double d0 = mc.player.getX() - bp.getX();
        double d2 = mc.player.getZ() - bp.getZ();
        return (int) (MathHelper.sqrt((float) (d0 * d0 + d2 * d2)));
    }

    public long getSetBackTime() {
        return setBackTimer.getPassedTimeMs();
    }

    public static float getRotations(Vec2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getPos().x;
        double z = vec.y - mc.player.getPos().z;
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

//    public void bobView(MatrixStack matrices, float tickDelta) {
//        if (!(mc.getCameraEntity() instanceof PlayerEntity playerEntity)) {
//            return;
//        }
//
//        float g = -(playerEntity.horizontalSpeed + (playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed) * tickDelta);
//        float h = MathHelper.lerp(tickDelta, playerEntity.prevStrideDistance, playerEntity.strideDistance);
//        matrices.translate(MathHelper.sin(g * (float) Math.PI) * h * 0.1f, -Math.abs(MathHelper.cos(g * (float) Math.PI) * h) * 0.3, 0.0f);
//        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(g * (float) Math.PI) * h * 3.0f));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(g * (float) Math.PI - 0.2f) * h) * 0.3f));
//    }
}
