package purr.purr.modules.Player;

import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import purr.purr.Purr;
import purr.purr.events.impl.EventChangePlayerLook;
import purr.purr.events.impl.EventPositionCamera;
import purr.purr.events.impl.EventRotateCamera;
import purr.purr.events.impl.EventSetOpaqueCube;
import purr.purr.modules.Parent;
import purr.purr.modules.settings.*;
import purr.purr.utils.RotateUtils;

public class FreeCam extends Parent {
    public final Setting<Integer> verticalSpeed = new Setting<>("vertical speed", 1, 1, 100);
    public final Setting<Integer> horizontalSpeed = new Setting<>("horizontal Speed", 1, 1, 100);

    private final FreeCamData freeCamData = new FreeCamData();

    private Input originalInput;

    public FreeCam() {
        super("freecam", "player");
        enable = false;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        originalInput = client.player.input;
        freeCamData.reset();
        client.player.input = new FreecamKeyboardInput(client.options, freeCamData);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onDisable() {
        client.player.input = originalInput;
        client.gameRenderer.getCamera().update(client.world, client.player, false, false, 0);
    }

    @Override
    public void setEnable(boolean value) {
        if (client.world == null || client.player == null) return;
        super.setEnable(value);
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

    @EventHandler
    @SuppressWarnings("unused")
    public void onSetOpaqueCube(EventSetOpaqueCube e) {
        if (enable) e.cancel();
    }

    public static class FreecamKeyboardInput extends Input {
        private final GameOptions options;
        private final FreeCamData freeCamData;

        public FreecamKeyboardInput(GameOptions options, FreeCamData freeCamData) {
            this.options = options;
            this.freeCamData = freeCamData;

            WorldRenderEvents.START.register(context -> {
                freeCam();
            });
        }

        public void freeCam() {
            unset();
            FreeCam freeCam = (FreeCam) Purr.moduleManager.getModuleByClass(FreeCam.class);
            if (freeCam == null) return;

            float hSpeed = freeCam.horizontalSpeed.getValue().floatValue() / 20f;
            float vSpeed = freeCam.verticalSpeed.getValue().floatValue() / 20f;
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
        public Vec3d position, lastPosition;

        public float yaw, pitch;

        public void reset() {
            position = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            lastPosition = position;

            yaw = RotateUtils.getCameraYaw();
            pitch = RotateUtils.getCameraPitch();
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

