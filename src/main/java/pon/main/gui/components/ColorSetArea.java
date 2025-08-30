package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.modules.settings.ColorSet;
import pon.main.utils.ColorUtils;
import pon.main.utils.TextUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;

import java.awt.*;

public class ColorSetArea extends RenderArea {
    private final ColorSet color;

    public ColorSetArea(ColorSet color, RenderArea parentArea) {
        super(parentArea);
        this.color = color;
        this.showFactor = color.getVisible() ? 1 : 0;
        for (ColorType t : ColorType.values()) {
            if (!color.hasAlpha && t.equals(ColorType.a)) continue;
            areas.add(new Slider(this, t));
        }
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float showFa = showFactor * parentArea.showFactor;
        int colorWidth = 20;

        int headerHeight = padding + Render2D.drawTextWithTransfer(
            color.getName(), context, textRenderer,
            startX, startY, width, padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * showFa)
        );
        height += headerHeight;

        for (RenderArea area : areas) {
            area.render(context, startX, startY + height, width - colorWidth - bigPadding, 0, mouseX, mouseY);
            height += area.height + padding;
        }
        height -= padding;

        Color c = ColorUtils.reverseColor(new Color(color.getValue(), false));
        Render2D.fill(
            context, (startX + width) - colorWidth, startY + headerHeight, (startX + width), startY + height,
            ColorUtils.fromRGB(c.getRed(), c.getGreen(), c.getBlue(), 50 * showFa),
            padding, 2
        );
        Render2D.fill(
            context, (startX + width) - colorWidth + 1, startY + headerHeight + 1, (startX + width - 1), startY + height - 1,
            ColorUtils.fromRGB(color.r(), color.g(), color.b(), color.a() * showFa),
            padding, 2
        );

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(color.getVisible(), showFactor);
    }

    public class Slider extends RenderArea {
        private ColorType colorType;

        private boolean inputting = false;
        private String inputText = "";

        private boolean dragged = false;

        private float lightFactor = 0;
        private boolean light = false;

        private int textWidth = textRenderer.getWidth("255") + padding;

        private Slider(ColorSetArea color, ColorType colorType) {
            super(color);
            this.colorType = colorType;
        }

        @Override
        public void render(
            DrawContext context,
            int startX, int startY,
            int width, int height,
            double mouseX, double mouseY
        ) {
            height += textRenderer.fontHeight;

            int c = ColorUtils.fromRGB(
                200 + (colorType.equals(ColorType.r) ? 25 : 0),
                200 + (colorType.equals(ColorType.g) ? 25 : 0),
                200 + (colorType.equals(ColorType.b) ? 25 : 0),
                (200 - (100 * lightFactor)) * parentArea.showFactor
            );
            context.drawText(
                textRenderer, inputting ? (inputText.isEmpty() ? "..." : inputText) : ("" + colorType.get((ColorSetArea) parentArea)),
                startX, startY, c, false
            );
            context.drawHorizontalLine(
                startX + textWidth, startX + width, startY + (height / 2),
                ColorUtils.fromRGB(190, 190, 190, 255 * parentArea.showFactor)
            );
            float sliderFactor = colorType.get((ColorSetArea) parentArea) / 255f;
            int sliderPoseX = (int) (startX + textWidth + (width - textWidth) * sliderFactor);
            int sliderPoseY = startY + (textRenderer.fontHeight / 2);
            context.fill(
                sliderPoseX,
                sliderPoseY - 1,
                sliderPoseX + 1,
                sliderPoseY + 2,
                ColorUtils.fromRGB(190, 190, 190, 255 * parentArea.showFactor)
            );

            super.render(context, startX, startY, width, height, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (checkHovered(x, y, textWidth - padding, height, mouseX, mouseY)) {
                inputting = true;
                inputText = inputText.isEmpty() ? "" + colorType.get((ColorSetArea) parentArea) : inputText;
                return true;
            } else if (inputting) {
                inputting = false;
            }
            if (checkHovered(x + textWidth, y, width - textWidth, height, mouseX, mouseY)) {
                dragged = true;
                setValue(mouseX);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (dragged) {
                setValue(mouseX);
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (dragged) {
                dragged = false;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        public void setValue(double mouseX) {
            colorType.set((ColorSetArea) parentArea, (int) Math.clamp(255 * ((mouseX - (x + textWidth)) / (width - textWidth)), 0, 255));
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
                    int v;
                    try {
                        v = Integer.parseInt(inputText);
                    } catch (Exception e) {
                        v = colorType.get((ColorSetArea) parentArea);
                    }
                    colorType.set((ColorSetArea) parentArea, Math.clamp(v, 0, 255));
                    inputText = "";
                }
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
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
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            if (inputting && inputText.length() < 3) {
                inputText += chr;
                return true;
            }
            return super.charTyped(chr, modifiers);
        }
    }

    public enum ColorType {
        r, g, b, a;

        public void set(ColorSetArea c, int v) {
            ColorSet color = c.color;
            if (this.equals(r)) {
                color.setColor(new Color(v, color.g(), color.b(), color.a()));
            } else if (this.equals(g)) {
                color.setColor(new Color(color.r(), v, color.b(), color.a()));
            } else if (this.equals(b)) {
                color.setColor(new Color(color.r(), color.g(), v, color.a()));
            } else {
                color.setColor(new Color(color.r(), color.g(), color.b(), v));
            }
        }

        public int get(ColorSetArea c) {
            ColorSet color = c.color;
            if (this.equals(r)) {
                return color.r();
            } else if (this.equals(g)) {
                return color.g();
            } else if (this.equals(b)) {
                return color.b();
            } else {
                return color.a();
            }
        }
    }
}
