package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.GetAnimDiff;
import pon.main.utils.render.Render2D;

import java.awt.*;
import java.util.function.Supplier;

public class ButtonArea extends RenderArea {
    private Runnable onClick;
    private String name;

    private Supplier<String> nameProvider;
    private Supplier<Integer> colorProvider;
    private Supplier<Float> showFactorProvider;
    private boolean backgroundColor = false;
    private boolean centered = false;

    private boolean clicked = false;
    private float clickedFactor = 0;
    private int color = ColorUtils.fromRGB(255, 255, 255);
    // самый ебанутый ряд конструкторов
    public ButtonArea(RenderArea parentArea, Runnable onClick, String name) {
        super(parentArea);
        this.onClick = onClick;
        this.name = name;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, String name, Supplier<Float> showFactorProvider) {
        super(parentArea);
        this.onClick = onClick;
        this.name = name;
        this.showFactorProvider = showFactorProvider;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, String name, int color) {
        super(parentArea);
        this.onClick = onClick;
        this.name = name;
        this.color = color;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, Supplier<String> nameProvider) {
        super(parentArea);
        this.onClick = onClick;
        this.nameProvider = nameProvider;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, Supplier<String> nameProvider, Supplier<Integer> colorProvider) {
        super(parentArea);
        this.onClick = onClick;
        this.nameProvider = nameProvider;
        this.colorProvider = colorProvider;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, Supplier<String> nameProvider, Supplier<Integer> colorProvider, Supplier<Float> showFactorProvider) {
        super(parentArea);
        this.onClick = onClick;
        this.nameProvider = nameProvider;
        this.colorProvider = colorProvider;
        this.showFactorProvider = showFactorProvider;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, Supplier<String> nameProvider, Supplier<Integer> colorProvider, Supplier<Float> showFactorProvider, boolean backgroundColor) {
        super(parentArea);
        this.onClick = onClick;
        this.nameProvider = nameProvider;
        this.colorProvider = colorProvider;
        this.showFactorProvider = showFactorProvider;
        this.backgroundColor = backgroundColor;
    }
    public ButtonArea(RenderArea parentArea, Runnable onClick, Supplier<String> nameProvider, Supplier<Integer> colorProvider, Supplier<Float> showFactorProvider, boolean backgroundColor, boolean centered) {
        super(parentArea);
        this.onClick = onClick;
        this.nameProvider = nameProvider;
        this.colorProvider = colorProvider;
        this.showFactorProvider = showFactorProvider;
        this.backgroundColor = backgroundColor;
        this.centered = centered;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        width = width <= 0 ? mc.textRenderer.getWidth(name) + (padding * 2) : width;
        height = height <= 0 ? mc.textRenderer.fontHeight + (padding * 2): height;
        int bgColor;
        if (backgroundColor) {
            bgColor = ColorUtils.applyOpacity(colorProvider.get(), ((80 + (30 * clickedFactor) + (30 * hoveredFactor)) * showFactor) / 255);
        } else {
            bgColor = ColorUtils.fromRGB(0, 0, 0, (80 + (30 * clickedFactor) + (30 * hoveredFactor)) * showFactor);
        }
        Render2D.fill(
            context, startX, startY,
            startX + width,
            startY + height,
            bgColor,
            bigPadding, 2
        );
        if (colorProvider != null && !backgroundColor) {
            color = colorProvider.get();
        }
        if (nameProvider != null) {
            name = nameProvider.get();
        }
        int textX = centered ? startX + ((width / 2) - (mc.textRenderer.getWidth(name) / 2)) : startX + padding;
        int textY = centered ? startY + ((height / 2) - (mc.textRenderer.fontHeight / 2)) : startY + padding;
        context.drawText(
            mc.textRenderer, name, textX, textY,
            ColorUtils.applyOpacity(color, ((200 - (50 * clickedFactor)) * showFactor) / 255),
            false
        );

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && showFactor > 0.9f) {
            clicked = true;
            onClick.run();
            parentArea.onProgramEnd();
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
        if (showFactorProvider != null) {
            showFactor = showFactorProvider.get();
        } else {
            showFactor = parentArea.showFactor;
        }
        clickedFactor = AnimHelper.handle(clicked, clickedFactor, GetAnimDiff.get() * 2);
    }
}
