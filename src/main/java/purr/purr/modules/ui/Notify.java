package purr.purr.modules.ui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import purr.purr.events.impl.EventTick;
import purr.purr.modules.Parent;
import purr.purr.modules.settings.Setting;
import meteordevelopment.orbit.EventHandler;
import purr.purr.utils.math.AnimHelper;
import purr.purr.utils.RGB;

import java.util.*;

// ... (import statements remain the same)

public class Notify extends Parent {
    private Setting<Integer> liveTimeSet = new Setting<>(
        "live time",
        50,
        10, 100
    );

    public Map<NotifyType, LinkedHashMap<String, Float>> history = new LinkedHashMap<>();
    public Map<NotifyType, LinkedHashMap<String, Boolean>> reverseAnim = new LinkedHashMap<>();
    public Map<NotifyType, LinkedHashMap<String, Integer>> liveTime = new LinkedHashMap<>();

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
        LinkedHashMap<String, Boolean> reverseMap = reverseAnim.get(NotifyType.Important);
        LinkedHashMap<String, Integer> timeMap = liveTime.get(NotifyType.Important);

        float y = 10;
        Iterator<Map.Entry<String, Float>> iterator = notifyHistory.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Float> entry = iterator.next();
            String text = entry.getKey();
            float animPercent = entry.getValue();

            // Рендеринг текста
            Text renderText = Text.literal(text.strip());
            float screenWidth = context.getScaledWindowWidth();
            float screenHeight = context.getScaledWindowHeight();

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(
                    (screenWidth / 2) - ((float) textRenderer.getWidth(renderText) / 2),
                    y * animPercent / 100
            );
            context.drawTextWithShadow(
                    textRenderer,
                    renderText,
                    0,
                    0,
                    RGB.getColor(255, 255, 255, (int) (255 * animPercent / 100))
            );
            context.getMatrices().popMatrix();

            y += textRenderer.fontHeight + 5;

            // Удаление уведомления после завершения анимации
            if (animPercent <= 0 && reverseMap.getOrDefault(text, false)) {
                iterator.remove();
                reverseMap.remove(text);
                timeMap.remove(text);
            }
        }

        history.put(NotifyType.Important, notifyHistory);
        reverseAnim.put(NotifyType.Important, reverseMap);
        liveTime.put(NotifyType.Important, timeMap);
    }

    private void animHandler() {
        for (NotifyType notifyType : NotifyType.values()) {
            LinkedHashMap<String, Boolean> reverseMap = reverseAnim.get(notifyType);
            LinkedHashMap<String, Float> animMap = history.get(notifyType);

            AnimHelper.handleMapAnim(animMap, reverseMap, AnimHelper.AnimMode.EaseOut);

            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, Float> entry : animMap.entrySet()) {
                if (Math.abs(entry.getValue()) <= 0f && Boolean.TRUE.equals(reverseMap.get(entry.getKey()))) {
                    toRemove.add(entry.getKey());
                }
            }

            for (String key : toRemove) {
                animMap.remove(key);
                reverseMap.remove(key);
                liveTime.get(notifyType).remove(key);
            }

            history.put(notifyType, animMap);
            reverseAnim.put(notifyType, reverseMap);
        }
    }

    private void closeHandler() {
        for (NotifyType notifyType : NotifyType.values()) {
            LinkedHashMap<String, Boolean> reverseMap = reverseAnim.get(notifyType);
            LinkedHashMap<String, Integer> timeMap = liveTime.get(notifyType);

            for (Map.Entry<String, Integer> entry : timeMap.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue() <= 0 && !reverseMap.getOrDefault(key, false)) {
                    reverseMap.put(key, true);
                }
            }

            int limit = limits.getOrDefault(notifyType, 5);
            if (timeMap.size() > limit) {
                Iterator<String> iterator = timeMap.keySet().iterator();
                int overflowCount = timeMap.size() - limit;

                for (int i = 0; i < overflowCount && iterator.hasNext(); i++) {
                    String key = iterator.next();
                    reverseMap.put(key, true);
                    iterator.remove();
                }
            }

            reverseAnim.put(notifyType, reverseMap);
            liveTime.put(notifyType, timeMap);
        }
    }

    @EventHandler
    private void onTick(EventTick e) {
        for (NotifyType notifyType : NotifyType.values()) {
            LinkedHashMap<String, Integer> timeMap = liveTime.get(notifyType);
            List<String> toRemove = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : timeMap.entrySet()) {
                int newValue = entry.getValue() - 1;
                if (newValue <= 0) {
                    toRemove.add(entry.getKey());
                } else {
                    entry.setValue(newValue);
                }
            }

            for (String key : toRemove) {
                timeMap.put(key, 0);
            }

            liveTime.put(notifyType, timeMap);
        }
    }

    public void add(String text, NotifyType notifyType) {
        LinkedHashMap<String, Float> h = history.get(notifyType);
        LinkedHashMap<String, Boolean> r = reverseAnim.get(notifyType);
        LinkedHashMap<String, Integer> t = liveTime.get(notifyType);

        String uniqueText = text;
        int suffix = 1;
        while (h.containsKey(uniqueText)) {
            uniqueText = text + " [" + (++suffix) + "]";
        }

        h.put(uniqueText, 0f);
        r.put(uniqueText, false);
        t.put(uniqueText, liveTimeSet.getValue());

        history.put(notifyType, h);
        reverseAnim.put(notifyType, r);
        liveTime.put(notifyType, t);
    }
}
