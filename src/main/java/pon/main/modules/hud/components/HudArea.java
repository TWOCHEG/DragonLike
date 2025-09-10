package pon.main.modules.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import pon.main.Main;
import pon.main.gui.HudGui;
import pon.main.gui.components.RenderArea;
import pon.main.managers.ConfigManager;
import pon.main.modules.client.Gui;
import pon.main.modules.hud.Hud;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.Timer;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public abstract class HudArea extends RenderArea {
    protected Hud hud;

    protected final ConfigManager CONFIG;

    public boolean dragged = false;
    public float draggedFactor = 0;

    public double areaClickX, areaClickY;

    private Timer timer = new Timer();

    private float colisionBorderFactor = 0;
    private boolean colisionBorderShow = false;

    public List<Setting> settings = new LinkedList();

    public HudArea(Hud hud) {
        super();
        this.hud = hud;
        this.CONFIG = new ConfigManager(hud.getName(this));
    }

    public void setPos(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        animHandler();
        draggedFactor = AnimHelper.handle(dragged, draggedFactor);

        if (timer.getTimeMs() > 250 && colisionBorderShow) {
            colisionBorderShow = false;
        }
        colisionBorderFactor = AnimHelper.handle(colisionBorderShow, colisionBorderFactor);

        if (Main.MODULE_MANAGER.getModule(Gui.class).showAreas.getValue()) {
            lightArea(context);
        }

        if (colisionBorderFactor > 0) {
            context.drawBorder(
                x - hud.areasOffset,
                y - hud.areasOffset,
                width + hud.areasOffset,
                height + hud.areasOffset,
                ColorUtils.fromRGB(255, 255, 255, (150 * colisionBorderFactor) * showFactor)
            );
        }

        if (draggedFactor != 0) {
            context.drawBorder(
                (int) (Hud.offset * draggedFactor), (int) (Hud.offset * draggedFactor),
                (int) (context.getScaledWindowWidth() - ((Hud.offset * 2) * draggedFactor)),
                (int) (context.getScaledWindowHeight() - ((Hud.offset * 2) * draggedFactor)),
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
            hud.changeHudPos(this, x, y);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragged) {
            double x = mouseX - areaClickX;
            double y = mouseY - areaClickY;

            setPos(x, y);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void showColisionBorders() {
        timer.reset();
        colisionBorderShow = true;
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
}
