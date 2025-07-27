package pon.purr.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import pon.purr.utils.math.Hover;

public abstract class RenderArea {
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;

    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public final TextRenderer textRenderer = mc.textRenderer;

    public RenderArea() {}

    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;

        this.animHandler();
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
        return false;
    }
}
