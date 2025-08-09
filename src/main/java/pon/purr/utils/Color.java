package pon.purr.utils;

import java.util.*;

public class Color {
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
    public static List<Integer> fromInt(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >> 24) & 0xff;
        return Arrays.asList(red, green, blue, alpha);
    }
}
