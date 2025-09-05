package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.TextUtils;
import pon.main.utils.math.AnimHelper;

import java.util.LinkedList;
import java.util.List;

public class StringSetArea extends RenderArea {
    private final Setting<String> set;

    private boolean activate = false;
    private String inputText = "";

    private float lightFactor = 0;
    private boolean light = false;

    public StringSetArea(Setting<String> set, RenderArea parentArea) {
        super(parentArea);
        this.showFactor = set.getVisible() ? 1 : 0;
        this.set = set;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float lowShowFactor = showFactor * parentArea.showFactor;

        String strValue = activate ? inputText + (lightFactor > 0.5f ? "|" : "") : set.getValue();
        strValue = strValue.isEmpty() ? "..." : strValue;
        LinkedList<String> value = TextUtils.splitForRender(strValue, width - padding * 2, s -> textRenderer.getWidth(s));

        height += padding + Render2D.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width,
            padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * lowShowFactor)
        );

        int vHeight = 0;
        for (String s : value) {
            vHeight += textRenderer.fontHeight + padding;
        }

        int color = (int) (0 + (20 * lightFactor));
        Render2D.fill(
            context,
            startX,
            startY + height - padding,
            startX + width,
            startY + height + vHeight,
            ColorUtils.fromRGB(color, color, color, 70 * lowShowFactor),
            vertexRadius, 2
        );

        context.enableScissor(
            startX,
            startY + height - padding,
            startX + width - padding,
            startY + height + vHeight
        );
        height += Render2D.drawTextWithTransfer(
            strValue,
            context,
            textRenderer,
            startX + padding,
            startY + height,
            width - padding * 2,
            padding,
            ColorUtils.fromRGB(255, 255, 255, 255 * lowShowFactor)
        );
        context.disableScissor();

        height += padding;

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (activate) {
            activate = false;
        }
        if (checkHovered(mouseX, mouseY)) {
            activate = true;
            inputText = inputText.isEmpty() ? set.getValue() : inputText;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activate) {
            if (keyCode == GLFW.GLFW_KEY_V && modifiers != 0) {
                inputText += mc.keyboard.getClipboard();
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputText.isEmpty()) {
                inputText = inputText.substring(0, inputText.length() - 1);
            } else if (Main.cancelButtons.contains(keyCode)) {
                activate = false;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                activate = false;
                set.setValue(inputText);
                inputText = "";
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (activate) {
            inputText += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void animHandler() {
        if (activate) {
            if (lightFactor == 1) {
                light = false;
            } else if (lightFactor == 0) {
                light = true;
            }
        } else {
            light = false;
        }
        lightFactor = AnimHelper.handle(light, lightFactor);
        showFactor = AnimHelper.handle(set.getVisible(), showFactor);
    }
}

