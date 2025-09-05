package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;

import java.util.List;

public class NumberSetArea extends RenderArea {
    private final Setting<Number> set;

    private int titleHeight = 0;

    private boolean inputting = false;
    private String inputText = "";
    private float lightFactor = 0;
    private boolean light = false;

    private boolean dragged = false;
    private float draggedFactor = 0;

    private float lineFactor = 0;

    private float delta = 0;

    private Number oldValue;

    public NumberSetArea(Setting<Number> set, RenderArea parentArea) {
        super(parentArea);
        this.showFactor = set.getVisible() ? 1 : 0;
        this.set = set;

        oldValue = set.min;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float lowShowFactor = showFactor * parentArea.showFactor;

        String value = inputting ? inputText : set.getValue().toString();
        int valueWidth = textRenderer.getWidth(value);
        titleHeight = Render2D.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width - (valueWidth - padding * 2) - bigPadding,
            padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * lowShowFactor)
        );
        height += titleHeight;
        Render2D.fill(
            context,
            startX + width - (valueWidth + padding * 2),
            startY + (height / 2 - (titleHeight + padding * 2) / 2),
            startX + width,
            startY + (height / 2 + (titleHeight + padding * 2) / 2),
            CategoryArea.makeAColor(70 * lowShowFactor, 0.25f),
            vertexRadius, 2
        );
        context.drawText(
            textRenderer,
            value,
            startX + width - (valueWidth + padding),
            startY + (height / 2 - (titleHeight + padding * 2) / 2) + padding,
            ColorUtils.fromRGB(255, 255, 255, (200 - (100 * lightFactor)) * lowShowFactor),
            false
        );
        height += padding;
        int sHeight = vertexRadius + 2;
        float normalizedValue = (set.getValue().floatValue() - set.min.floatValue()) /
                (set.max.floatValue() - set.min.floatValue());
        float normalizedOldValue = (oldValue.floatValue() - set.min.floatValue()) /
                (set.max.floatValue() - set.min.floatValue());
        lineFactor = MathHelper.lerp(delta, normalizedOldValue, normalizedValue);
        Render2D.fill(
            context,
            startX,
            startY + height,
            startX + width,
            startY + height + sHeight,
            ColorUtils.fromRGB(100, 100, 100, (70 + 50 * draggedFactor) * lowShowFactor),
            vertexRadius / 2, 2
        );
        int panelWidth = (int) ((width - 1) * lineFactor);
        Render2D.fill(
            context,
            startX + 1,
            startY + height + 1,
            startX + (panelWidth <= vertexRadius + 1 ? vertexRadius + 1 : panelWidth),
            startY + height + sHeight - 1,
            ColorUtils.fromRGB(200, 200, 200, 70 * lowShowFactor),
            vertexRadius / 2, 2
        );
        height += sHeight;

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inputting) {
            inputting = false;
        }
        if (checkHovered(x, y, width, titleHeight, mouseX, mouseY)) {
            inputting = true;
            inputText = inputText.isEmpty() ? set.getValue().toString() : inputText;
        } if (checkHovered(x, y + titleHeight, width, height - titleHeight, mouseX, mouseY)) {
            dragged = true;
            mouseSetValue(mouseX);
            return true;
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
                inputting = false;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                inputting = false;
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
        if (inputting) {
            inputText += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(set.getVisible(), showFactor);
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
        draggedFactor = AnimHelper.handle(dragged, draggedFactor);
        delta = AnimHelper.handle(true, delta);
        showFactor = AnimHelper.handle(set.getVisible(), showFactor);
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
            mouseSetValue(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (checkHovered(mouseX, mouseY)) {
            scrollSetValue(scrollY > 0 ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void mouseSetValue(double mouseX) {
        float percent = (float) ((mouseX - x) / width);
        float rawValue = set.min.floatValue() + (set.max.floatValue() - set.min.floatValue()) * percent;
        boolean integer = set.defaultValue instanceof Integer;

        if (integer) {
            int clampedValue = MathHelper.clamp(Math.round(rawValue), set.min.intValue(), set.max.intValue());
            lowSetValue(clampedValue);
        } else {
            int decimalPlaces = 0;
            String defaultValueStr = set.defaultValue.toString();
            int dotIndex = defaultValueStr.indexOf('.');

            if (dotIndex != -1) {
                decimalPlaces = defaultValueStr.length() - dotIndex - 1;
            }

            float factor = (float) Math.pow(10, decimalPlaces);
            float roundedValue = Math.round(rawValue * factor) / factor;
            float clampedValue = Math.clamp(roundedValue, set.min.floatValue(), set.max.floatValue());
            lowSetValue(clampedValue);
        }
    }
    private void scrollSetValue(int delta) {
        boolean integer = set.defaultValue instanceof Integer;

        if (integer) {
            int clampedValue = MathHelper.clamp(set.getValue().intValue() + delta, set.min.intValue(), set.max.intValue());
            lowSetValue(clampedValue);
        } else {
            int decimalPlaces = 0;
            String defaultValueStr = set.defaultValue.toString();
            int dotIndex = defaultValueStr.indexOf('.');
            if (dotIndex != -1) {
                decimalPlaces = defaultValueStr.length() - dotIndex - 1;
            }

            float factor = (float) Math.pow(10, decimalPlaces);
            float value = set.getValue().floatValue() + (delta / factor);
            float roundedValue = Math.round(value * factor) / factor;
            float clampedValue = Math.clamp(roundedValue, set.min.floatValue(), set.max.floatValue());
            lowSetValue(clampedValue);
        }
    }
    private void lowSetValue(Number n) {
        Number v = set.getValue();
        if (!v.equals(n)) {
            oldValue = v;
            delta = 0;
            set.setValue(n);
        }
    }
}
