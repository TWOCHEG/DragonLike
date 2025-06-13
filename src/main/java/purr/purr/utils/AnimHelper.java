package purr.purr.utils;

import java.util.Iterator;
import java.util.Map;

public class AnimHelper {
    /**
     * @EaseInOut - замедление в начале и в конце (по умолчанию)
     * @EaseIn - замедление в начале
     * @EaseOut - замедление в конце
     */
    public enum AnimMode {
        EaseInOut, EaseIn, EaseOut
    }
    public static float handleAnimValue(boolean reverse, float percent, AnimMode mode) {
        percent = Math.max(percent, 0.1f);

        float t = reverse ? (100 - percent) / 100f : percent / 100f;
        float easeFactor = calculateEaseSpeed(mode, t);
        easeFactor = Math.max(easeFactor, 0.1f);

        float delta = GetAnimDiff.get() * easeFactor * (reverse ? -1 : 1);
        percent += delta;

        return Math.max(0, Math.min(100, percent));
    }

    private static float calculateEaseSpeed(AnimMode mode, float t) {
        switch(mode) {
            case EaseIn:
                return t * t;
            case EaseOut:
                return 1 - (1 - t) * (1 - t);
            case EaseInOut:
                if (t < 0.5f) return 4 * t * t * t;
                else return 1 - 4 * (1 - t) * (1 - t) * (1 - t);
            default: return 1;
        }
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
