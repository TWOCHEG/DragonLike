package pon.main.utils.math;

public class Hover {
    public static boolean hoverCheck(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (
            mouseX >= x &&
            mouseX <= x + width &&
            mouseY >= y &&
            mouseY <= y + height
        );
    }
}