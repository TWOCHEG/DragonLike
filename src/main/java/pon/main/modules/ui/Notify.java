package pon.main.modules.ui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import pon.main.Main;
import pon.main.events.impl.EventOnRender;
import pon.main.events.impl.EventTick;
import pon.main.gui.components.CategoryArea;
import pon.main.modules.Parent;
import pon.main.modules.settings.Setting;
import meteordevelopment.orbit.EventHandler;
import pon.main.utils.TextUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.ColorUtils;

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
        super("notify", Main.Categories.ui);
        enable = config.get("enable", true);

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            TextRenderer textRenderer = mc.textRenderer;
            // норм
            renderImportant(context, textRenderer);
            clearList();
            renderSystem(context, textRenderer);
            clearList();
            renderModule(context, textRenderer);
            clearList();
        });
    }

    private void clearList() {
        Iterator<NotifyData> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            NotifyData n = iterator.next();
            if (n.visibleFactor == 0 && n.reverseFactor) {
                Main.EVENT_BUS.unsubscribe(n);
                iterator.remove();
            }
        }
    }

    private void renderImportant(DrawContext context, TextRenderer textRenderer) {
        float y = 10;

        for (NotifyData c : notifications) {
            if (!Objects.equals(c.notifyType, NotifyType.Important)) continue;

            float screenWidth = context.getScaledWindowWidth();
            float animFactor = c.visibleFactor;

            int maxWidth = 0;
            for (String t : TextUtils.splitForRender(c.notifyText, 200, s -> textRenderer.getWidth(s))) {
                int currentWidth = textRenderer.getWidth(t);
                if (currentWidth > maxWidth) {
                    maxWidth = currentWidth;
                }
            }

            int height = Render2D.drawTextWithTransfer(
                c.notifyText, context, textRenderer,
                (int) ((screenWidth / 2) - (maxWidth / 2)),
                (int) (y * animFactor),
                200,
                2,
                ColorUtils.fromRGB(255, 255, 255, (int) (255 * animFactor)),
                true
            );

            y += (height + 5) * animFactor;
        }
    }
    private void renderSystem(DrawContext context, TextRenderer textRenderer) {
        float y = context.getScaledWindowHeight() - 8;
        int padding = 2;
        int textPadding = 5;

        for (int i = notifications.size() - 1; i >= 0; i--) {
            NotifyData c = notifications.get(i);
            if (!Objects.equals(c.notifyType, NotifyType.System)) continue;

            float animFactor = c.visibleFactor;
            float screenWidth = context.getScaledWindowWidth();

            int maxWidth = 0;
            for (String t : TextUtils.splitForRender(c.notifyText, 200, s -> textRenderer.getWidth(s))) {
                int currentWidth = textRenderer.getWidth(t);
                if (currentWidth > maxWidth) {
                    maxWidth = currentWidth;
                }
            }
            int height = (textRenderer.fontHeight + padding) * TextUtils.splitForRender(c.notifyText, 200, s -> textRenderer.getWidth(s)).size();
            Render2D.fill(
                context,
                (int) (screenWidth - ((maxWidth + textPadding + padding) * animFactor)),
                (int) y - (height - padding),
                (int) ((screenWidth - (textPadding * animFactor)) + padding),
                (int) y + textPadding,
                CategoryArea.makeAColor((100 * animFactor) / 255),
                5, 2
            );
            Render2D.drawTextWithTransfer(
                c.notifyText, context, textRenderer,
                (int) (screenWidth - ((maxWidth + textPadding) * animFactor)),
                (int) ((y - height) + textPadding), 200,
                padding, ColorUtils.fromRGB(255, 255, 255, 255 * animFactor)
            );

            y -= (height + textPadding) * animFactor;
        }
    }
    private void renderModule(DrawContext context, TextRenderer textRenderer) {
        float y = (context.getScaledWindowHeight() / 2f) + 10;
        int padding = 2;

        for (int i = notifications.size() - 1; i >= 0; i--) {
            NotifyData c = notifications.get(i);
            if (!Objects.equals(c.notifyType, NotifyType.Module)) continue;

            Text renderText = Text.literal(c.notifyText);
            float animFactor = c.visibleFactor;
            float screenWidth = context.getScaledWindowWidth();
            float offset = c.reverseFactor ?
            (-textRenderer.getWidth(renderText) / 2f) * (1 - animFactor) :
            (textRenderer.getWidth(renderText) / 2f) * (1 - animFactor);

            Render2D.fill(
                context,
                (int) (((screenWidth / 2 - textRenderer.getWidth(renderText) / 2f) - padding) + offset),
                (int) (y - padding * 2),
                (int) (((screenWidth / 2 + textRenderer.getWidth(renderText) / 2f) + padding) + offset),
                (int) (y + textRenderer.fontHeight + padding),
                CategoryArea.makeAColor((100 * animFactor) / 255),
                5,
                2
            );
            context.drawText(
                textRenderer,
                renderText,
                (int) ((screenWidth / 2) - (textRenderer.getWidth(renderText) / 2) - offset),
                (int) (y),
                ColorUtils.fromRGB(255, 255, 255, (int) (255 * animFactor)),
                false
            );

            y += (textRenderer.fontHeight + 5) * animFactor;
        }
    }

    public void add(NotifyData n) {
        if (enable) {
            Main.EVENT_BUS.subscribe(n);
            notifications.add(n);
        }
    }

    public static class NotifyData {
        public int liveTime;
        public NotifyType notifyType;
        public String notifyText;

        public float visibleFactor = 0f;
        public boolean reverseFactor = false;

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
                    reverseFactor = true;
                }
            }
        }

        @EventHandler
        private void onRender(EventOnRender e) {
            visibleFactor = AnimHelper.handle(reverseFactor, visibleFactor);
        }
    }
}
