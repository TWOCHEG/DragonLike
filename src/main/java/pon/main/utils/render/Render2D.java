package pon.main.utils.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import pon.main.utils.TextUtils;

import java.util.LinkedList;

public class Render2D {
    /**
     * @rounded - неиспользуйте эта хуйня не работает
     * @linear - работает
     */
    public enum CurveType {
        rounded, linear;
        public int getValue(int i, int r, int steps) {
            if (this.equals(linear)) {
                return (r * i) / steps;
            } else if (this.equals(rounded)) {
                int s = (r * i) / steps;
                s = s * (steps / (i + 1));
                return s;
            }
            return 0;
        }
    }

    public static void fill(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int r,
        int s
    ) {
        fill(
            context,
            x1, y1, x2, y2, c,
            r, s,
            CurveType.linear
        );
    }
    public static void fill(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int r,
        int s,
        CurveType t
    ) {
        int stepSize = r / s;

        for (int i = 0; i < s; i++) {
            int offset = t.getValue(i, r, s);
            context.fill(
                x1 + offset,
                (y1 + r) - offset,
                x2 - offset,
                (y1 + r) - (offset + stepSize),
                c
            );
        }
        context.fill(
            x1,
            y1 + r,
            x2,
            y2 - r,
            c
        );
        for (int i = 0; i < s; i++) {
            int offset = t.getValue(i, r, s);
            context.fill(
                x1 + offset,
                (y2 - r) + offset,
                x2 - offset,
                (y2 - r) + (offset + stepSize),
                c
            );
        }
    }

    public static void fillPart(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int s,
        boolean up
    ) {
        fillPart(
            context,
            x1, y1, x2, y2, c,
            s,
            up,
            CurveType.linear
        );
    }
    public static void fillPart(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int s,
        boolean up,
        CurveType t
    ) {
        int radius = y2 - y1;
        int stepSize = radius / s;
        if (up) {
            for (int i = 0; i < s; i++) {
                int offset = t.getValue(i, radius, s);
                context.fill(
                    x1 + offset,
                    (y1 + radius) - offset,
                    x2 - offset,
                    (y1 + radius) - (offset + stepSize),
                    c
                );
            }
        } else {
            for (int i = 0; i < s; i++) {
                int offset = t.getValue(i, radius, s);
                context.fill(
                    x1 + offset,
                    (y2 - radius) + offset,
                    x2 - offset,
                    (y2 - radius) + (offset + stepSize),
                    c
                );
            }
        }
    }

    public static int drawTextWithTransfer(
        String text, DrawContext context, TextRenderer textRenderer, int x, int y, int maxWidth, int padding, int color,
        boolean centered
    ) {
        LinkedList<String> toDrawText = TextUtils.splitForRender(text, maxWidth, s -> textRenderer.getWidth(s));
        int textY = y;
        for (String t : toDrawText) {
            context.drawText(
                textRenderer,
                t.strip(),
                centered ? x + (maxWidth / 2 - textRenderer.getWidth(t.strip()) / 2) : x,
                textY,
                color,
                false
            );
            textY += textRenderer.fontHeight + padding;
        }
        return textY - y - padding;
    }
    public static int drawTextWithTransfer(
        String text, DrawContext context, TextRenderer textRenderer, int x, int y, int maxWidth, int padding, int color
    ) {
        return drawTextWithTransfer(text, context, textRenderer, x, y, maxWidth, padding, color, false);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }
    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }
    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int) interpolate(oldValue, newValue, (float) interpolationValue);
    }
}
