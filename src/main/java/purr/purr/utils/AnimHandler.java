package purr.purr.utils;

import java.util.Iterator;
import java.util.Map;

public class AnimHandler {
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

        if (!reverse && percent < 100) {
            float t = percent / 100f;
            float easeSpeed = calculateEaseSpeed(mode, t);
            percent += Math.max(0.1f, GetAnimDiff.get() * easeSpeed);
        } else if (reverse && percent > 0) {
            float t = (100 - percent) / 100f;
            float easeSpeed = 1 - calculateEaseSpeed(mode, t);
            percent -= Math.max(0.1f, GetAnimDiff.get() * easeSpeed);
        }

        return Math.clamp(percent, 0, 100);
    }
    private static float calculateEaseSpeed(AnimMode mode, float t) {
        switch(mode) {
            case EaseIn:
                return t * t;
            case EaseOut:
                return 1 - (1 - t) * (1 - t);
            case EaseInOut:
                if (t < 0.5f) {
                    return 2 * t * t;
                } else {
                    return 1 - 2 * (1 - t) * (1 - t);
                }
            default:
                return 1 - Math.abs(2 * t - 1);
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
}
