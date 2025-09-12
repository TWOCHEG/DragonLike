package pon.main.modules.hud;

import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.events.impl.EventMouseKey;
import pon.main.events.impl.EventMouseMove;
import pon.main.events.impl.EventMouseScroll;
import pon.main.events.impl.EventResizeScreen;
import pon.main.managers.Managers;
import pon.main.modules.Parent;
import pon.main.modules.hud.components.*;
import pon.main.utils.math.MouseUtils;

public class HudModule extends Parent {
    public static final int offset = 5;
    public static final int areasOffset = 1;

    public HudModule() {
        super(null, null);

        HudRenderCallback.EVENT.register((context, tickDelta) -> render(context));
    }

    @EventHandler
    private void onResize(EventResizeScreen e) {
        for (HudArea hudArea : Managers.HUD_MANAGER.hudList()) {
            int[] pos = hudArea.getPos();
            hudArea.setPos(pos[0], pos[1]);
        }
    }

    private boolean isColliding(HudArea a, HudArea b) {
        return a.x < b.x + b.width + areasOffset &&
                a.x + a.width + areasOffset > b.x &&
                a.y < b.y + b.height + areasOffset &&
                a.y + a.height + areasOffset > b.y;
    }

    private int[] resolveCollision(HudArea a, HudArea b) {
        int x = a.x;
        int y = a.y;
        double overlapX = Math.min(a.x + a.width + areasOffset, b.x + b.width + areasOffset) - Math.max(a.x, b.x);
        double overlapY = Math.min(a.y + a.height + areasOffset, b.y + b.height + areasOffset) - Math.max(a.y, b.y);

        if (overlapX < overlapY) {
            if (x < b.x) {
                x -= overlapX;
            } else {
                x += overlapX;
            }
        } else {
            if (y < b.y) {
                y -= overlapY;
            } else {
                y += overlapY;
            }
        }
        return new int[] {x, y};
    }

    public void render(DrawContext context) {
        for (HudArea hudArea : Managers.HUD_MANAGER.hudList()) {
            if (hudArea.getEnable()) {
                double[] mousePos = MouseUtils.getPos();
                int[] pos = hudArea.getRenderPos();

                for (HudArea other : Managers.HUD_MANAGER.hudList()) {
                    if (hudArea.equals(other)) continue;
                    if (isColliding(hudArea, other)) {
                        int[] hudPos = resolveCollision(hudArea, other);
                        pos[0] = hudPos[0];
                        pos[1] = hudPos[1];
                        other.showCollisionBorders();
                    }
                }

                hudArea.render(
                    context,
                    Math.clamp(pos[0], offset, context.getScaledWindowWidth() - hudArea.width - offset),
                    Math.clamp(pos[1], offset, context.getScaledWindowHeight() - hudArea.height - offset),
                    -1, -1,
                    mousePos[0], mousePos[1]
                );
            }
        }
    }

    @EventHandler
    private void onMouseKey(EventMouseKey e) {
        if (e.getAction() == 1) {
            for (HudArea area : Managers.HUD_MANAGER.hudList()) {
                if (area.mouseClicked(e.getX(), e.getY(), e.getButton())) break;
            }
        } else if (e.getAction() == 0) {
            for (HudArea area : Managers.HUD_MANAGER.hudList()) {
                if (area.mouseReleased(e.getX(), e.getY(), e.getButton())) break;
            }
        }
    }

    @EventHandler
    private void onMouseMove(EventMouseMove e) {
        for (HudArea area : Managers.HUD_MANAGER.hudList()) {
            if (area.mouseDragged(e.x, e.y, GLFW.GLFW_MOUSE_BUTTON_LEFT, e.deltaX, e.deltaY)) break;
        }
    }

    @EventHandler
    private void onMouseScroll(EventMouseScroll e) {
        for (HudArea area : Managers.HUD_MANAGER.hudList()) {
            if (area.mouseScrolled(e.getX(), e.getY(), e.getX(), e.getH())) break;
        }
    }
}
