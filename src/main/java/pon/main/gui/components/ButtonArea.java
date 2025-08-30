package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.GetAnimDiff;
import pon.main.utils.render.Render2D;

public class ButtonArea extends RenderArea {
    private Runnable onClick;
    private String name;

    private int targetWidth = 0;

    private boolean clicked = false;
    private float clickedFactor = 0;

    private boolean hovered = false;
    private float hoveredFactor = 0;

    private int color = ColorUtils.fromRGB(255, 255, 255);

    public ButtonArea(RenderArea parentArea, Runnable onClick, String name) {
        super(parentArea);
        this.onClick = onClick;
        this.name = name;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, String name, int targetWidth) {
        super(parentArea);
        this.onClick = onClick;
        this.name = name;
        this.targetWidth = targetWidth;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, String name, int targetWidth, int color) {
        super(parentArea);
        this.onClick = onClick;
        this.name = name;
        this.targetWidth = targetWidth;
        this.color = color;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        hovered = checkHovered(mouseX, mouseY);

        width = targetWidth == 0 ? mc.textRenderer.getWidth(name) + (padding * 2) : targetWidth;
        height = mc.textRenderer.fontHeight + (padding * 2);
        Render2D.fill(
            context, startX, startY,
            startX + width,
            startY + height,
            ColorUtils.fromRGB(0, 0, 0, (80 + (30 * clickedFactor) + (30 * hoveredFactor)) * parentArea.showFactor),
            bigPadding, 2
        );
        context.drawText(
            mc.textRenderer, name, startX + padding, startY + padding,
            ColorUtils.applyOpacity(color, ((200 - (50 * clickedFactor)) * parentArea.showFactor) / 255),
            false
        );

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            clicked = true;
            onClick.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (clicked) {
            clicked = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void animHandler() {
        clickedFactor = AnimHelper.handle(clicked, clickedFactor, GetAnimDiff.get() * 2);
        hoveredFactor = AnimHelper.handle(hovered, hoveredFactor);
    }
}
