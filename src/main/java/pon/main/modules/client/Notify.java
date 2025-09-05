package pon.main.modules.client;

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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

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
        super("notify", Main.Categories.client, true);

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
            if (n.visibleFactor == 0 && n.animForward) {
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
            for (String t : TextUtils.splitForRender(c.getNotifyText(), 200, s -> textRenderer.getWidth(s))) {
                int currentWidth = textRenderer.getWidth(t);
                if (currentWidth > maxWidth) {
                    maxWidth = currentWidth;
                }
            }

            int height = Render2D.drawTextWithTransfer(
                c.getNotifyText(), context, textRenderer,
                (int) (screenWidth / 2) - 100,
                (int) (y * animFactor),
                200, 2,
                ColorUtils.applyOpacity(c.getColor(), animFactor),
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
            for (String t : TextUtils.splitForRender(c.getNotifyText(), 200, s -> textRenderer.getWidth(s))) {
                int currentWidth = textRenderer.getWidth(t);
                if (currentWidth > maxWidth) {
                    maxWidth = currentWidth;
                }
            }
            int height = (textRenderer.fontHeight + padding) * TextUtils.splitForRender(c.getNotifyText(), 200, s -> textRenderer.getWidth(s)).size();
            Render2D.fill(
                context,
                (int) (screenWidth - ((maxWidth + textPadding + padding) * animFactor)),
                (int) y - (height - padding),
                (int) ((screenWidth - (textPadding * animFactor)) + padding),
                (int) y + textPadding,
                CategoryArea.makeAColor(100 * animFactor),
                5, 2
            );
            Render2D.drawTextWithTransfer(
                c.notifyText, context, textRenderer,
                (int) (screenWidth - ((maxWidth + textPadding) * animFactor)),
                (int) ((y - height) + textPadding), 200,
                padding, ColorUtils.applyOpacity(c.getColor(), animFactor)
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

            Text renderText = Text.literal(c.getNotifyText());
            float animFactor = c.visibleFactor;
            float screenWidth = context.getScaledWindowWidth();
            float offset = c.animForward ?
            (-textRenderer.getWidth(renderText) / 2f) * (1 - animFactor) :
            (textRenderer.getWidth(renderText) / 2f) * (1 - animFactor);

            Render2D.fill(
                context,
                (int) (((screenWidth / 2 - textRenderer.getWidth(renderText) / 2f) - padding) + offset),
                (int) (y - padding * 2),
                (int) (((screenWidth / 2 + textRenderer.getWidth(renderText) / 2f) + padding) + offset),
                (int) (y + textRenderer.fontHeight + padding),
                CategoryArea.makeAColor(100 * animFactor),
                5,
                2
            );
            context.drawText(
                textRenderer,
                renderText,
                (int) ((screenWidth / 2) - (textRenderer.getWidth(renderText) / 2) - offset),
                (int) (y),
                ColorUtils.applyOpacity(c.getColor(), animFactor),
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
        private String notifyText;

        public float visibleFactor = 0f;
        public boolean animForward = true;

        private Supplier<Boolean> animForwardProvider = null;
        private Supplier<String> notifyTextProvider = null;

        int totalTickDelta = 0;

        public List<Integer> colors = new LinkedList<>();

        public NotifyData(String text, NotifyType notifyType) {
            this.notifyText = text;
            this.notifyType = notifyType;
            this.liveTime = Main.MODULE_MANAGER.getModule(Notify.class).liveTimeSet.getValue();
        }

        public NotifyData setNotifyTextProvider(Supplier<String> notifyTextProvider) {
            this.notifyTextProvider = notifyTextProvider;
            return this;
        }
        public NotifyData setAnimForwardProvider(Supplier<Boolean> animForwardProvider) {
            this.animForwardProvider = animForwardProvider;
            return this;
        }

        public int getColor() {
            if (colors.isEmpty()) {
                return new Color(255, 255, 255).getRGB();
            }

            final float speed = 20.0f;
            final int size = colors.size();

            float cyclePosition = (totalTickDelta / speed) % size;
            int currentIndex = (int) cyclePosition;
            int nextIndex = (currentIndex + 1) % size;
            float progress = cyclePosition - currentIndex;

            int color1 = colors.get(currentIndex);
            int color2 = colors.get(nextIndex);

            int r1 = (color1 >> 16) & 0xFF;
            int g1 = (color1 >> 8) & 0xFF;
            int b1 = color1 & 0xFF;

            int r2 = (color2 >> 16) & 0xFF;
            int g2 = (color2 >> 8) & 0xFF;
            int b2 = color2 & 0xFF;

            int r = (int) (r1 + (r2 - r1) * progress);
            int g = (int) (g1 + (g2 - g1) * progress);
            int b = (int) (b1 + (b2 - b1) * progress);

            return (r << 16) | (g << 8) | b;
        }

        @EventHandler
        private void onTick(EventTick e) {
            totalTickDelta ++;

            if (liveTime > 0 && animForwardProvider == null) {
                liveTime--;
                if (liveTime < 1) {
                    animForward = false;
                }
            }
        }

        public String getNotifyText() {
            if (notifyTextProvider != null) {
                return notifyTextProvider.get().strip();
            }
            return notifyText.strip();
        }

        @EventHandler
        private void onRender(EventOnRender e) {
            if (animForwardProvider != null) {
                animForward = animForwardProvider.get();
            }
            visibleFactor = AnimHelper.handle(animForward, visibleFactor);
        }
    }
}
