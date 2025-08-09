package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.gui.ModulesGui;
import pon.purr.utils.Color;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;


import java.util.*;

public class HintsArea extends RenderArea {
    private final List<String> hints;
    private final ModulesGui gui;

    private boolean open = false;
    private float openPercent = 0f;

    private final int textPadding = 2;

    private final String closeText = "hints";

    public HintsArea(String text, ModulesGui gui) {
        super();
        this.gui = gui;
        this.hints = text.lines().toList();
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        int textAlpha = (int) (200 * gui.openPercent);

        String longestText;
        longestText = hints.get(0);
        for (String str : hints) {
            if (str.length() > longestText.length()) {
                longestText = str;
            }
        }

        int openHeight = textPadding + (textRenderer.fontHeight + textPadding) * hints.size();
        int openWidth = textRenderer.getWidth(longestText) + (textPadding * 2);

        int closeHeight = textPadding * 2 + textRenderer.fontHeight;
        int closeWidth = textRenderer.getWidth(closeText) + textPadding * 2;

        width = (int) (closeWidth + (openWidth - closeWidth) * openPercent);
        height = (int) (closeHeight + (openHeight - closeHeight) * openPercent);

        open = checkHovered(mouseX, mouseY);

        Render.fill(
            context,
            startX,
            startY - height,
            startX + width,
            startY,
            Color.fromRGB(0, 0, 0, 100 * gui.openPercent),
            2,
            2
        );
        context.drawText(
            textRenderer,
            closeText,
            startX + textPadding,
            startY - (textRenderer.fontHeight + textPadding),
            Color.fromRGB(255, 255, 255, textAlpha * (1 - openPercent)),
            false
        );
        if (openPercent > 0) {
            int text0 = startY - height + textPadding;
            int offSet = 0;
            for (String s : hints) {
                context.drawText(
                    textRenderer,
                    s,
                    startX + textPadding,
                    (int) (text0 + offSet * openPercent),
                    Color.fromRGB(255, 255, 255, textAlpha * openPercent),
                    false
                );
                offSet += textRenderer.fontHeight + textPadding;
            }
        }

        super.render(context, startX, startY - closeHeight, closeWidth, closeHeight, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        openPercent = AnimHelper.handleAnimValue(!open, openPercent);
    }
}
