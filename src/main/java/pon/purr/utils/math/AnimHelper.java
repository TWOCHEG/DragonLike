package pon.purr.utils.math;

import java.util.Iterator;
import java.util.Map;

import pon.purr.Purr;
import pon.purr.modules.ui.Gui;
import pon.purr.utils.GetAnimDiff;

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

        public float getDiff(float percent, float diff) {
            if (this.equals(EaseIn)) {
                return diff * percent;
            } else if (this.equals(EaseOut)) {
                return diff * (1 - percent);
            } else if (this.equals(EaseInOut)) {
                if (percent < 0.5f) {
                    return diff * percent;
                } else {
                    return diff * (1 - percent);
                }
            } else {
                return diff;
            }
        }
    }

    public static float handleAnimValue(boolean reverse, float percent, float diff, AnimMode mode) {
        if (percent == 1 && !reverse) return 1;
        if (percent == 0 && reverse) return 0;
        float finalDiff = diff;
        if (mode != null) {
            finalDiff = Math.max(mode.getDiff(percent, finalDiff), 0.01f);
        }

        percent = reverse ? percent - finalDiff : percent + finalDiff;
        return Math.clamp(percent, 0, 1);
    }
    public static float handleAnimValue(boolean reverse, float percent, float diff) {
        return handleAnimValue(reverse, percent, diff, AnimMode.EaseInOut);
    }
    public static float handleAnimValue(boolean reverse, float percent, AnimMode mode) {
        return handleAnimValue(reverse, percent, GetAnimDiff.get(), mode);
    }
    public static float handleAnimValue(boolean reverse, float percent) {
        return handleAnimValue(reverse, percent, GetAnimDiff.get(), AnimMode.EaseInOut);
    }

    public static String getAnimText(String startText, String endText, float percent) {
        percent = Math.max(0, Math.min(1, percent));
        if (percent <= 0.5f) {
            float keepRatio = 1 - percent * 2;
            int keepChars = Math.round(startText.length() * keepRatio);
            return startText.substring(0, keepChars);
        } else {
            float drawRatio = (percent - 0.5f) * 2;
            int drawChars = Math.round(endText.length() * drawRatio);
            return endText.substring(0, drawChars);
        }
    }
}
