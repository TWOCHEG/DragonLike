package com.purr.modules.ui;

import com.purr.modules.Parent;
import com.purr.modules.settings.Setting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.purr.utils.GetAnimDiff;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.*;

public class Notify extends Parent {
    private Setting<Integer> liveTimeSet = new Setting<>(
        "live time",
        50,
        10, 100
    );

    public Map<NotifyType, Object> history = new LinkedHashMap<>();
    public Map<NotifyType, Object> reverseAnim = new LinkedHashMap<>();
    public Map<NotifyType, Object> liveTime = new LinkedHashMap<>();

    private Map<NotifyType, Integer> limits = Map.of(
        NotifyType.Important, 1,
        NotifyType.Module, 5,
        NotifyType.System, 4
    );

    public enum NotifyType {
        Important, Module, System
    }

    public Notify() {
        super("notify", "notify", "ui");
        enable = config.get("enable", true);
        for (NotifyType notifyType : NotifyType.values()) {
            history.put(notifyType, new LinkedHashMap<>());
            reverseAnim.put(notifyType, new LinkedHashMap<>());
            liveTime.put(notifyType, new LinkedHashMap<>());
        }

        ClientTickEvents.START_CLIENT_TICK.register(context -> {
            liveTimeHandler();
            animHandler();
            sizeHandler();
//            System.out.println(history);
        });

        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {

        });
    }

//    private void renderImportant(DrawContext context) {
//        TextRenderer textRenderer = client.textRenderer;
//        Map<String, Float> notifyHistory = (Map<String, Float>) history.get(NotifyType.Important);
//        float y = 10;
//        for (String text : notifyHistory.keySet()) {
//            float animPercent = notifyHistory.get(text);
//            Text renderText = Text.literal(text);
//
//            float screenWidth = context.getScaledWindowWidth();
//            float screenHeight = context.getScaledWindowHeight();
//
//            context.getMatrices().push();
//            context.getMatrices().translate((screenWidth / 2) - ((float) textRenderer.getWidth(renderText) / 2), y * animPercent / 100, 1);
//            context.drawTextWithShadow(
//                textRenderer,
//                renderText,
//                0,
//                0,
//                RGB.getColor(255, 255, 255, 255 * (int) animPercent / 100)
//            );
//            context.getMatrices().pop();
//
//            y += textRenderer.fontHeight;
//        }
//    }

    private void sizeHandler() {
        for (NotifyType notifyType : NotifyType.values()) {
            int limit = limits.getOrDefault(notifyType, 1);
            if (((Map<?, ?>) history.get(notifyType)).size() > limit) {
                String key = ((Map<String, ?>) history.get(notifyType)).keySet().iterator().next();

                Map<String, Boolean> reverseHistory = (Map<String, Boolean>) reverseAnim.getOrDefault(notifyType, new LinkedHashMap<>());
                reverseHistory.remove(key);
                reverseAnim.put(notifyType, reverseHistory);
            }
        }
    }

    private void animHandler() {
        float deltaTime = GetAnimDiff.get();
        float clampedDelta = 10;

        for (NotifyType notifyType : NotifyType.values()) {
            Map<String, Float> notifyHistory = (Map<String, Float>) history.get(notifyType);
            Map<String, Boolean> reverseHistory = (Map<String, Boolean>) reverseAnim.get(notifyType);

            for (String key : notifyHistory.keySet()) {
                boolean animReverse = reverseHistory.getOrDefault(key, true);
                float animPercent = notifyHistory.get(key);

                if (!animReverse && animPercent < 100) {
                    float increment = (clampedDelta * (100 - animPercent)) / 100;
                    animPercent += Math.max(0.1f, increment);
                } else if (animReverse && animPercent > 0) {
                    float decrement = (clampedDelta * animPercent) / 100;
                    animPercent -= Math.max(0.1f, decrement);
                }

                animPercent = Math.clamp(animPercent, 0.0f, 100.0f);
                notifyHistory.put(key, animPercent);
            }
            history.put(notifyType, notifyHistory);
        }
    }

    private void liveTimeHandler() {
//        System.out.println(liveTime);
        for (NotifyType notifyType : NotifyType.values()) {
            Map<String, Integer> mapTimes = (Map<String, Integer>) liveTime.get(notifyType);
            if (mapTimes == null) continue;

            Iterator<Map.Entry<String, Integer>> iterator = mapTimes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = iterator.next();
                int value = entry.getValue() - 1;
                entry.setValue(value);

                if (value < 1) {
                    iterator.remove();
                    Map<String, Boolean> reverseHistory = (Map<String, Boolean>) reverseAnim.getOrDefault(notifyType, new LinkedHashMap<>());
                    reverseHistory.remove(entry.getKey());
                    reverseAnim.put(notifyType, reverseHistory);
                }
            }
        }
    }

    public void add(String text, NotifyType notifyType) {
        if (!enable) return;

        Map<String, Float> notifyHistory = (Map<String, Float>) history.getOrDefault(notifyType, new LinkedHashMap<>());
        String lastKey = "";
        for (String key : notifyHistory.keySet()) {
            lastKey = key;
        }
        if (lastKey.equals(text)) return;

        notifyHistory.put(text, 0.0f);
        history.put(notifyType, notifyHistory);

        Map<String, Boolean> reverseHistory = (Map<String, Boolean>) reverseAnim.getOrDefault(notifyType, new LinkedHashMap<>());
        reverseHistory.put(text, false);
        reverseAnim.put(notifyType, reverseHistory);

        Map<String, Integer> liveHistory = (Map<String, Integer>) liveTime.getOrDefault(notifyType, new LinkedHashMap<>());
        liveHistory.put(text, liveTimeSet.getValue());
        liveTime.put(notifyType, liveHistory);
    }
}
