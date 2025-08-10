package pon.purr.modules.render;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;
import pon.purr.Purr;
import pon.purr.events.impl.*;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.player.InputUtils;
import pon.purr.utils.player.MovementUtility;
import pon.purr.utils.render.Render2DEngine;
import pon.purr.utils.render.Render3DEngine;

public class FreeCam extends Parent {
    public FreeCam() {
        super("free cam", Purr.Categories.render);
    }

    private final Setting<Float> speed = new Setting<>("h speed", 1f, 0.1f, 3f);
    private final Setting<Float> hspeed = new Setting<>("v speed", 0.42f, 0.1f, 3f);
    private final Setting<Boolean> freeze = new Setting<>("freeze", false);
    public final Setting<Boolean> track = new Setting<>("track", false);

    private float fakeYaw, fakePitch, prevFakeYaw, prevFakePitch, prevScroll;
    private double fakeX, fakeY, fakeZ, prevFakeX, prevFakeY, prevFakeZ;
    public LivingEntity trackEntity;

    @Override
    public void onEnable() {
        mc.chunkCullingEnabled = false;
        trackEntity = null;

        fakePitch = mc.player.getPitch();
        fakeYaw = mc.player.getYaw();

        prevFakePitch = fakePitch;
        prevFakeYaw = fakeYaw;

        fakeX = mc.player.getX();
        fakeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        fakeZ = mc.player.getZ();

        prevFakeX = mc.player.getX();
        prevFakeY = mc.player.getY();
        prevFakeZ = mc.player.getZ();
    }

    @EventHandler
    public void onAttack(EventAttack e) {
        if (!e.isPre() && e.getEntity() instanceof LivingEntity entity && track.getValue())
            trackEntity = entity;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) return;
        mc.chunkCullingEnabled = true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSync(EventSync e) {
        prevFakeYaw = fakeYaw;
        prevFakePitch = fakePitch;

        if (isKeyPressed(GLFW.GLFW_KEY_ESCAPE) || isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT))
            trackEntity = null;

        if (trackEntity != null) {
            fakeYaw = trackEntity.getYaw();
            fakePitch = trackEntity.getPitch();

            prevFakeX = fakeX;
            prevFakeY = fakeY;
            prevFakeZ = fakeZ;

            fakeX = trackEntity.getX();
            fakeY = trackEntity.getY() + trackEntity.getEyeHeight(trackEntity.getPose());
            fakeZ = trackEntity.getZ();
        } else {
            fakeYaw = mc.player.getYaw();
            fakePitch = mc.player.getPitch();
        }
    }


    @EventHandler
    public void onKeyboardInput(EventKeyboardInput e) {
        if (mc.player == null) return;

        if (trackEntity == null) {
            double[] motion = MovementUtility.forward(speed.getValue());

            prevFakeX = fakeX;
            prevFakeY = fakeY;
            prevFakeZ = fakeZ;

            fakeX += motion[0];
            fakeZ += motion[1];

            if (mc.options.jumpKey.isPressed())
                fakeY += hspeed.getValue();

            if (mc.options.sneakKey.isPressed())
                fakeY -= hspeed.getValue();
        }

        InputUtils.setBackward(false);
        InputUtils.setLeft(false);
        InputUtils.setRight(false);
        InputUtils.setForward(false);
        InputUtils.setJumping(false);
        InputUtils.setSneaking(false);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onMove(EventMove e) {
        if (freeze.getValue()) {
            e.setX(0.);
            e.setY(0.);
            e.setZ(0.);
            e.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (freeze.getValue() && e.getPacket() instanceof PlayerMoveC2SPacket)
            e.cancel();
    }

    @EventHandler
    public void onScroll(EventMouse e) {
        if (e.getAction() == 2) {
            if (e.getButton() > 0) speed.setValue(speed.getValue() + 0.05f);
            else speed.setValue(speed.getValue() - 0.05f);
            prevScroll = e.getButton();
        }
    }


    public float getFakeYaw() {
        return (float) Render2DEngine.interpolate(prevFakeYaw, fakeYaw, Render3DEngine.getTickDelta());
    }

    public float getFakePitch() {
        return (float) Render2DEngine.interpolate(prevFakePitch, fakePitch, Render3DEngine.getTickDelta());
    }

    public double getFakeX() {
        return Render2DEngine.interpolate(prevFakeX, fakeX, Render3DEngine.getTickDelta());
    }

    public double getFakeY() {
        return Render2DEngine.interpolate(prevFakeY, fakeY, Render3DEngine.getTickDelta());
    }

    public double getFakeZ() {
        return Render2DEngine.interpolate(prevFakeZ, fakeZ, Render3DEngine.getTickDelta());
    }
}
