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

    public void add(String text, NotifyType notifyType) {
    }
}
