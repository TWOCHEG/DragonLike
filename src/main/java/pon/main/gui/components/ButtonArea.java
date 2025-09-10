package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.GetAnimDiff;
import pon.main.utils.render.Render2D;

import java.util.function.Supplier;

public class ButtonArea extends RenderArea {
    private Builder params;

    private boolean clicked = false;
    private float clickedFactor = 0;

    public ButtonArea(Builder params) {
        super(params.parentArea);
        this.showFactor = 0;
        this.params = params;
    }

    public String getName() {
        return params.name;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        width = width <= 0 ? mc.textRenderer.getWidth(params.name) + (padding * 2) : width;
        height = height <= 0 ? mc.textRenderer.fontHeight + (padding * 2): height;
        if (params.bgRenderType.equals(Builder.bgType.full)) {
            int bgColor;
            if (params.backgroundColor) {
                bgColor = ColorUtils.applyOpacity(params.colorProvider.get(), ((80 + (30 * clickedFactor) + (30 * hoveredFactor)) * showFactor) / 255);
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
        } else if (params.bgRenderType.equals(Builder.bgType.noBgHover)) {
            context.fill(
                startX, startY,
                startX + width,
                startY + height,
                ColorUtils.fromRGB(0, 0, 0, ((30 * clickedFactor) + (30 * hoveredFactor)) * showFactor)
            );
        }
        if (params.colorProvider != null && !params.backgroundColor) {
            params.color = params.colorProvider.get();
        }
        if (params.nameProvider != null) {
            params.name = params.nameProvider.get();
        }
        int textX = params.centered ? startX + ((width / 2) - (mc.textRenderer.getWidth(params.name) / 2)) : startX + padding;
        int textY = params.centered ? startY + ((height / 2) - (mc.textRenderer.fontHeight / 2)) : startY + padding;
        context.drawText(
            mc.textRenderer, params.name, textX, textY,
            ColorUtils.applyOpacity(params.color, ((200 - (50 * clickedFactor)) * showFactor) / 255),
            false
        );

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && showFactor > 0.9f) {
            clicked = true;
            params.onClick.run();
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
        if (params.showFactorProvider != null) {
            showFactor = params.showFactorProvider.get();
        } else {
            showFactor = parentArea != null ? parentArea.showFactor : 0;
        }
        clickedFactor = AnimHelper.handle(clicked, clickedFactor, GetAnimDiff.get() * 2);
    }

    public static class Builder {
        private Runnable onClick;

        private String name;
        private Supplier<String> nameProvider;

        private Supplier<Integer> colorProvider;
        private int color = ColorUtils.fromRGB(255, 255, 255);

        private Supplier<Float> showFactorProvider;

        private boolean backgroundColor = false;
        private boolean centered = false;

        private bgType bgRenderType = bgType.full;

        private RenderArea parentArea;

        public enum bgType {
            full, noBgHover, noBg
        }

        public Builder(String name) {
            this.name = name;
        }
        public Builder() {}

        public ButtonArea build() {
            return new ButtonArea(this);
        }

        public Builder parentArea(RenderArea parentArea) {
            this.parentArea = parentArea;
            return this;
        }

        public Builder centered(boolean centered) {
            this.centered = centered;
            return this;
        }

        public Builder bgRenderType(bgType bgRenderType) {
            this.bgRenderType = bgRenderType;
            return this;
        }

        public Builder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder nameProvider(Supplier<String> nameProvider) {
            this.nameProvider = nameProvider;
            return this;
        }

        public Builder colorProvider(Supplier<Integer> colorProvider) {
            return colorProvider(colorProvider, false);
        }
        public Builder colorProvider(Supplier<Integer> colorProvider, boolean backgroundColor) {
            this.colorProvider = colorProvider;
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder color(int color) {
            return color(color, backgroundColor);
        }
        public Builder color(int color, boolean backgroundColor) {
            this.color = color;
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder showFactorProvider(Supplier<Float> showFactorProvider) {
            this.showFactorProvider = showFactorProvider;
            return this;
        }
    }
}
