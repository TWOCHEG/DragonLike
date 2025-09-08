package pon.main.utils.math;

import net.minecraft.client.util.Window;

import static pon.main.modules.Parent.mc;

public class MouseUtils {
    public static boolean hoverCheck(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (
            mouseX >= x &&
            mouseX <= x + width &&
            mouseY >= y &&
            mouseY <= y + height
        );
    }

    public static double[] getPos() {
        Window window = mc.getWindow();
        double mouseX = mc.mouse.getScaledX(window);
        double mouseY = mc.mouse.getScaledY(window);
        return new double[]{mouseX, mouseY};
    }
    public static float[] getPosFloat() {
        Window window = mc.getWindow();
        float mouseX = (float) mc.mouse.getScaledX(window);
        float mouseY = (float) mc.mouse.getScaledY(window);
        return new float[]{mouseX, mouseY};
    }
}