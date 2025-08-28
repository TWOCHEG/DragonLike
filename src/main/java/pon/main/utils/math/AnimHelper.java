package pon.main.utils.math;

import pon.main.Main;
import pon.main.modules.ui.Gui;

public class AnimHelper {
    /**
     * @EaseInOut замедление в начале и в конце (по умолчанию)
     * @EaseIn замедление в начале
     * @EaseOut замедление в конце
     */
    public enum AnimMode {
        EaseIn,
        EaseOut,
        EaseInOut,
        Linear;

        public float getDiff(float factor, float diff) {
            if (this.equals(EaseIn)) {
                return diff * factor;
            } else if (this.equals(EaseOut)) {
                return diff * (1 - factor);
            } else if (this.equals(EaseInOut)) {
                if (factor < 0.5f) {
                    return diff * factor;
                } else {
                    return diff * (1 - factor);
                }
            } else {
                return diff;
            }
        }
    }

    public static float handle(boolean reverse, float factor, float diff, AnimMode mode) {
        if (factor == 1 && !reverse) return 1;
        if (factor == 0 && reverse) return 0;

        float finalDiff = diff;
        if (mode != null) {
            finalDiff = Math.max(mode.getDiff(factor, finalDiff), 0.01f);
        }

        factor = reverse ? factor - finalDiff : factor + finalDiff;
        return Math.clamp(factor, 0, 1);
    }
    public static float handle(boolean reverse, float factor, float diff) {
        return handle(reverse, factor, diff, AnimMode.EaseInOut);
    }
    public static float handle(boolean reverse, float factor, AnimMode mode) {
        return handle(reverse, factor, GetAnimDiff.get(), mode);
    }
    public static float handle(boolean reverse, float factor) {
        return handle(reverse, factor, GetAnimDiff.get(), AnimMode.EaseInOut);
    }

    public static String getAnimText(String startText, String endText, float factor) {
        factor = Math.max(0, Math.min(1, factor));
        if (factor <= 0.5f) {
            float keepRatio = 1 - factor * 2;
            int keepChars = Math.round(startText.length() * keepRatio);
            return startText.substring(0, keepChars);
        } else {
            float drawRatio = (factor - 0.5f) * 2;
            int drawChars = Math.round(endText.length() * drawRatio);
            return endText.substring(0, drawChars);
        }
    }
}
