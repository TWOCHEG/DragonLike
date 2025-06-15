package purr.purr.modules.ui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import purr.purr.events.impl.EventTick;
import purr.purr.modules.Parent;
import purr.purr.modules.settings.Setting;
import meteordevelopment.orbit.EventHandler;
import purr.purr.utils.AnimHelper;
import purr.purr.utils.RGB;

import java.util.*;

public class Notify extends Parent {
    private Setting<Integer> liveTimeSet = new Setting<>(
        "live time",
        50,
        10, 100
    );

    public Map<NotifyType, LinkedHashMap> history = new LinkedHashMap<>();
    public Map<NotifyType, LinkedHashMap> reverseAnim = new LinkedHashMap<>();
    public Map<NotifyType, LinkedHashMap> liveTime = new LinkedHashMap<>();

    private Map<NotifyType, Integer> limits = Map.of(
        NotifyType.Important, 1,
        NotifyType.Module, 5,
        NotifyType.System, 4
    );

    public enum NotifyType {
        Important, Module, System
    }

    public Notify() {
        super("notify", "ui");
        enable = config.get("enable", true);
        for (NotifyType notifyType : NotifyType.values()) {
            history.put(notifyType, new LinkedHashMap<>());
            reverseAnim.put(notifyType, new LinkedHashMap<>());
            liveTime.put(notifyType, new LinkedHashMap<>());
        }

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            animHandler();
            closeHandler();
            renderImportant(context);
        });
    }

    private void renderImportant(DrawContext context) {
        TextRenderer textRenderer = client.textRenderer;
        LinkedHashMap<String, Float> notifyHistory = history.get(NotifyType.Important);
        float y = 10;
        for (String text : notifyHistory.keySet()) {
            Text renderText = Text.literal(text.strip());

            if (!notifyHistory.containsKey(text)) {
                notifyHistory.remove(text);
                continue;
            }
            float animPercent = notifyHistory.get(text);

            float screenWidth = context.getScaledWindowWidth();
            float screenHeight = context.getScaledWindowHeight();

            context.getMatrices().push();
            context.getMatrices().translate(
                (screenWidth / 2) - ((float) textRenderer.getWidth(renderText) / 2),
                y * animPercent / 100,
                1
            );
            context.drawTextWithShadow(
                textRenderer,
                renderText,
                0,
                0,
                RGB.getColor(255, 255, 255, (int) (255 * animPercent / 100))
            );
            context.getMatrices().pop();

            y += textRenderer.fontHeight + 5;
        }
        history.put(NotifyType.Important, notifyHistory);
    }

    private void animHandler() {
        for (NotifyType notifyType : NotifyType.values()) {
            LinkedHashMap<Object, Boolean> reverseMap = reverseAnim.get(notifyType);
            LinkedHashMap<Object, Float> animMap = history.get(notifyType);
            AnimHelper.handleMapAnim(animMap, reverseMap, AnimHelper.AnimMode.EaseOut);
            for (Object k1 : animMap.keySet()) {
                if (k1 instanceof String k) {
                    if (animMap.get(k) == 0f && reverseMap.get(k)) {
                        animMap.remove(k);
                    }
                }
            }
            history.put(notifyType, animMap);
        }
    }

    private void closeHandler() {
        for (NotifyType notifyType : NotifyType.values()) {
            LinkedHashMap<Object, Boolean> reverseMap = reverseAnim.get(notifyType);
            LinkedHashMap<Object, Integer> timeMap = liveTime.get(notifyType);

            LinkedList<String> notReverse = new LinkedList<>();
            for (Object k1 : reverseMap.keySet()) {
                if (k1 instanceof String k) {
                    if (!reverseMap.get(k) || timeMap.getOrDefault(k, 0) < 1) {
                        notReverse.add(k);
                    }
                }
            }
            if (notReverse.size() > limits.getOrDefault(notifyType, 5)) {
                int i = 0;
                for (String e : notReverse) {
                    if (notReverse.size() - limits.getOrDefault(notifyType, 5) >= i) break;
                    notReverse.remove(e);
                    reverseMap.put(e, true);
                    i++;
                }
            }
            reverseAnim.put(notifyType, reverseMap);
        }
    }

//    @EventHandler
//    private void onTick(EventTick e) {
//        for (NotifyType notifyType : NotifyType.values()) {
//            LinkedHashMap<Object, Integer> timeMap = liveTime.get(notifyType);
//            for (Object k : timeMap.keySet()) {
//                timeMap.put(k, timeMap.get(k) - 1);
//                if (timeMap.get(k) <= 0) {
//                    timeMap.remove(k);
//                }
//            }
//            liveTime.put(notifyType, timeMap);
//        }
//    }

    public void add(String text, NotifyType notifyType) {
        LinkedHashMap<String, Float> h = history.get(notifyType);
        String last = "";
        for (String e : h.keySet()) {
            last = e;
        }
        if (Objects.equals(last, text)) return;
        while (h.containsKey(text)) {
            text += " ";
        }

        LinkedHashMap<Object, Integer> timeMap = liveTime.get(notifyType);
        timeMap.put(text, liveTimeSet.getValue());
        liveTime.put(notifyType, timeMap);

        h.put(text, 0f);
        history.put(notifyType, h);
    }
}
