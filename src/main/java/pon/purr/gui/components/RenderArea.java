package pon.purr.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import pon.purr.utils.math.Hover;

public abstract class RenderArea {
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;

    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public final TextRenderer textRenderer = mc.textRenderer;

    public RenderArea() {}

    public void onRender(DrawContext context, int startX, int startY, int width, int height, float visiblePercent, double mouseX, double mouseY) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;

        this.animHandler();
    }

    public boolean checkHovered(double mouseX, double mouseY) {
        return Hover.hoverCheck(x, y, width, height, mouseX, mouseY);
    }

    public void animHandler() {}
}
