package purr.purr.utils;

public class RGB {
    public static int getColor(int r, int g, int b, int a) {
        if (a <= 4 || a > 255) {
            return 0x00000000;
        }
        r = Math.clamp(r, 0, 255);
        g = Math.clamp(g, 0, 255);
        b = Math.clamp(b, 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    public static int getColor(int r, int g, int b, Float a) {
        return getColor(r, g, b, a.intValue());
    }
    public static int getColor(int r, int g, int b) {
        return getColor(r, g, b, 255);
    }
}
