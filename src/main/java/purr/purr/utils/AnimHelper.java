package purr.purr.utils;

import java.util.Iterator;
import java.util.Map;

public class AnimHelper {
    /**
     * @EaseInOut замедление в начале и в конце (по умолчанию)
     * @EaseIn замедление в начале
     * @EaseOut замедление в конце
     */
    public enum AnimMode {
        EaseInOut, EaseIn, EaseOut
    }
    public static float handleAnimValue(boolean reverse, float percent, AnimMode mode) {
        percent = Math.max(percent, 0.1f);

        if (!reverse && percent < 100) {
            float t = percent / 100f;
            float easeSpeed = Math.max(calculateEaseSpeed(mode, t), 0.1f);
            percent += Math.max(0.1f, GetAnimDiff.get() * easeSpeed);
        } else if (reverse && percent > 0) {
            float t = (100 - percent) / 100f;
            float easeSpeed = 1 - Math.max(calculateEaseSpeed(mode, t), 0.1f);
            percent -= Math.max(0.1f, GetAnimDiff.get() * easeSpeed);
        }

        return Math.clamp(percent, 0, 100);
    }

    private static float calculateEaseSpeed(AnimMode mode, float t) {
        switch(mode) {
            case EaseIn:
                return t * t * t; // Кубическая функция для ease-in [[5]]
            case EaseOut:
                float f = 1 - t;
                return 1 - f * f * f; // Обратная кубическая функция для ease-out [[5]]
            case EaseInOut:
                if (t < 0.5f) {
                    return 4 * t * t * t; // Удвоенная кубическая для первой половины [[5]]
                } else {
                    float a = 1 - t;
                    return 1 - 4 * a * a * a; // Удвоенная обратная кубическая для второй половины [[5]]
                }
            default:
                return 1; // Линейная анимация
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
