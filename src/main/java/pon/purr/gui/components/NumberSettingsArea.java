package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

import java.util.List;

public class NumberSettingsArea extends RenderArea {
    private final Setting<Number> set;

    private ModuleArea module = null;
    private SettingsGroupArea group = null;

    private float showPercent = 0f;

    private int titleHeight = 0;

    private boolean activate = false;
    private String inputText = "";
    private float lightPercent = 0f;
    private boolean light = false;

    private boolean dragged = false;
    private float draggedPercent = 0f;

    private float delta = 0f;

    private Number oldValue;

    public NumberSettingsArea(Setting<Number> set, Object o) {
        super();
        this.set = set;
        if (o instanceof ModuleArea m) {
            this.module = m;
        } else if (o instanceof SettingsGroupArea g) {
            this.group = g;
        }

        oldValue = set.min;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float alphaPercent = showPercent * (
            module != null ?
                module.openPercent * module.category.visiblePercent :
                group.openPercent * group.module.category.visiblePercent
        );

        String value = activate ? inputText : set.getValue().toString();
        int valueWidth = textRenderer.getWidth(value);
        titleHeight = Render.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width - (valueWidth - padding * 2) - bigPadding,
            padding,
            RGB.getColor(255, 255, 255, 200 * alphaPercent)
        );
        height += titleHeight;
        Render.fill(
            context,
            startX + width - (valueWidth + padding * 2),
            startY + (height / 2 - (titleHeight + padding * 2) / 2),
            startX + width,
            startY + (height / 2 + (titleHeight + padding * 2) / 2),
            RGB.getColor(0, 0, 0, 70 * alphaPercent),
            vertexRadius,
            2
        );
        context.drawText(
            textRenderer,
            value,
            startX + width - (valueWidth + padding),
            startY + (height / 2 - (titleHeight + padding * 2) / 2) + padding,
            RGB.getColor(255, 255, 255, (200 - (100 * lightPercent)) * alphaPercent),
            false
        );
        height += padding;
        int sHeight = vertexRadius + 2;
        float normalizedValue = (set.getValue().floatValue() - set.min.floatValue()) /
                (set.max.floatValue() - set.min.floatValue());
        System.out.println(normalizedValue);
        float normalizedOldValue = (oldValue.floatValue() - set.min.floatValue()) /
                (set.max.floatValue() - set.min.floatValue());
        float diffPercent = MathHelper.lerp(delta, normalizedOldValue, normalizedValue);
        Render.fill(
            context,
            startX,
            startY + height,
            startX + width,
            startY + height + sHeight,
            RGB.getColor(100, 100, 100, (70 + 50 * draggedPercent) * alphaPercent),
            vertexRadius / 2, 2
        );
        int panelWidth = (int) ((width - 1) * diffPercent);
        Render.fill(
            context,
            startX + 1,
            startY + height + 1,
            startX + (panelWidth <= vertexRadius + 1 ? vertexRadius + 1 : panelWidth),
            startY + height + sHeight - 1,
            RGB.getColor(200, 200, 200, 70 * alphaPercent),
            vertexRadius / 2, 2
        );
        height += sHeight;

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (activate) {
            activate = false;
        }
        if (checkHovered(x, y, width, titleHeight, mouseX, mouseY)) {
            activate = true;
            inputText = inputText.isEmpty() ? set.getValue().toString() : inputText;
            return true;
        } else if (checkHovered(x, y + titleHeight, width, height - titleHeight, mouseX, mouseY)) {
            setValue(mouseX);
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
                oldValue = set.getValue();
                if (oldValue instanceof Float) {
                    float v;
                    try {
                        v = Float.parseFloat(inputText.replace(",", ".").replace(" ", "."));
                    } catch (Exception e) {
                        v = (float) oldValue;
                    }
                    set.setValue(Math.clamp(v, set.min.floatValue(), set.max.floatValue()));
                } else if (oldValue instanceof Integer) {
                    int v;
                    try {
                        v = Integer.parseInt(inputText.replace(",", ".").replace(" ", "."));
                    } catch (Exception e) {
                        v = (int) oldValue;
                    }
                    set.setValue(Math.clamp(v, set.min.intValue(), set.max.intValue()));
                }
                delta = 0;
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
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), showPercent);
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
        draggedPercent = AnimHelper.handleAnimValue(!dragged, draggedPercent);
        delta = AnimHelper.handleAnimValue(false, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragged) {
            dragged = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragged || checkHovered(x, y + titleHeight, width, height - titleHeight, mouseX, mouseY)) {
            dragged = true;

            setValue(mouseX);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void setValue(double mouseX) {
        oldValue = set.getValue();

        float percent = (float) ((mouseX - x) / width);
        float rawValue = set.min.floatValue() + (set.max.floatValue() - set.min.floatValue()) * percent;
        boolean integer = set.defaultValue instanceof Integer;

        if (integer) {
            int clampedValue = MathHelper.clamp(Math.round(rawValue), set.min.intValue(), set.max.intValue());
            set.setValue(clampedValue);
        } else {
            int decimalPlaces = 0;
            String defaultValueStr = set.defaultValue.toString();
            int dotIndex = defaultValueStr.indexOf('.');

            if (dotIndex != -1) {
                decimalPlaces = defaultValueStr.length() - dotIndex - 1;
            }

            float factor = (float) Math.pow(10, decimalPlaces);
            float roundedValue = Math.round(rawValue * factor) / factor;
            float clampedValue = MathHelper.clamp(roundedValue, set.min.floatValue(), set.max.floatValue());
            set.setValue(clampedValue);
        }
        if (!set.getValue().equals(oldValue)) {
            delta = 0f;
        }
    }
}
