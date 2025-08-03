package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.Text;
import pon.purr.utils.math.AnimHelper;

import java.util.LinkedList;
import java.util.List;

public class StringSettingsArea extends RenderArea {
    private final Setting<String> set;

    private ModuleArea module = null;
    private SettingsGroupArea group = null;

    private float showPercent = 0f;

    private boolean activate = false;
    private String inputText = "";

    private float lightPercent = 0f;
    private boolean light = false;

    public StringSettingsArea(Setting<String> set, Object o) {
        super();
        this.set = set;
        if (o instanceof ModuleArea m) {
            this.module = m;
        } else if (o instanceof SettingsGroupArea g) {
            this.group = g;
        }
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        String strValue = activate ? inputText + (lightPercent > 0.5f ? "|" : "") : set.getValue();
        strValue = strValue.isEmpty() ? "..." : strValue;
        LinkedList<String> value = Text.splitForRender(strValue, width - padding * 2, s -> textRenderer.getWidth(s));

        height += padding + Render.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width,
            padding,
            RGB.getColor(255, 255, 255, 200 * showPercent)
        );

        int vHeight = 0;
        for (String s : value) {
            vHeight += textRenderer.fontHeight + padding;
        }

        int color = (int) (0 + (20 * lightPercent));
        Render.fill(
            context,
            startX,
            startY + height - padding,
            startX + width,
            startY + height + vHeight,
            RGB.getColor(color, color, color, 70 * showPercent),
            vertexRadius, 2
        );

        context.enableScissor(
            startX,
            startY + height - padding,
            startX + width - padding,
            startY + height + vHeight
        );
        height += Render.drawTextWithTransfer(
            strValue,
            context,
            textRenderer,
            startX + padding,
            startY + height,
            width - padding * 2,
            padding,
            RGB.getColor(255, 255, 255, 255 * showPercent)
        );
        context.disableScissor();

        height += padding;

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
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
        List<Integer> cancelButtons = group != null ? group.module.cancelButtons : module.cancelButtons;
        if (activate) {
            if (keyCode == GLFW.GLFW_KEY_V && modifiers != 0) {
                inputText += mc.keyboard.getClipboard();
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputText.isEmpty()) {
                inputText = inputText.substring(0, inputText.length() - 1);
            } else if (cancelButtons.contains(keyCode)) {
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
            if (lightPercent == 1) {
                light = false;
            } else if (lightPercent == 0) {
                light = true;
            }
        } else {
            light = false;
        }
        lightPercent = AnimHelper.handleAnimValue(!light, lightPercent);
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), module != null ? module.openPercent * module.category.visiblePercent : group.openPercent);
    }
}

