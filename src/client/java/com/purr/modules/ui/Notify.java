package com.purr.modules.ui;

import com.purr.modules.Parent;
import com.purr.modules.settings.Setting;
import com.purr.utils.RGB;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.purr.utils.getAnimDiff;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.font.TextRenderer;

import java.util.*;

public class Notify extends Parent {
    private Setting<Integer> liveTimeSet = new Setting<>(
        "live time",
        "live_time",
        config.get("live_time", 50.0F).intValue(),
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

            for (NotifyType notifyType : NotifyType.values()) {

                int limit = limits.getOrDefault(notifyType, 1);
                Map<String, Float> notifyHistory = (Map<String, Float>) history.get(notifyType);
                int size = notifyHistory.keySet().size();

                Map<String, Integer> mapTimes = (Map<String, Integer>) liveTime.get(notifyType);

                String firstKey = "";
                for (String key : notifyHistory.keySet()) {
                    firstKey = key;
                    break;
                }

                if (size > limit || mapTimes.getOrDefault(firstKey, 0) < 1) {
                    Map<String, Boolean> reverseHistory = (Map<String, Boolean>) reverseAnim.getOrDefault(notifyType, new LinkedHashMap<>());
                    reverseHistory.remove(firstKey);

                    reverseAnim.put(notifyType, reverseHistory);
                }
            }
        });

        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            renderImportant(matrices);

        });
    }

    private void renderImportant(DrawContext context) {
        TextRenderer textRenderer = client.textRenderer;
        Map<String, Float> notifyHistory = (Map<String, Float>) history.get(NotifyType.Important);
        float y = 10;
        for (String text : notifyHistory.keySet()) {
            float animPercent = notifyHistory.get(text);
            Text renderText = Text.literal(text);

            float screenWidth = context.getScaledWindowWidth();
            float screenHeight = context.getScaledWindowHeight();

            context.getMatrices().push();
            context.getMatrices().translate((screenWidth / 2) - ((float) textRenderer.getWidth(renderText) / 2), y * animPercent / 100, 1);
            context.drawTextWithShadow(
                textRenderer,
                renderText,
                0,
                0,
                RGB.getColor(255, 255, 255, 255 * (int) animPercent / 100)
            );
            context.getMatrices().pop();

            y += textRenderer.fontHeight;
        }
    }

    private void animHandler() {
        float deltaTime = getAnimDiff.get();
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
        System.out.println(liveTime);
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
