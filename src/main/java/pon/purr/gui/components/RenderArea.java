package pon.purr.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import pon.purr.Purr;
import pon.purr.modules.ui.Gui;
import pon.purr.utils.ColorUtils;
import pon.purr.utils.math.Hover;
import pon.purr.utils.math.MathUtils;

import java.util.LinkedList;
import java.util.List;

public abstract class RenderArea {
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;

    public List<RenderArea> areas = new LinkedList<>();

    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public final TextRenderer textRenderer = mc.textRenderer;

    private final int lightColor = ColorUtils.fromRGB(
        MathUtils.random(0, 255),
        MathUtils.random(0, 255),
        MathUtils.random(0, 255),
        100
    );

    public final int padding = 2;
    public final int bigPadding = 5;
    public final int vertexRadius = 4;

    public RenderArea() {}

    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.animHandler();

        Gui gui = Purr.moduleManager.getModuleByClass(Gui.class);
        if (gui.showAreas.getValue()) {
            lightArea(context);
        }
    }

    public void lightArea(DrawContext context) {
        context.fill(
            x,
            y,
            x + width,
            y + height,
            lightColor
        );
    }

    public static void lightMouse(DrawContext context, double mouseX, double mouseY) {
        context.fill(
            (int) mouseX,
            (int) mouseY,
            (int) (mouseX + 2),
            (int) (mouseY + 2),
            ColorUtils.fromRGB(255, 255, 255, 100)
        );
    }

    public boolean checkHovered(double mouseX, double mouseY) {
        return Hover.hoverCheck(x, y, width, height, mouseX, mouseY);
    }

    public static boolean checkHovered(int x, int y, int width, int height, double mouseX, double mouseY) {
        return Hover.hoverCheck(x, y, width, height, mouseX, mouseY);
    }
    public static boolean checkHovered(RenderArea ra, double mouseX, double mouseY) {
        return Hover.hoverCheck(ra.x, ra.y, ra.width, ra.height, mouseX, mouseY);
    }

    public void animHandler() {}

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (RenderArea a : areas) {
            if (a.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (RenderArea a : areas) {
            if (a.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }
    public boolean charTyped(char chr, int modifiers) {
        for (RenderArea area : areas) {
            if (area.charTyped(chr, modifiers)) return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (RenderArea area : areas) {
            if (area.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (RenderArea area : areas) {
            if (area.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (RenderArea area : areas) {
            if (area.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        }
        return false;
    }
}
