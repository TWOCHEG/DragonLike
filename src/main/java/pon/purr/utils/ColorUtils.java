package pon.purr.utils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ColorUtils {
    public static int fromRGB(int r, int g, int b, int a) {
        if (a <= 4 || a > 255) {
            return 0x00000000;
        }
        r = Math.clamp(r, 0, 255);
        g = Math.clamp(g, 0, 255);
        b = Math.clamp(b, 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    public static int fromRGB(int r, int g, int b, Float a) {
        return fromRGB(r, g, b, a.intValue());
    }
    public static int fromRGB(int r, int g, int b) {
        return fromRGB(r, g, b, 255);
    }

    /**
     * @param color цвет в int кодировке
     * @return red, green, blue, alpha
     */
    public static Color fromInt(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >> 24) & 0xff;
        return new Color(red, green, blue, alpha);
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }
    public static int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity)).getRGB();
    }
}
