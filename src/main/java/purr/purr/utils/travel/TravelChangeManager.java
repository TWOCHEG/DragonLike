package purr.purr.utils.travel;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import purr.purr.Purr;
import purr.purr.events.impl.EventChangePlayerLook;
import purr.purr.events.impl.EventPlayerTravel;
import purr.purr.events.impl.EventRotateCamera;
import purr.purr.events.impl.EventTick;
import purr.purr.utils.InputUtils;
import purr.purr.utils.Rotations;
import purr.purr.utils.math.MathUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TravelChangeManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final List<TravelChanger> changers = new CopyOnWriteArrayList<>();
    private final FreeCamData freeCamData = new FreeCamData();
    private float lastYaw;
    private float lastPitch;

    public TravelChangeManager() {
        Purr.EVENT_BUS.subscribe(this);
    }

    public void addChanger(TravelChanger changer) {
        if (!changers.contains(changer)) {
            changers.add(changer);
            filterChangers();
            freeCamData.yaw = Rotations.getCameraYaw();
            freeCamData.pitch = Rotations.getCameraPitch();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public void removeChanger(TravelChanger changer) {
        if (changers.contains(changer)) {
            changers.remove(changer);
            filterChangers();
            if (mc.player != null && mc.world != null) {
                mc.player.setYaw(Rotations.getCameraYaw());
                mc.player.setPitch(Rotations.getCameraPitch());
            }
        }
    }

    public boolean containsChanger(TravelChanger changer) {
        return changers.contains(changer);
    }

    private void filterChangers() {
        changers.sort(Comparator.comparing(TravelChanger::priority));
        Collections.reverse(changers);
    }

    public float getLastYaw() {
        return lastYaw;
    }

    public float getLastPitch() {
        return lastPitch;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCameraRotate(EventRotateCamera e) {
        if (!changers.isEmpty()) {
            e.setRotation(new Vec2f(freeCamData.yaw, freeCamData.pitch));
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onChangePlayer(EventChangePlayerLook e) {
        if (!changers.isEmpty()) {
            e.cancel();
            freeCamData.changeLookDirection(e.cursorDeltaX, e.cursorDeltaY);
        }
    }

    @EventHandler
    @SuppressWarnings({"unused", "DataFlowIssue"})
    public void onPlayerTravel(EventPlayerTravel e) {
        if (!changers.isEmpty()) {
            Float[] rots = changers.getFirst().rotateGetter().get();
            lastYaw = rots[0];
            lastPitch = rots[1];
            mc.player.setYaw(lastYaw);
            mc.player.setPitch(lastPitch);
        }
    }

    @EventHandler
    @SuppressWarnings({"unused", "DataFlowIssue"})
    public void onInputUpdate(EventTick e) {
        if (!changers.isEmpty()) {
            TravelChanger travelChanger = changers.getFirst();
            if (travelChanger.withMoveFix().get())
                moveFix(mc.player.isSneaking(), travelChanger);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void moveFix(boolean sneaking, TravelChanger travelChanger) {
        float forward = (mc.player.input.playerInput.forward() ? 1 : mc.player.input.playerInput.backward() ? -1 : 0);
        float sideways = (mc.player.input.playerInput.left() ? 1 : mc.player.input.playerInput.right() ? -1 : 0);

        Matrix4f matrix = new Matrix4f();
        matrix.rotate((float) Math.toRadians(mc.player.getYaw() - Rotations.getCameraYaw()), 0, 1, 0);
        Vec3d updatedInput = MathUtils.transformPos(matrix, sideways, 0, forward);

        forward = (float) (travelChanger.strongMoveFix().get() ? updatedInput.getZ() : Math.round(updatedInput.getZ())) * (sneaking ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);
        sideways = (float) (travelChanger.strongMoveFix().get() ? updatedInput.getX() : Math.round(updatedInput.getX())) * (sneaking ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);

        InputUtils.setForward(forward > 0.0f);
        InputUtils.setBackward(forward < 0.0f);
        InputUtils.setLeft(sideways > 0.0f);
        InputUtils.setRight(sideways < 0.0f);

        mc.player.travel(new Vec3d(sideways, 0, forward));
    }

    public static class FreeCamData {
        public Vec3d position, lastPosition;

        public float yaw, pitch;

        public void reset() {
            position = mc.gameRenderer.getCamera().getPos();
            lastPosition = position;

            yaw = Rotations.getCameraYaw();
            pitch = Rotations.getCameraPitch();
        }

        public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
            float f = (float)cursorDeltaY * 0.15F;
            float g = (float)cursorDeltaX * 0.15F;
            this.pitch += f;
            this.yaw += g;
            this.pitch = Math.clamp(pitch, -90.0F, 90.0F);
        }
    }
}
