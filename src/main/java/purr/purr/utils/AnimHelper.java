package purr.purr.utils;

import java.util.Iterator;
import java.util.Map;

import purr.purr.Purr;
import purr.purr.modules.ui.Gui;

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
            percent = Math.max(percent, 1);
            if (this.equals(EaseIn)) {
                return diff * percent / 100;
            } else if (this.equals(EaseOut)) {
                return diff * (100 - percent) / 100;
            } else if (this.equals(EaseInOut)) {
                if (percent < 50f) {
                    return diff * percent / 100;
                } else {
                    return diff * (100 - percent) / 100;
                }
            } else {
                return diff;
            }
        }
    }

    public static float handleAnimValue(boolean reverse, float percent, float diff, AnimMode mode) {
        if (percent == 100f && !reverse) return 100f;
        if (percent == 0f && reverse) return 0f;

        if (Purr.moduleManager != null) {
            Gui guiModule = (Gui) Purr.moduleManager.getModuleByClass(Gui.class);
            if (guiModule != null && !guiModule.animEnable.getValue()) {
                return reverse ? 0f : 100f;
            }
        }

        float finalDiff = Math.max(mode.getDiff(percent, diff), 1);

        percent = reverse ? percent - finalDiff : percent + finalDiff;

        return Math.clamp(percent, 0f, 100f);
    }
    public static float handleAnimValue(boolean reverse, float percent, AnimMode mode) {
        return handleAnimValue(reverse, percent, GetAnimDiff.get(), mode);
    }
    public static float handleAnimValue(boolean reverse, float percent) {
        return handleAnimValue(reverse, percent, GetAnimDiff.get(), AnimMode.EaseInOut);
    }

    public static void handleMapAnim(Map<Object, Float> animMap, Map<Object, Boolean> reverceMap, AnimMode mode, Boolean delete) {
        if (!animMap.isEmpty()) {
            Iterator<Object> it = animMap.keySet().iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                float percent = animMap.get(obj);
                boolean reverse = reverceMap.getOrDefault(obj, false);

                float newPercent = handleAnimValue(reverse, percent, mode);
                if (newPercent == percent) continue;
                animMap.put(obj, newPercent);

                if ((reverse && newPercent <= 1) && delete) {
                    it.remove();
                    reverceMap.remove(obj);
                }
            }
        }
    }
    public static void handleMapAnim(Map<Object, Float> animMap, Map<Object, Boolean> reverceMap, AnimMode mode) {
        handleMapAnim(animMap, reverceMap, mode, true);
    }
    public static void handleMapAnim(Map<Object, Float> animMap, Map<Object, Boolean> reverceMap) {
        handleMapAnim(animMap, reverceMap, AnimMode.EaseInOut);
    }
    public static void handleMapAnim(Map<Object, Float> animMap, Map<Object, Boolean> reverceMap, Boolean delete) {
        handleMapAnim(animMap, reverceMap, AnimMode.EaseInOut, delete);
    }

    public static String getAnimText(String startText, String endText, int percent) {
        percent = Math.max(0, Math.min(100, percent));
        float pct = percent / 100f;
        if (pct <= 0.5f) {
            float keepRatio = 1 - pct * 2;
            int keepChars = Math.round(startText.length() * keepRatio);
            return startText.substring(0, keepChars);
        } else {
            float drawRatio = (pct - 0.5f) * 2;
            int drawChars = Math.round(endText.length() * drawRatio);
            return endText.substring(0, drawChars);
        }
    }
}
