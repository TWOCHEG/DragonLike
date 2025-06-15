package purr.purr.utils;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyName {
    public static String get(int keyKode) {
        InputUtil.Key key = InputUtil.fromKeyCode(keyKode, GLFW.glfwGetKeyScancode(keyKode));
        return key.getTranslationKey()
            .substring(key.getTranslationKey().lastIndexOf('.') + 1)
            .toUpperCase();
    }
}
