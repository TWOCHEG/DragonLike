package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonInputArea extends RenderArea {
    private boolean inputting = false;
    private String inputText = "";
    private float inputFactor = 0;
    private float lightFactor = 0;
    private boolean light = false;

    private Builder params;

    public ButtonInputArea(Builder params) {
        super(params.parentArea);
        this.showFactor = 0;
        this.params = params;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        width = width <= 0 ? mc.textRenderer.getWidth(params.name) + (padding * 2) : width;
        height = inputting ? this.height : mc.textRenderer.fontHeight + (padding * 2);

        if (params.bgRenderType.equals(Builder.bgType.full)) {
            int bgColor;
            if (params.backgroundColor) {
                bgColor = ColorUtils.applyOpacity(params.colorProvider.get(), ((80 + (30 * hoveredFactor)) * showFactor) / 255);
            } else {
                bgColor = ColorUtils.fromRGB(0, 0, 0, (80 + (30 * hoveredFactor)) * showFactor);
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
                ColorUtils.fromRGB(0, 0, 0, ((30 * hoveredFactor)) * showFactor)
            );
        }

        if (params.colorProvider != null) {
            params.color = params.colorProvider.get();
        }
        if (params.nameProvider != null) {
            params.name = params.nameProvider.get();
        }
        String input = (inputText.isEmpty() ? "..." : inputText) + (lightFactor > 0.5f ? "|" : "");

        context.enableScissor(startX, startY, startX + width, startY + height);
        context.drawText(
            mc.textRenderer, params.name, (int) (startX + padding - (width * inputFactor)), startY + padding,
            ColorUtils.applyOpacity(params.color, ((200 * (1 - inputFactor)) * parentArea.showFactor) / 255),
            false
        );

        if (inputFactor != 0) {
            height = (padding * 2) + Render2D.drawTextWithTransfer(
                input, context, textRenderer, (int) (startX + padding + (width * (1 - inputFactor))), startY + padding,
                width - (padding * 2), padding, ColorUtils.fromRGB(255, 255, 255, (200 * inputFactor) * parentArea.showFactor)
            );
        }

        context.disableScissor();

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            inputting = true;
            return true;
        } else if (inputting) {
            inputting = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputting) {
            if (keyCode == GLFW.GLFW_KEY_V && modifiers != 0) {
                inputText += mc.keyboard.getClipboard();
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputText.isEmpty()) {
                inputText = inputText.substring(0, inputText.length() - 1);
            } else if (Main.cancelButtons.contains(keyCode)) {
                inputText = "";
                inputting = false;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                inputting = false;
                params.onEnter.accept(inputText);
                parentArea.onProgramEnd();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (inputting) {
            inputText += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void animHandler() {
        if (inputting) {
            if (lightFactor == 1) {
                light = false;
            } else if (lightFactor == 0) {
                light = true;
            }
        } else {
            light = false;
        }
        lightFactor = AnimHelper.handle(light, lightFactor);
        inputFactor = AnimHelper.handle(inputting, inputFactor);
        if (params.showFactorProvider != null) {
            showFactor = params.showFactorProvider.get();
        } else {
            showFactor = parentArea != null ? parentArea.showFactor : 0;
        }
    }

    public static class Builder {
        private Consumer<String> onEnter;

        private String name;
        private Supplier<String> nameProvider;

        private Supplier<Integer> colorProvider;
        private int color = ColorUtils.fromRGB(255, 255, 255);

        private Supplier<Float> showFactorProvider;

        private boolean backgroundColor = false;
        private boolean centered = false;

        private bgType bgRenderType = bgType.full;

        private RenderArea parentArea;

        private String inputHeader;

        public enum bgType {
            full, noBgHover, noBg
        }

        public Builder(String name) {
            this.name = name;
        }
        public Builder() {}

        public ButtonInputArea build() {
            return new ButtonInputArea(this);
        }

        public Builder parentArea(RenderArea parentArea) {
            this.parentArea = parentArea;
            return this;
        }

        public Builder inputHeader(String inputHeader) {
            this.inputHeader = inputHeader;
            return this;
        }

        public Builder bgRenderType(bgType bgRenderType) {
            this.bgRenderType = bgRenderType;
            return this;
        }

        public Builder onEnter(Consumer<String> onEnter) {
            this.onEnter = onEnter;
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

