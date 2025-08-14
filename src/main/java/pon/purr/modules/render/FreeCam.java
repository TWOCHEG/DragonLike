package pon.purr.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import pon.purr.Purr;
import pon.purr.events.impl.EventChangePlayerLook;
import pon.purr.events.impl.EventPositionCamera;
import pon.purr.events.impl.EventRotateCamera;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.Setting;

public class FreeCam extends Parent {
    public final Setting<Integer> verticalSpeed = new Setting<>("v Sspeed",10, 1, 100);
    public final Setting<Integer> horizontalSpeed = new Setting<>("h speed", 10, 1, 100);

    private final FreeCamData freeCamData = new FreeCamData();

    public FreeCam() {
        super("free cam", Purr.Categories.render);
        enable = false;
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;
        freeCamData.reset();
        mc.player.input = new FreecamKeyboardInput(mc.options, freeCamData);
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) return;
        mc.player.input = new KeyboardInput(mc.options);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCameraPosition(EventPositionCamera e) {
        if (freeCamData.lastPosition == null) return;
        e.setPosition(freeCamData.lastPosition.lerp(freeCamData.position, e.getTickDelta()));
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCameraRotate(EventRotateCamera e) {
        e.setRotation(new Vec2f(freeCamData.yaw, freeCamData.pitch));
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMouseUpdate(EventChangePlayerLook e) {
        e.cancel();
        freeCamData.changeLookDirection(e.cursorDeltaX, e.cursorDeltaY);
    }

//    @EventHandler
//    @SuppressWarnings("unused")
//    public void onSetOpaqueCube(SetOpaqueCubeEvent e) {
//        e.setCancelled(true);
//    }

    public static class FreecamKeyboardInput extends Input {

        private final GameOptions options;
        private final FreeCamData freeCamData;

        public FreecamKeyboardInput(GameOptions options, FreeCamData freeCamData) {
            this.options = options;
            this.freeCamData = freeCamData;
        }

        @Override
        public void tick() {
            if (fullNullCheck()) return;
            unset();
            FreeCam freeCam = Purr.moduleManager.getModuleByClass(FreeCam.class);

            float hSpeed = freeCam.horizontalSpeed.getValue().floatValue() / 10f;
            float vSpeed = freeCam.verticalSpeed.getValue().floatValue() / 10f;
            float fakeMovementForward = getMovementMultiplier(options.forwardKey.isPressed(), options.backKey.isPressed());
            float fakeMovementSideways = getMovementMultiplier(options.leftKey.isPressed(), options.rightKey.isPressed());
            Vec2f dir = handleVanillaMotion(hSpeed, fakeMovementForward, fakeMovementSideways);

            float y = 0;
            if (options.jumpKey.isPressed()) {
                y += vSpeed;
            } else if (options.sneakKey.isPressed()) {
                y -= vSpeed;
            }

            freeCamData.lastPosition = freeCamData.position;
            freeCamData.position = freeCamData.position.add(dir.x, y, dir.y);
        }

        private void unset() {
            playerInput = new PlayerInput(false, false, false, false, false, false, false);
        }

        private float getMovementMultiplier(boolean positive, boolean negative) {
            if (positive == negative) {
                return 0.0F;
            } else {
                return positive ? 1.0F : -1.0F;
            }
        }

        private Vec2f handleVanillaMotion(final float speed, float forward, float strafe) {
            if (forward == 0.0f && strafe == 0.0f) {
                return Vec2f.ZERO;
            } else if (forward != 0.0f && strafe != 0.0f) {
                forward *= (float) Math.sin(0.7853981633974483);
                strafe *= (float) Math.cos(0.7853981633974483);
            }
            return new Vec2f((float) (forward * speed * -Math.sin(Math.toRadians(freeCamData.yaw)) + strafe * speed * Math.cos(Math.toRadians(freeCamData.yaw))),
                    (float) (forward * speed * Math.cos(Math.toRadians(freeCamData.yaw)) - strafe * speed * -Math.sin(Math.toRadians(freeCamData.yaw))));
        }
    }

    public static class FreeCamData {
        private MinecraftClient mc = MinecraftClient.getInstance();
        public Vec3d position, lastPosition;

        public float yaw, pitch;

        public void reset() {
            if (fullNullCheck()) return;

            position = mc.gameRenderer.getCamera().getPos();
            lastPosition = position;

            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
        }

        public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
            float f = (float)cursorDeltaY * 0.15F;
            float g = (float)cursorDeltaX * 0.15F;
            this.pitch += f;
            this.yaw += g;
            this.pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
        }
    }
}