package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.modules.settings.KeyButton;
import pon.main.utils.ColorUtils;
import pon.main.utils.KeyName;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

public class KeyButtonArea extends RenderArea {
    private final KeyButton keyButton;

    private boolean inputting = false;
    private float inputtingFactor = 0;

    public KeyButtonArea(KeyButton keyButton, RenderArea parentArea) {
        super(parentArea);
        this.showFactor = keyButton.getVisible() ? 1 : 0;
        this.keyButton = keyButton;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float lowShowFactor = showFactor * parentArea.showFactor;

        height += Render2D.drawTextWithTransfer(
            keyButton.getName(), context, textRenderer,
            startX, startY, width, padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * lowShowFactor)
        );
        height += padding;
        String keyName = (keyButton.getValue() != -1 ? KeyName.get(keyButton.getValue()) : "none").toLowerCase();
        keyName = inputtingFactor == 0 ? keyName : AnimHelper.getAnimText(keyName, "...", inputtingFactor);
        Render2D.fill(
            context,
            startX,
            startY + height,
            startX + textRenderer.getWidth(keyName) + (padding * 2),
            startY + height + textRenderer.fontHeight + (padding * 2),
            CategoryArea.makeAColor(100 * lowShowFactor, 0.25f),
            bigPadding, 2
        );
        context.drawText(
            textRenderer,
            keyName,
            startX + padding, startY + height + padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * lowShowFactor),
            false
        );
        height += textRenderer.fontHeight + (padding * 2);

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY)) {
            inputting = true;
            return true;
        } else {
            inputting = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputting) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                keyButton.setValue(-1);
                inputting = false;
            } else if (Main.cancelButtons.contains(keyCode)) {
                inputting = false;
            } else {
                keyButton.setValue(keyCode);
                inputting = false;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(keyButton.getVisible(), showFactor);
        inputtingFactor = AnimHelper.handle(inputting, inputtingFactor);
    }
}
