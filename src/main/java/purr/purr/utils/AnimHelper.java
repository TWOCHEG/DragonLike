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
        EaseInOut;

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
        public float getDiff(float percent) {
            return getDiff(percent, GetAnimDiff.get());
        }
    }

    public static float handleAnimValue(boolean reverse, float percent, AnimMode mode) {
        if (Purr.moduleManager != null) {
            Gui guiModule = (Gui) Purr.moduleManager.getModuleByClass(Gui.class);
            if (guiModule != null && !guiModule.animEnable.getValue()) {
                return reverse ? 0f : 100f;
            }
        }

        float diff = Math.max(mode.getDiff(percent), 1);

        percent = reverse ? percent - diff : percent + diff;

        return Math.clamp(percent, 0f, 100f);
    }
    public static float handleAnimValue(boolean reverse, float percent) {
        return handleAnimValue(reverse, percent, AnimMode.EaseInOut);
    }

    public static void handleMapAnim(Map<Object, Float> animMap, Map<Object, Boolean> reverceMap, AnimMode mode, Boolean delete) {
        if (!animMap.isEmpty()) {
            Iterator<Object> it = animMap.keySet().iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                float percent = animMap.get(obj);
                boolean reverse = reverceMap.getOrDefault(obj, false);

                percent = handleAnimValue(reverse, percent, mode);
                animMap.put(obj, percent);

                if ((reverse && percent <= 1) && delete) {
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
