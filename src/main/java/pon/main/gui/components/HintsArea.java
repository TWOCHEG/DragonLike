package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.gui.ModulesGui;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;


import java.util.*;

public class HintsArea extends RenderArea {
    private final List<String> hints;
    private final ModulesGui gui;

    private boolean open = false;

    private final int textPadding = 2;

    private final String closeText = "hints";

    public HintsArea(String text, ModulesGui gui) {
        super();
        this.gui = gui;
        this.showFactor = 0;
        this.hints = text.lines().toList();
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        int textAlpha = (int) (200 * gui.openFactor);

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

        width = (int) (closeWidth + (openWidth - closeWidth) * showFactor);
        height = (int) (closeHeight + (openHeight - closeHeight) * showFactor);

        open = checkHovered(mouseX, mouseY);

        Render2D.fill(
            context,
            startX,
            startY - height,
            startX + width,
            startY,
            CategoryArea.makeAColor(100 * gui.openFactor),
            bigPadding, 2
        );
        context.drawText(
            textRenderer,
            closeText,
            startX + textPadding,
            startY - (textRenderer.fontHeight + textPadding),
            ColorUtils.fromRGB(255, 255, 255, textAlpha * (1 - showFactor)),
            false
        );
        if (showFactor > 0) {
            int text0 = startY - height + textPadding;
            int offSet = 0;
            for (String s : hints) {
                context.drawText(
                    textRenderer,
                    s,
                    startX + textPadding,
                    (int) (text0 + offSet * showFactor),
                    ColorUtils.fromRGB(255, 255, 255, textAlpha * showFactor),
                    false
                );
                offSet += textRenderer.fontHeight + textPadding;
            }
        }
        super.render(context, startX, startY - closeHeight, closeWidth, closeHeight, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(open, showFactor);
    }
}
