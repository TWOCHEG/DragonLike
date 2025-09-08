package pon.main.modules.hud;

import com.google.gson.internal.LinkedTreeMap;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.events.impl.EventMouseKey;
import pon.main.events.impl.EventMouseMove;
import pon.main.events.impl.EventMouseScroll;
import pon.main.modules.Parent;
import pon.main.modules.hud.components.CordsHud;
import pon.main.modules.hud.components.FPSHud;
import pon.main.modules.hud.components.HudArea;
import pon.main.modules.hud.components.TPSHud;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.MouseUtils;

import java.util.ArrayList;

public class Hud extends Parent {
    public static final ArrayList<HudArea> areas = new ArrayList<>();

    public static final int offset = 5;
    public static final int areasOffset = 1;

    public Hud() {
        super("hud", null);

        areas.add(new FPSHud(this));
        areas.add(new CordsHud(this));
        areas.add(new TPSHud(this));

        for (HudArea hudArea : areas) {
            LinkedTreeMap<String, Object> map = getValue(getName(hudArea), new LinkedTreeMap<>());

            hudArea.setPos((double) map.getOrDefault("x", 300.0), (double) map.getOrDefault("x", 200.0));
        }

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            render(context);
        });
    }

    private boolean isColliding(HudArea a, HudArea b) {
        return a.x < b.x + b.width + areasOffset &&
                a.x + a.width + areasOffset > b.x &&
                a.y < b.y + b.height + areasOffset &&
                a.y + a.height + areasOffset > b.y;
    }

    private void resolveCollision(HudArea a, HudArea b) {
        double overlapX = Math.min(a.x + a.width + areasOffset, b.x + b.width + areasOffset) - Math.max(a.x, b.x);
        double overlapY = Math.min(a.y + a.height + areasOffset, b.y + b.height + areasOffset) - Math.max(a.y, b.y);

        if (overlapX < overlapY) {
            if (a.x < b.x) {
                a.x -= overlapX;
            } else {
                a.x += overlapX;
            }
        } else {
            if (a.y < b.y) {
                a.y -= overlapY;
            } else {
                a.y += overlapY;
            }
        }
    }

    public void render(DrawContext context) {
        for (HudArea hudArea : areas) {
            LinkedTreeMap<String, Object> map = getValue(getName(hudArea), new LinkedTreeMap<>());
            if ((boolean) map.getOrDefault("enable", false) && (map.get("x") != null && map.get("y") != null)) {
                double[] pos = MouseUtils.getPos();

                for (HudArea other : areas) {
                    if (hudArea.equals(other)) continue;
                    if (isColliding(hudArea, other)) {
                        resolveCollision(hudArea, other);
                        context.drawBorder(
                            other.x - areasOffset,
                            other.y - areasOffset,
                            other.width + areasOffset,
                            other.height + areasOffset,
                            ColorUtils.fromRGB(255, 255, 255, 150 * hudArea.draggedFactor)
                        );
                    }
                }

                hudArea.render(
                    context,
                    Math.clamp(hudArea.x, offset, context.getScaledWindowWidth() - hudArea.width - offset),
                    Math.clamp(hudArea.y, offset, context.getScaledWindowHeight() - hudArea.height - offset),
                    -1, -1,
                    pos[0], pos[1]
                );
            }
        }
    }

    public boolean checkEnable(HudArea hudArea) {
        return (boolean) getValue(getName(hudArea), new LinkedTreeMap<>()).getOrDefault("enable", false);
    }

    public void setStatusHud(HudArea hudArea, boolean enable) {
        if (!enable) {
            setValue(getName(hudArea), new LinkedTreeMap<>());
        } else {
            LinkedTreeMap<String, Object> map = getValue(getName(hudArea), new LinkedTreeMap<>());
            map.put("enable", true);
            map.put("x", 300.0);
            map.put("y", 200.0);
            setValue(getName(hudArea), map);
        }
    }

    public void changeHudPos(HudArea area, int x, int y) {
        LinkedTreeMap<String, Object> map = getValue(getName(area), new LinkedTreeMap<>());
        map.put("x", x);
        map.put("y", y);
        setValue(getName(area), map);
    }
    public void changeHudPos(HudArea area, double x, double y) {
        changeHudPos(area, (int) x, (int) y);
    }

    public String getName(HudArea area) {
        return area.getClass().getSimpleName().replaceAll(
            "(?<=[a-z])([A-Z])|(?<=[A-Z])([A-Z][a-z])",
            " $1$2"
        ).trim().toLowerCase();
    }

    @EventHandler
    private void onMouseKey(EventMouseKey e) {
        if (e.getAction() == 1) {
            for (HudArea area : areas) {
                if (area.mouseClicked(e.getX(), e.getY(), e.getButton())) break;
            }
        } else if (e.getAction() == 0) {
            for (HudArea area : areas) {
                if (area.mouseReleased(e.getX(), e.getY(), e.getButton())) break;
            }
        }
    }

    @EventHandler
    private void onMouseMove(EventMouseMove e) {
        for (HudArea area : areas) {
            if (area.mouseDragged(e.x, e.y, GLFW.GLFW_MOUSE_BUTTON_LEFT, e.deltaX, e.deltaY)) break;
        }
    }

    @EventHandler
    private void onMouseScroll(EventMouseScroll e) {
        for (HudArea area : areas) {
            if (area.mouseScrolled(e.getX(), e.getY(), e.getX(), e.getH())) break;
        }
    }
}
