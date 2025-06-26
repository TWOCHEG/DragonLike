package purr.purr.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import purr.purr.events.impl.EventPositionCamera;
import purr.purr.events.impl.EventRotateCamera;
import purr.purr.modules.Parent;
import purr.purr.modules.settings.*;

public class ModifyCamera extends Parent {
    public final Setting<Boolean> rewriteDistance = new Setting<>("rewrite distance", true);
    public final Setting<Float> distance = new Setting<>("distance", 7f, 2.5f, 40f).visibleIf(m -> rewriteDistance.getValue());

    public final Setting<Boolean> rewritePosition = new Setting<>("rewrite position", false);
    public final Setting<Integer> posX = new Setting<>("position X", 0, -30, 30).visibleIf(m -> rewritePosition.getValue());
    public final Setting<Integer> posY = new Setting<>("position Y", 0, -30, 30).visibleIf(m -> rewritePosition.getValue());
    public final Setting<Integer> posZ = new Setting<>("position Z", 0, -30, 30).visibleIf(m -> rewritePosition.getValue());

    public final Setting<Boolean> rewriteRotation = new Setting<>("rewrite rotation", false);
    public final Setting<Integer> yaw = new Setting<>("camera yaw", 0, -180, 180).visibleIf(m -> rewriteRotation.getValue());
    public final Setting<Integer> pitch = new Setting<>("camera pitch", 0, -90, 90).visibleIf(m -> rewriteRotation.getValue());

    public ModifyCamera() {
        super("modify camera", "render");
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCameraPos(EventPositionCamera e) {
        if (rewritePosition.getValue()) {
            e.setPosition(new Vec3d(e.getX() + posX.getValue(), e.getY() + posY.getValue(), e.getZ() + posZ.getValue()));
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCameraRotation(EventRotateCamera e) {
        if (rewriteRotation.getValue()) {
            e.setRotation(new Vec2f(yaw.getValue().floatValue(), pitch.getValue().floatValue()));
        }
    }
}
