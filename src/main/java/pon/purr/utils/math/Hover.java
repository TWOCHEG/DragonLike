package pon.purr.utils.math;

public class Hover {
    public static boolean hoverCheck(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x
        && mouseX <= x + w
        && mouseY >= y
        && mouseY <= y + h;
    }
}
