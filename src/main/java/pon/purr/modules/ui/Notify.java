package pon.purr.modules.ui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import pon.purr.Purr;
import pon.purr.events.impl.EventOnRender;
import pon.purr.events.impl.EventTick;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.Setting;
import meteordevelopment.orbit.EventHandler;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;
import pon.purr.utils.Color;

import java.util.*;

public class Notify extends Parent {
    public Setting<Integer> liveTimeSet = new Setting<>(
        "live time",
        50,
        10, 100
    );

    public LinkedList<NotifyData> notifications = new LinkedList<>();

    public enum NotifyType {
        Important, Module, System;

        public int getLimit() {
            Map<NotifyType, Integer> limits = Map.of(
                NotifyType.Important, 2,
                NotifyType.Module, 5,
                NotifyType.System, 4
            );
            return limits.getOrDefault(this, 3);
        }
    }

    public Notify() {
        super("notify", Purr.Categories.ui);
        enable = config.get("enable", true);

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            TextRenderer textRenderer = mc.textRenderer;
            renderImportant(context, textRenderer);
            renderSystem(context, textRenderer);
            renderModule(context, textRenderer);
        });
    }

    private void renderImportant(DrawContext context, TextRenderer textRenderer) {
        float y = 10;

        for (NotifyData c : notifications) {
            if (!c.notifyType.equals(NotifyType.Important)) continue;

            Text renderText = Text.literal(c.notifyText);
            float screenWidth = context.getScaledWindowWidth();
            float animPercent = c.visiblePercent;

            context.drawCenteredTextWithShadow(
                textRenderer,
                renderText,
                (int) (screenWidth / 2),
                (int) (y * animPercent),
                Color.fromRGB(255, 255, 255, (int) (255 * animPercent))
            );

            y += (textRenderer.fontHeight + 5) * animPercent;

            if (animPercent == 0f && c.reverseAnim) {
                notifications.remove(c);
            }
        }
    }
    private void renderSystem(DrawContext context, TextRenderer textRenderer) {
        float y = context.getScaledWindowHeight() - 5 - textRenderer.fontHeight;
        int padding = 2;
        int textPadding = 5;

        for (int i = notifications.size() - 1; i >= 0; i--) {
            NotifyData c = notifications.get(i);
            if (!c.notifyType.equals(NotifyType.System)) continue;

            Text renderText = Text.literal(c.notifyText);
            float animPercent = c.visiblePercent;
            float screenWidth = context.getScaledWindowWidth();

            Render.fill(
                context,
                (int) (screenWidth - textPadding + padding * animPercent),
                (int) (y - padding),
                (int) (screenWidth - (textRenderer.getWidth(renderText) + textPadding) * animPercent - padding),
                (int) (y + textRenderer.fontHeight + padding),
                Color.fromRGB(0, 0, 0, 100 * animPercent),
                textRenderer.getWidth(renderText) / 20,
                3
            );
            context.drawText(
                textRenderer,
                renderText,
                (int) (screenWidth - ((textRenderer.getWidth(renderText) + textPadding) * animPercent)),
                (int) (y),
                Color.fromRGB(255, 255, 255, (int) (255 * animPercent)),
                false
            );

            y += (textRenderer.fontHeight + 5) * animPercent;

            if (animPercent == 0f && c.reverseAnim) {
                notifications.remove(c);
            }
        }
    }
    private void renderModule(DrawContext context, TextRenderer textRenderer) {
        float y = (context.getScaledWindowHeight() / 2f) + 10;
        int padding = 2;

        for (int i = notifications.size() - 1; i >= 0; i--) {
            NotifyData c = notifications.get(i);
            if (!c.notifyType.equals(NotifyType.Module)) continue;
            Text renderText = Text.literal(c.notifyText);
            float animPercent = c.visiblePercent;
            float screenWidth = context.getScaledWindowWidth();
            float offset = c.reverseAnim ?
            (-textRenderer.getWidth(renderText) / 2f) * (1 - animPercent) :
            (textRenderer.getWidth(renderText) / 2f) * (1 - animPercent);

            Render.fill(
                context,
                (int) (((screenWidth / 2 - textRenderer.getWidth(renderText) / 2f) - padding) + offset),
                (int) (y - padding * 2),
                (int) (((screenWidth / 2 + textRenderer.getWidth(renderText) / 2f) + padding) + offset),
                (int) (y + textRenderer.fontHeight + padding),
                Color.fromRGB(0, 0, 0, (int) (100 * animPercent)),
                5,
                2
            );
            context.drawText(
                textRenderer,
                renderText,
                (int) ((screenWidth / 2) - (textRenderer.getWidth(renderText) / 2) - offset),
                (int) (y),
                Color.fromRGB(255, 255, 255, (int) (255 * animPercent)),
                false
            );

            y += (textRenderer.fontHeight + 5) * animPercent;

            if (animPercent == 0f && c.reverseAnim) {
                notifications.remove(c);
            }
        }
    }

    public void add(NotifyData n) {
        if (!enable || mc.currentScreen != null) return;
        Purr.EVENT_BUS.subscribe(n);
        notifications.add(n);
    }

    public static class NotifyData {
        public int liveTime;
        public NotifyType notifyType;
        public String notifyText;

        public float visiblePercent = 0f;
        public boolean reverseAnim = false;

        public NotifyData(String text, NotifyType notifyType, int liveTime) {
            this.notifyText = text;
            this.notifyType = notifyType;
            this.liveTime = liveTime;
        }

        @EventHandler
        private void onTick(EventTick e) {
            if (liveTime > 0) {
                liveTime--;
                if (liveTime < 1) {
                    reverseAnim = true;
                }
            }
        }

        @EventHandler
        private void onRender(EventOnRender e) {
            visiblePercent = AnimHelper.handleAnimValue(reverseAnim, visiblePercent);
        }
    }
}
