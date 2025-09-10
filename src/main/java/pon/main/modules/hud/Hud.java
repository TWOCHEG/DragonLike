package pon.main.modules.hud;

import com.google.gson.internal.LinkedTreeMap;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.events.impl.EventMouseKey;
import pon.main.events.impl.EventMouseMove;
import pon.main.events.impl.EventMouseScroll;
import pon.main.events.impl.EventResizeScreen;
import pon.main.modules.Parent;
import pon.main.modules.hud.components.*;
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
        areas.add(new ArmorHud(this));

        for (HudArea hudArea : areas) {
            LinkedTreeMap<String, Object> map = getValue(getName(hudArea), new LinkedTreeMap<>());

            hudArea.setPos((double) map.getOrDefault("x", 300.0), (double) map.getOrDefault("x", 200.0));
        }

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            render(context);
        });
    }

    @EventHandler
    private void onResize(EventResizeScreen e) {
        for (HudArea hudArea : areas) {
            LinkedTreeMap<String, Object> map = getValue(getName(hudArea), new LinkedTreeMap<>());

            hudArea.setPos((double) map.getOrDefault("x", 300.0), (double) map.getOrDefault("x", 200.0));
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
        for (HudArea hudArea : areas) {
            LinkedTreeMap<String, Object> map = getValue(getName(hudArea), new LinkedTreeMap<>());
            if ((boolean) map.getOrDefault("enable", false) && (map.get("x") != null && map.get("y") != null)) {
                double[] pos = MouseUtils.getPos();
                int x = hudArea.x;
                int y = hudArea.y;

                for (HudArea other : areas) {
                    if (hudArea.equals(other)) continue;
                    if (isColliding(hudArea, other)) {
                        int[] hudPos = resolveCollision(hudArea, other);
                        x = hudPos[0];
                        y = hudPos[1];
                        other.showColisionBorders();
                    }
                }

                hudArea.render(
                    context,
                    Math.clamp(x, offset, context.getScaledWindowWidth() - hudArea.width - offset),
                    Math.clamp(y, offset, context.getScaledWindowHeight() - hudArea.height - offset),
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
