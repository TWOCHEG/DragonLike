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
    private String name;

    private Supplier<String> nameProvider;
    private Supplier<Integer> colorProvider;
    private Supplier<Float> showFactorProvider;
    private Consumer<String> onFinal;

    private boolean inputting = false;
    private String inputText = "";
    private float inputFactor = 0;
    private float lightFactor = 0;
    private boolean light = false;

    private int color = ColorUtils.fromRGB(255, 255, 255);

    public ButtonInputArea(RenderArea parentArea, Consumer<String> onFinal, String name) {
        super(parentArea);
        this.onFinal = onFinal;
        this.name = name;
    }
    public ButtonInputArea(RenderArea parentArea, Consumer<String> onFinal, String name, int color) {
        super(parentArea);
        this.onFinal = onFinal;
        this.name = name;
        this.color = color;
    }
    public ButtonInputArea(RenderArea parentArea, Consumer<String> onFinal, Supplier<String> nameProvider) {
        super(parentArea);
        this.onFinal = onFinal;
        this.nameProvider = nameProvider;
    }
    public ButtonInputArea(RenderArea parentArea, Consumer<String> onFinal, Supplier<String> nameProvider, Supplier<Integer> colorProvider) {
        super(parentArea);
        this.onFinal = onFinal;
        this.nameProvider = nameProvider;
        this.colorProvider = colorProvider;
    }
    public ButtonInputArea(RenderArea parentArea, Consumer<String> onFinal, Supplier<String> nameProvider, Supplier<Integer> colorProvider, Supplier<Float> showFactorProvider) {
        super(parentArea);
        this.onFinal = onFinal;
        this.nameProvider = nameProvider;
        this.colorProvider = colorProvider;
        this.showFactorProvider = showFactorProvider;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        width = width <= 0 ? mc.textRenderer.getWidth(name) + (padding * 2) : width;
        height = inputting ? this.height : mc.textRenderer.fontHeight + (padding * 2);

        Render2D.fill(
            context, startX, startY,
            startX + width,
            startY + height,
            ColorUtils.fromRGB(0, 0, 0, (80 + (30 * hoveredFactor)) * parentArea.showFactor),
            bigPadding, 2
        );
        if (colorProvider != null) {
            color = colorProvider.get();
        }
        if (nameProvider != null) {
            name = nameProvider.get();
        }
        String input = (inputText.isEmpty() ? "..." : inputText) + (lightFactor > 0.5f ? "|" : "");

        context.enableScissor(startX, startY, startX + width, startY + height);
        context.drawText(
            mc.textRenderer, name, (int) (startX + padding - (width * inputFactor)), startY + padding,
            ColorUtils.applyOpacity(color, ((200 * (1 - inputFactor)) * parentArea.showFactor) / 255),
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
                onFinal.accept(inputText);
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
        if (showFactorProvider != null) {
            showFactor = showFactorProvider.get();
        } else {
            showFactor = parentArea.showFactor;
        }
    }
}

