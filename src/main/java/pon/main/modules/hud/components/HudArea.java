package pon.main.modules.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import pon.main.gui.HudGui;
import pon.main.gui.components.RenderArea;
import pon.main.managers.client.ConfigManager;
import pon.main.managers.Managers;
import pon.main.modules.client.Gui;
import pon.main.modules.hud.HudModule;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.MouseUtils;
import pon.main.utils.math.Timer;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public abstract class HudArea extends RenderArea {
    protected HudModule hud;

    protected final ConfigManager CONFIG;

    public boolean dragged = false;
    public float draggedFactor = 0;

    public double areaClickX, areaClickY;

    private Timer timer = new Timer();

    private float collisionBorderFactor = 0;
    private boolean collisionBorderShow = false;

    public List<Setting> settings = new LinkedList();

    public HudArea() {
        super();
        this.hud = Managers.MODULE_MANAGER.getModule(HudModule.class);
        this.CONFIG = new ConfigManager(getName());

        int[] savePos = getPos();
        setPos(savePos[0], savePos[1]);
    }

    public void setEnable(boolean enable) {
        CONFIG.set("enable", enable);
    }
    public boolean getEnable() {
        return CONFIG.get("enable", false);
    }

    public void setPos(double x, double y) {
        CONFIG.set("x", x);
        CONFIG.set("y", y);
        setRenderPos((int) x, (int) y);
    }

    public int[] getPos() {
        return new int[]{CONFIG.get("x", 0), CONFIG.get("y", 0)};
    }

    public int[] getRenderPos() {
        return new int[]{x, y};
    }

    public void setRenderPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void onEnableInGui() {
        setEnable(true);
        double[] mousePos = MouseUtils.getPos();
        setRenderPos((int) mousePos[0], (int) mousePos[1]);
        dragged = true;
        areaClickX = mousePos[0] - x;
        areaClickY = mousePos[1] - y;
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        animHandler();
        draggedFactor = AnimHelper.handle(dragged, draggedFactor);

        if (timer.getTimeMs() > 250 && collisionBorderShow) {
            collisionBorderShow = false;
        }
        collisionBorderFactor = AnimHelper.handle(collisionBorderShow, collisionBorderFactor);

        if (Managers.MODULE_MANAGER.getModule(Gui.class).showAreas.getValue()) {
            lightArea(context);
        }

        if (collisionBorderFactor > 0) {
            context.drawBorder(
                x - hud.areasOffset,
                y - hud.areasOffset,
                width + hud.areasOffset,
                height + hud.areasOffset,
                ColorUtils.fromRGB(255, 255, 255, (150 * collisionBorderFactor) * showFactor)
            );
        }

        if (draggedFactor != 0) {
            context.drawBorder(
                (int) (HudModule.offset * draggedFactor), (int) (HudModule.offset * draggedFactor),
                (int) (context.getScaledWindowWidth() - ((HudModule.offset * 2) * draggedFactor)),
                (int) (context.getScaledWindowHeight() - ((HudModule.offset * 2) * draggedFactor)),
                ColorUtils.fromRGB(255, 255, 255, 150 * draggedFactor)
            );
        }
    }

    @Override
    public void lightArea(DrawContext context) {
        context.fill(
            x, y,
            x + width,
            y + height,
            lightColor
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudGui)) {
            areaClickX = mouseX - x;
            areaClickY = mouseY - y;
            dragged = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragged) {
            dragged = false;
            setPos(x, y);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragged) {
            double x = mouseX - areaClickX;
            double y = mouseY - areaClickY;

            setRenderPos((int) x, (int) y);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void showCollisionBorders() {
        timer.reset();
        collisionBorderShow = true;
    }

    public List<Setting> getSettings() {
        if (!settings.isEmpty()) {
            return settings;
        }
        Class<?> currentSuperclass = getClass();

        while (currentSuperclass != null) {
            for (Field field : currentSuperclass.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType()))
                    continue;

                try {
                    field.setAccessible(true);
                    settings.add((Setting) field.get(this));
                } catch (IllegalAccessException ignored) {}
            }

            currentSuperclass = currentSuperclass.getSuperclass();
        }
        settings.forEach(s -> s.init(CONFIG));
        return settings;
    }

    public String getName() {
        return this.getClass().getSimpleName().replaceAll(
                "(?<=[a-z])([A-Z])|(?<=[A-Z])([A-Z][a-z])",
                " $1$2"
        ).trim().toLowerCase();
    }
}
